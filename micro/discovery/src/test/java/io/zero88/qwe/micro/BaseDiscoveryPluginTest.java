package io.zero88.qwe.micro;

import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.IConfig;
import io.zero88.qwe.PluginTestHelper.PluginDeployTest;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.eventbus.EventBusClient;

@ExtendWith(VertxExtension.class)
public abstract class BaseDiscoveryPluginTest implements PluginDeployTest<DiscoveryPlugin> {

    protected MicroConfig config;
    protected EventBusClient ebClient;
    protected ServiceDiscoveryApi discovery;
    @TempDir
    public Path folder;

    @BeforeAll
    public static void init() {
        TestHelper.setup();
    }

    @Override
    public Path testDir() {
        return folder.getRoot();
    }

    @BeforeEach
    public void tearUp(Vertx vertx, VertxTestContext ctx) {
        discovery = deploy(vertx, ctx, initConfig(), initProvider()).pluginContext().getDiscovery();
        ebClient = EventBusClient.create(createSharedData(vertx));
    }

    @AfterEach
    public void tearDown(Vertx vertx, VertxTestContext context) {
        vertx.close(context.succeedingThenComplete());
    }

    protected ServiceGatewayConfig getGatewayConfig() {
        return Objects.requireNonNull(config.lookup(ServiceGatewayConfig.NAME, ServiceGatewayConfig.class));
    }

    @Override
    public MicroConfig initConfig() {
        return config = IConfig.fromClasspath("local.json", MicroConfig.class);
    }

    @Override
    public DiscoveryPluginProvider initProvider() {
        return new DiscoveryPluginProvider();
    }

}
