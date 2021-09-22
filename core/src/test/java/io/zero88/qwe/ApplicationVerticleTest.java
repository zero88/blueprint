package io.zero88.qwe;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.VertxImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.mock.DummyPluginProvider;
import io.zero88.qwe.mock.MockApplication;
import io.zero88.qwe.mock.MockExtension;
import io.zero88.qwe.mock.MockExtension.MockErrorExtension;
import io.zero88.qwe.mock.MockPluginConfig;
import io.zero88.qwe.mock.MockPluginProvider;

import ch.qos.logback.classic.Level;

@RunWith(VertxUnitRunner.class)
public class ApplicationVerticleTest {

    private Vertx vertx;
    private MockApplication app;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup(Level.TRACE);
    }

    @Before
    public void before() {
        vertx = Vertx.vertx();
        app = new MockApplication();
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test_compute_deployment_option_per_plugin(TestContext context) {
        Async async = context.async(2);
        Pattern p = Pattern.compile(Application.generateThreadName(app.getClass(), app.appName(), "[A-Za-z\\.]+"));
        Consumer<String> asserter = deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(3, vertx.deploymentIDs().size());
            vertx.deploymentIDs()
                 .stream()
                 .map(id -> ((VertxImpl) vertx).getDeployment(id))
                 .filter(Deployment::isChild)
                 .map(Deployment::deploymentOptions)
                 .forEach(opt -> context.verify(i -> {
                     Assert.assertEquals(7, opt.getWorkerPoolSize());
                     Assert.assertTrue(p.matcher(opt.getWorkerPoolName()).matches());
                 }));
        };
        deployThenSucceed(context, app.onCompleted(i -> TestHelper.testComplete(async))
                                      .addProvider(new DummyPluginProvider())
                                      .addProvider(new MockPluginProvider()),
                          new DeploymentOptions().setWorkerPoolSize(15), asserter);
    }

    @Test
    public void test_use_deployment_option_from_plugin(TestContext context) {
        Async async = context.async(2);
        final String deployKey = PluginConfig.PLUGIN_DEPLOY_CONFIG_KEY + MockPluginConfig.MOCK_CFG_KEY;
        QWEAppConfig config = new QWEAppConfig().put(deployKey, new DeploymentOptions().setHa(true)
                                                                                       .setWorkerPoolSize(3)
                                                                                       .setWorkerPoolName("test")
                                                                                       .toJson());
        Consumer<String> asserter = deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(2, vertx.deploymentIDs().size());
            vertx.deploymentIDs()
                 .stream()
                 .map(id -> ((VertxImpl) vertx).getDeployment(id))
                 .filter(Deployment::isChild)
                 .map(Deployment::deploymentOptions)
                 .forEach(opt -> context.verify(i -> {
                     Assert.assertTrue(opt.isHa());
                     Assert.assertEquals(3, opt.getWorkerPoolSize());
                     Assert.assertEquals("test", opt.getWorkerPoolName());
                 }));
        };
        deployThenSucceed(context,
                          app.onCompleted(i -> TestHelper.testComplete(async)).addProvider(new DummyPluginProvider()),
                          new DeploymentOptions().setConfig(config.toJson()), asserter);
    }

    @Test
    public void test_contain_two_plugin_vertical_having_same_type_should_deploy_only_one(TestContext context) {
        Async async = context.async(2);
        deployThenSucceed(context, app.onCompleted(i -> TestHelper.testComplete(async))
                                      .addProvider(new DummyPluginProvider())
                                      .addProvider(new DummyPluginProvider()), null, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(2, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_contain_two_plugin_vertical_having_different_type_should_deploy_both(TestContext context) {
        Async async = context.async(2);
        deployThenSucceed(context, app.onCompleted(i -> TestHelper.testComplete(async))
                                      .addProvider(new DummyPluginProvider())
                                      .addProvider(new MockPluginProvider()), null, deployId -> {
            TestHelper.testComplete(async);
            Assert.assertEquals(3, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_cannot_start_coz_throw_exception_onStart(TestContext context) {
        deployThenFailed(context, app.errorOnStart(true).addProvider(new DummyPluginProvider()),
                         new QWEException("UNKNOWN_ERROR | Cause: Error when starting"));
    }

    @Test
    public void test_cannot_start_coz_install_plugin_that_throw_exception(TestContext context) {
        deployThenFailed(context, app.addProvider(new DummyPluginProvider()).addProvider(new MockPluginProvider(true)),
                         new QWEException("UNKNOWN_ERROR | Cause: Error when starting Plugin[mock]"));
    }

    @Test
    public void test_start_but_throw_exception_onInstallCompleted(TestContext context) {
        Async async = context.async();
        deployThenSucceed(context, app.errorOnCompleted(true).addProvider(new DummyPluginProvider()), null,
                          deployId -> {
                              TestHelper.testComplete(async);
                              Assert.assertEquals(3, vertx.deploymentIDs().size());
                          });
    }

    @Test
    public void test_add_extension_success(TestContext context) {
        Async async = context.async(2);
        Handler<ApplicationContextHolder> v = holder -> {
            context.verify(i -> {
                Assert.assertNotNull(holder.getExtension(MockExtension.class));
                Assert.assertEquals(MockExtension.class, holder.getExtension(MockExtension.class).getClass());
                Assert.assertEquals(1, holder.extensions().size());
            });
            TestHelper.testComplete(async);
        };
        deployThenSucceed(context, app.onCompleted(v).addExtension(MockExtension.class), null,
                          i -> TestHelper.testComplete(async));
    }

    @Test
    public void test_add_duplicate_extension_success(TestContext context) {
        Async async = context.async(2);
        Handler<ApplicationContextHolder> v = holder -> {
            context.verify(i -> {
                Assert.assertNotNull(holder.getExtension(MockExtension.class));
                Assert.assertEquals(MockExtension.class, holder.getExtension(MockExtension.class).getClass());
                Assert.assertEquals(1, holder.extensions().size());
            });
            TestHelper.testComplete(async);
        };
        deployThenSucceed(context,
                          app.onCompleted(v).addExtension(MockExtension.class).addExtension(MockExtension.class), null,
                          i -> TestHelper.testComplete(async));
    }

    @Test
    public void test_add_extension_but_error_in_setup(TestContext context) {
        deployThenFailed(context, app.addExtension(MockErrorExtension.class), new IllegalArgumentException("xxx"));
    }

    private void deployThenSucceed(TestContext context, Application application, DeploymentOptions options,
                                   Consumer<String> asserter) {
        VertxHelper.deploy(vertx, context, DeployContext.builder()
                                                        .verticle(application)
                                                        .options(options)
                                                        .successAsserter(asserter)
                                                        .build());
    }

    private void deployThenFailed(TestContext context, Application application, Throwable error) {
        Async async = context.async();
        VertxHelper.deploy(vertx, context, DeployContext.builder().verticle(application).failedAsserter(t -> {
            try {
                Assert.assertTrue(error.getClass().isInstance(t));
                Assert.assertEquals(error.getMessage(), t.getMessage());
                Assert.assertEquals(0, vertx.deploymentIDs().size());
            } finally {
                TestHelper.testComplete(async);
            }
        }).build());
    }

}
