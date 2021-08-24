package io.zero88.qwe.sql.pg;

import org.jooq.SQLDialect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.sql.SQLPluginConfig;
import io.zero88.qwe.sql.SQLPluginFailedTest;
import io.zero88.qwe.sql.SQLPluginProvider;

public class PgUnableConnectTest extends SQLPluginFailedTest {

    @Test
    public void unable_connect(Vertx vertx, VertxTestContext testContext) {
        Checkpoint cp = testContext.checkpoint();
        deployFailed(vertx, testContext, new SQLPluginConfig().setDialect(SQLDialect.POSTGRES).setAutoDetect(true),
                     new SQLPluginProvider(PgEntityHandler.class), t -> testContext.verify(() -> {
                Assertions.assertTrue(t instanceof RuntimeException);
                Assertions.assertNotNull(t.getCause());
                Assertions.assertEquals("Connection refused: localhost/127.0.0.1:5432", t.getCause().getMessage());
                cp.flag();
            }));
    }

}