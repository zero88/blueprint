package io.zero88.qwe.sql.handler.schema;

import java.util.Optional;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.Table;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.jooqx.DSLAdapter;
import io.zero88.jooqx.SQLExecutor;
import io.zero88.jooqx.SQLPreparedQuery;
import io.zero88.jooqx.SQLResultCollector;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.sql.handler.EntityHandler;
import io.zero88.qwe.sql.spi.checker.CheckTableExistLoader;
import io.zero88.qwe.sql.spi.checker.TableExistChecker;

import lombok.NonNull;

/**
 * Represents for Schema handler.
 *
 * @since 1.0.0
 */
public interface SchemaHandler<S, B, PQ extends SQLPreparedQuery<B>, RS, RC extends SQLResultCollector<RS>,
                                  E extends SQLExecutor<S, B, PQ, RS, RC>> {

    String READINESS_ADDRESS = "SCHEMA_READINESS_ADDRESS";

    /**
     * Defines {@code table} to check whether database is new or not.
     *
     * @return the table
     * @since 1.0.0
     */
    @NonNull <R extends Record> Table<R> table();

    /**
     * Check database is new or not. Normally just checking one specific table is existed or not.
     * <p>
     * It
     *
     * @param jooqx the jooqx instance
     * @return {@code true} if new database, else otherwise
     * @see <a href="https://github.com/jOOQ/jOOQ/issues/8038">https://github.com/jOOQ/jOOQ/issues/8038</a>
     * @see TableExistChecker
     * @since 1.0.0
     */
    default Future<Boolean> isNew(E jooqx) {
        final SQLDialect dialect = jooqx.dsl().family();
        final TableExistChecker check = new CheckTableExistLoader().lookup(dialect);
        if (check == null) {
            return Future.succeededFuture(false);
        }
        final SelectConditionStep<Record1<Integer>> q = check.query(jooqx.dsl(), table());
        return jooqx.execute(dsl -> q, DSLAdapter.fetchExists(q.asTable()));
    }

    /**
     * Declares schema initializer.
     *
     * @return the schema initializer
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default @NonNull SchemaInitializer<S, B, PQ, RS, RC, E> initializer() {
        return SchemaInitializer.NO_DATA_INITIALIZER;
    }

    /**
     * Declares schema migrator.
     *
     * @return the schema migrator
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default @NonNull SchemaMigrator<S, B, PQ, RS, RC, E> migrator() {
        return SchemaMigrator.NON_MIGRATOR;
    }

    /**
     * Declares readiness address for notification after {@link #execute(EntityHandler)}.
     *
     * @param entityHandler given handler for helping lookup dynamic {@code address}
     * @return readiness address
     * @since 1.0.0
     */
    default @NonNull String readinessAddress(@NonNull EntityHandler<S, B, PQ, RS, RC, E> entityHandler) {
        return Optional.ofNullable((String) entityHandler.sharedData().getData(READINESS_ADDRESS))
                       .orElse(this.getClass().getName() + ".readiness");
    }

    /**
     * Do execute the initialization task or migration task.
     *
     * @param entityHandler the entity handler
     * @return the event message in single
     * @see EntityHandler
     * @see #initializer()
     * @see #migrator()
     * @since 1.0.0
     */
    default @NonNull Future<EventMessage> execute(@NonNull EntityHandler<S, B, PQ, RS, RC, E> entityHandler) {
        final EventBusClient c = entityHandler.transporter();
        final String address = readinessAddress(entityHandler);
        return this.isNew(entityHandler.jooqx())
                   .flatMap(b -> b ? initializer().execute(entityHandler) : migrator().execute(entityHandler))
                   .onFailure(t -> c.publish(address, EventMessage.error(EventAction.NOTIFY_ERROR, t)))
                   .onSuccess(msg -> {
                       final JsonObject headers = new JsonObject().put("status", msg.getStatus())
                                                                  .put("action", msg.getAction());
                       final RequestData reqData = RequestData.builder().body(msg.getData()).headers(headers).build();
                       c.publish(address, EventMessage.initial(EventAction.NOTIFY, reqData));
                   });
    }

}
