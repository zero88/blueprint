package io.zero88.qwe;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.QWEConfig.QWEDeployConfig;
import io.zero88.qwe.exceptions.ConfigException;

public class QWEConfigTest {

    @Test
    public void test_default() {
        QWEConfig cfg = QWEConfig.create();
        QWEConfig from = IConfig.fromClasspath("system.json", QWEConfig.class);
        System.out.println(cfg.toJson().encodePrettily());
        Assertions.assertEquals(cfg.toJson(), from.toJson());

        Assertions.assertNull(from.getBootConfig());

        Assertions.assertNotNull(from.getDeployConfig());
        Assertions.assertEquals(new DeploymentOptions().toJson(), from.getDeployConfig().toJson());

        Assertions.assertNotNull(from.getAppConfig());
        Assertions.assertEquals(QWEAppConfig.DEFAULT_DATADIR, from.getAppConfig().dataDir());
        Assertions.assertTrue(from.getAppConfig().other().isEmpty());
        Assertions.assertEquals(new DeliveryOptions().toJson(), from.getAppConfig().getDeliveryOptions().toJson());
    }

    @Test
    public void test_deserialize_from_blank_json() {
        Assertions.assertEquals(QWEConfig.create().toJson(), IConfig.from(new JsonObject(), QWEConfig.class).toJson());
    }

    @Test
    public void test_deserialize_full_system_config() {
        QWEConfig config = QWEConfig.builder().bootConfig(new QWEBootConfig()).build();
        System.out.println(config.getBootConfig().toJson().encodePrettily());
        QWEConfig from = IConfig.fromClasspath("full-system-cfg.json", QWEConfig.class);
        JsonHelper.assertJson(config.getBootConfig().toJson(), from.getBootConfig().toJson());
        Assertions.assertEquals("/data", from.getAppConfig().getDataDir());
        Assertions.assertEquals(config.getDeployConfig().toJson(), from.getDeployConfig().toJson());
        Assertions.assertEquals(config.getAppConfig().toJson(), from.getAppConfig().toJson());
    }

    @Test
    public void test_deserializeInvalidJson_shouldFailed() {
        TestHelper.assertCause(() -> IConfig.from("hello", QWEConfig.class), ConfigException.class,
                               DecodeException.class);
    }

    @Test
    public void test_deserializeRootHaveRedundantProperties_shouldFailed() {
        String jsonStr = "{\"__redundant__\":{},\"__system__\":{}}";
        TestHelper.assertCause(() -> IConfig.from(jsonStr, QWEConfig.class), ConfigException.class,
                               IllegalArgumentException.class);
    }

    @Test
    public void test_deserialize_child_from_root() {
        String jsonStr = "{\"__deploy__\":{\"ha\":true,\"instances\":10,\"maxWorkerExecuteTime\":60000000000," +
                         "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"worker\":false,\"workerPoolSize\":20}}";
        DeploymentOptions cfg = IConfig.from(jsonStr, QWEDeployConfig.class);
        Assertions.assertNotNull(cfg);
        Assertions.assertTrue(cfg.isHa());
        Assertions.assertEquals(10, cfg.getInstances());
        Assertions.assertEquals(60000000000L, cfg.getMaxWorkerExecuteTime());
        Assertions.assertEquals(TimeUnit.NANOSECONDS, cfg.getMaxWorkerExecuteTimeUnit());
        Assertions.assertFalse(cfg.isWorker());
    }

    @Test
    public void test_deserialize_appCfg_from_root() {
        String jsonStr = "{\"__app__\":{\"http.port\":8085}}";
        QWEConfig cfg = IConfig.from(jsonStr, QWEConfig.class);
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals(new QWEDeployConfig().toJson(), cfg.getDeployConfig().toJson());
        Assertions.assertNotNull(cfg.getAppConfig());
        Assertions.assertNotNull(cfg.getAppConfig().dataDir());
        Assertions.assertNotNull(cfg.getAppConfig().getDeliveryOptions());
        Assertions.assertEquals(8085, cfg.getAppConfig().lookup("http.port"));
    }

    @Test
    public void test_blank() {
        QWEConfig cfg = QWEConfig.create();
        Assertions.assertNotNull(cfg);
        Assertions.assertNotNull(cfg.getAppConfig());
        Assertions.assertNotNull(cfg.getAppConfig().dataDir());
        Assertions.assertTrue(cfg.getAppConfig().other().isEmpty());
        Assertions.assertNotNull(cfg.getDeployConfig());
        JsonHelper.assertJson(new JsonObject("{\"worker\":false,\"workerPoolSize\":20," +
                                             "\"maxWorkerExecuteTime\":60000000000,\"ha\":false,\"instances\":1," +
                                             "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\"}"),
                              cfg.getDeployConfig().toJson());
        Assertions.assertNull(cfg.getBootConfig());
    }

    @Test
    public void test_blank_with_app_cfg() {
        QWEConfig config = QWEConfig.create(new JsonObject().put("hello", 1));
        Assertions.assertNotNull(config);
        Assertions.assertNotNull(config.getAppConfig().dataDir());
        Assertions.assertNotNull(config.getAppConfig());
        Assertions.assertEquals(1, config.getAppConfig().other().size());
        Assertions.assertEquals(1, config.getAppConfig().lookup("hello"));
        Assertions.assertNotNull(config.getDeployConfig());
        JsonHelper.assertJson(new JsonObject("{\"worker\":false,\"workerPoolSize\":20," +
                                             "\"maxWorkerExecuteTime\":60000000000,\"ha\":false,\"instances\":1," +
                                             "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\"}"),
                              config.getDeployConfig().toJson());
        Assertions.assertNull(config.getBootConfig());
    }

}
