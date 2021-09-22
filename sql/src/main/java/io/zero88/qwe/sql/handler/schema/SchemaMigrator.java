package io.zero88.qwe.sql.handler.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.jooqx.SQLExecutor;
import io.zero88.jooqx.SQLPreparedQuery;
import io.zero88.jooqx.SQLResultCollector;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.sql.SQLError.SQLMigrationError;
import io.zero88.qwe.sql.handler.EntityHandler;

import lombok.NonNull;

/**
 * Represents for Schema migrator.
 *
 * @since 1.0.0
 */
public interface SchemaMigrator<S, B, PQ extends SQLPreparedQuery<B>, RS, RC extends SQLResultCollector<RS>,
                                   E extends SQLExecutor<S, B, PQ, RS, RC>>
    extends SchemaExecutor<S, B, PQ, RS, RC, E> {

    SchemaMigrator NON_MIGRATOR = entityHandler -> Future.succeededFuture(new JsonObject().put("migration", "Nope"));

    @Override
    default Logger logger() {
        return LoggerFactory.getLogger(SchemaMigrator.class);
    }

    @Override
    default Future<EventMessage> execute(@NonNull EntityHandler<S, B, PQ, RS, RC, E> entityHandler) {
        return doMigrate(entityHandler).recover(t -> Future.failedFuture(new SQLMigrationError(t)))
                                       .map(b -> EventMessage.success(EventAction.MIGRATE, b));
    }

    /**
     * Init default data
     *
     * @param handler an entity handler
     * @return json result in future
     */
    @NonNull Future<JsonObject> doMigrate(@NonNull EntityHandler<S, B, PQ, RS, RC, E> handler);

}

