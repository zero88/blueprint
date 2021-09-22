package io.zero88.qwe.sql.handler.schema;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Constraint;
import org.jooq.CreateIndexStep;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Key;
import org.jooq.Schema;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.jooqx.SQLExecutor;
import io.zero88.jooqx.SQLPreparedQuery;
import io.zero88.jooqx.SQLResultCollector;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.sql.SQLError.InitDataError;
import io.zero88.qwe.sql.SQLError.InitSchemaError;
import io.zero88.qwe.sql.handler.EntityHandler;
import io.zero88.qwe.utils.FutureHelpers;

import lombok.NonNull;

/**
 * Represents for Schema initializer.
 *
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public interface SchemaInitializer<S, B, PQ extends SQLPreparedQuery<B>, RS, RC extends SQLResultCollector<RS>,
                                      E extends SQLExecutor<S, B, PQ, RS, RC>>
    extends SchemaExecutor<S, B, PQ, RS, RC, E> {

    SchemaInitializer NO_DATA_INITIALIZER = entityHandler -> Future.succeededFuture(new JsonObject());

    @Override
    default Logger logger() {
        return LoggerFactory.getLogger(SchemaInitializer.class);
    }

    /**
     * Create {@code Schema}.
     *
     * @param handler the entity handler
     * @param schema  the schema
     * @return the schema in future for fluent API
     */
    default @NonNull Future<Schema> createSchema(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler,
                                                 @NonNull Schema schema) {
        return handler.jooqx()
                      .ddl(dsl -> dsl.createSchemaIfNotExists(schema))
                      .onSuccess(i -> logger().info(decor("Created schema [{}]"), schema.getName()))
                      .map(i -> schema);
    }

    /**
     * Create {@code Table}.
     *
     * @param handler the entity handler
     * @param table   the table
     * @return the table in future for fluent API
     */
    default @NonNull Future<Table> createTable(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler,
                                               @NonNull Table<?> table) {
        return handler.jooqx()
                      .ddl(dsl -> dsl.createTableIfNotExists(table).columns(table.fields()))
                      .onSuccess(i -> logger().info(decor("Created table [{}]"), table.getName()))
                      .map(i -> table);
    }

    /**
     * Create {@code Table Index} on each {@code Table}.
     *
     * @param handler the entity handler
     * @param table   the table
     * @return the table in future for fluent API
     */
    default @NonNull Future<Table> createIndexes(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler,
                                                 @NonNull Table<?> table) {
        //@formatter:off
        BiFunction<DSLContext, Index, CreateIndexStep> f = (dsl, idx) -> idx.getUnique()
                                                                            ? dsl.createUniqueIndexIfNotExists(idx.getName())
                                                                            : dsl.createIndexIfNotExists(idx.getName());
        return CompositeFuture.all(table.getIndexes().stream()
                                        .map(idx -> handler.jooqx()
                                                           .ddl(dsl -> f.apply(dsl, idx).on(table, idx.getFields()).where(idx.getWhere()))
                                                           .onSuccess(i -> logger().info(decor("Created index [{}][{}]"), table.getName(), idx.getName())))
                                        .collect(Collectors.toList()))
                              .map(i -> table);
        //@formatter:on
    }

    /**
     * Create {@code Constraints}.
     *
     * @param handler the entity handler
     * @param table   the table
     * @return the table in future for fluent API
     * @apiNote Creating constraints step is called only once after all {@code Table} and {@code Table Index} in
     *     {@code Schema} are created
     * @since 1.0.0
     */
    default @NonNull Future<Table> createConstraints(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler,
                                                     @NonNull Table<?> table) {
        Stream<Constraint> cs = table.getKeys().stream().map(Key::constraint);
        if (Objects.nonNull(table.getPrimaryKey())) {
            cs = Stream.concat(cs, Stream.of(table.getPrimaryKey().constraint()));
        }
        cs = Stream.concat(cs, table.getReferences().stream().map(ForeignKey::constraint)).distinct();
        //@formatter:off
        return CompositeFuture.all(cs.map(c -> handler.jooqx()
                                                      .ddl(dsl -> dsl.alterTable(table).add(c))
                                                      .onSuccess(i -> logger().info(decor("Created constraint [{}][{}]"),
                                                                                    table.getName(), c.getName())))
                                     .collect(Collectors.toList()))
                              .map(i -> table);
        //@formatter:on
    }

    /**
     * Create {@code Triggers}.
     *
     * @param handler the entity handler
     * @param table   the table
     * @return the table in future for fluent API
     * @apiNote Creating {@code triggers} step is called only once after {@code Table} and {@code Table Index} in
     *     {@code Schema} are created
     * @since 1.0.0
     */
    default @NonNull Future<Table> createTriggers(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler,
                                                  @NonNull Table<?> table) {
        return Future.succeededFuture(table);
    }

    /**
     * Do something with data schema in special case.
     *
     * @param handler the entity handler
     * @return succeeded future if no error
     * @since 1.0.0
     */
    default Future<Void> doMisc(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler) {
        return Future.succeededFuture();
    }

    /**
     * Setup database schema
     *
     * @param handler entity handler
     * @return succeeded future if no error
     */
    default Future<Void> setupSchema(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler) {
        logger().info(decor("Creating database model..."));
        Function<Table, Future<Table>> f1 = t -> createTable(handler, t).flatMap(a -> createIndexes(handler, a));
        Function<Table, Future<Table>> f2 = t -> createConstraints(handler, t);
        Function<Table, Future<Table>> f3 = t -> createTriggers(handler, t);
        BiFunction<List<Table>, Function<Table, Future<Table>>, Future<List<Table>>> bf
            = (ts, fun) -> FutureHelpers.flatten(CompositeFuture.all(ts.stream().map(fun).collect(Collectors.toList())),
                                                 Table.class);
        return CompositeFuture.all(handler.catalog()
                                          .schemaStream()
                                          .map(s -> this.createSchema(handler, s)
                                                        .map(x -> x.tableStream()
                                                                   .map(Table.class::cast)
                                                                   .collect(Collectors.toList()))
                                                        .flatMap(tt -> bf.apply(tt, f1)))
                                          .map(f -> f.flatMap(tt -> bf.apply(tt, f2)))
                                          .map(f -> f.flatMap(tt -> bf.apply(tt, f3)))
                                          .collect(Collectors.toList())).flatMap(list -> doMisc(handler));
    }

    /**
     * Init default data
     *
     * @param handler an entity handler
     * @return json result in future
     */
    @NonNull Future<JsonObject> initData(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler);

    @Override
    default Future<EventMessage> execute(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler) {
        return setupSchema(handler).recover(t -> Future.failedFuture(new InitSchemaError(t)))
                                   .flatMap(i -> initData(handler).recover(t -> Future.failedFuture(new InitDataError(t))))
                                   .map(b -> EventMessage.success(EventAction.INIT, b));
    }

}
