package io.zero88.qwe.http.server.proxy;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.Before;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.zero88.qwe.ApplicationVerticle;
import io.zero88.qwe.DeployContext;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.VertxHelper;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.zero88.qwe.http.server.RestApiTestHelper;
import io.zero88.qwe.http.server.proxy.mock.MockGatewayServer;

public abstract class ProxyServiceTestBase extends HttpServerPluginTestBase implements RestApiTestHelper {

    @Before
    public void before(TestContext context) {
        super.before(context);
        startGatewayAndService(context, service(), getServiceOptions());
    }

    protected void startGatewayAndService(TestContext context, ApplicationVerticle app,
                                          DeploymentOptions serviceOptions) {
        CountDownLatch c1 = new CountDownLatch(1);
        DeploymentOptions options = new DeploymentOptions().setConfig(deployConfig(httpConfig.getPort()));
        ApplicationVerticle gateway = gateway().get();
        VertxHelper.deploy(vertx, context, DeployContext.builder()
                                                        .options(options)
                                                        .verticle(gateway)
                                                        .successAsserter(id -> c1.countDown())
                                                        .build());
        TestHelper.waitTimeout(timeoutInSecond(), c1);
        CountDownLatch c2 = new CountDownLatch(1);
        VertxHelper.deploy(vertx, context, DeployContext.builder()
                                                        .options(serviceOptions)
                                                        .verticle(app)
                                                        .successAsserter(d -> c2.countDown())
                                                        .build());
        //small delay for enable dynamic api
        TestHelper.sleep(500);
        TestHelper.waitTimeout(timeoutInSecond(), c2);
    }

    protected JsonObject deployConfig(int port) {
        return new JsonObject().put("__app__", new JsonObject().put("__http__", new JsonObject().put("port", port)));
    }

    @SuppressWarnings("unchecked")
    protected <T extends ApplicationVerticle> Supplier<T> gateway() {
        return () -> (T) new MockGatewayServer();
    }

    protected DeploymentOptions getServiceOptions() {
        return new DeploymentOptions();
    }

    protected abstract <T extends ApplicationVerticle> T service();

    protected int timeoutInSecond() {
        return TestHelper.TEST_TIMEOUT_SEC;
    }

}
