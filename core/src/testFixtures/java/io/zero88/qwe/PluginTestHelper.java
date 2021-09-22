package io.zero88.qwe;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.reactivex.annotations.NonNull;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.crypto.CryptoContext;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface PluginTestHelper extends AppContextTest {

    @Override
    default String appName() {
        return "PluginTest";
    }

    default <T extends Plugin> T deploy(Vertx vertx, VertxTestContext context, PluginConfig config,
                                        PluginProvider<T> provider) {
        return deploy(vertx, context, config, provider, null);
    }

    default <T extends Plugin> T deploy(Vertx vertx, VertxTestContext context, PluginConfig config,
                                        PluginProvider<T> provider,
                                        Map<Class<? extends Extension>, ExtensionConfig> extConfigMap) {
        T plugin = provider.get();
        SharedDataLocalProxy sharedData = createSharedData(vertx);
        Set<Extension> extensions = getExtensions(provider, sharedData, appName(), testDir(), extConfigMap);
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJson());
        return VertxHelper.deploy(vertx, context, DeployContext.<T>builder()
                                                               .verticle(preDeploy(sharedData, plugin, extensions))
                                                               .options(options)
                                                               .successAsserter(id -> {
                                                                   postDeploy(plugin, id);
                                                                   context.completeNow();
                                                               })
                                                               .build());
    }

    default <T extends Plugin> void deployFailed(Vertx vertx, VertxTestContext context, PluginConfig config,
                                                 PluginProvider<T> provider, Consumer<Throwable> handler) {
        VertxHelper.deploy(vertx, context, DeployContext.<T>builder()
                                                        .verticle(provider.get())
                                                        .options(new DeploymentOptions().setConfig(config.toJson()))
                                                        .failedAsserter(handler)
                                                        .build());
    }

    default <T extends Plugin> T deploy(Vertx vertx, TestContext context, PluginConfig config,
                                        PluginProvider<T> provider) {
        T plugin = provider.get();
        SharedDataLocalProxy sharedData = createSharedData(vertx);
        Set<Extension> extensions = getExtensions(provider, sharedData, appName(), testDir(), null);
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJson());
        return VertxHelper.deploy(vertx, context, DeployContext.<T>builder()
                                                               .verticle(preDeploy(sharedData, plugin, extensions))
                                                               .options(options)
                                                               .successAsserter(id -> postDeploy(plugin, id))
                                                               .build());
    }

    default <T extends Plugin> void deployFailed(Vertx vertx, TestContext context, PluginConfig config,
                                                 PluginProvider<T> provider, Consumer<Throwable> handler) {
        VertxHelper.deploy(vertx, context, DeployContext.<T>builder()
                                                        .verticle(provider.get())
                                                        .options(new DeploymentOptions().setConfig(config.toJson()))
                                                        .failedAsserter(handler)
                                                        .build());
    }

    default <T extends Plugin> T preDeploy(SharedDataLocalProxy sharedData, T plugin, Set<Extension> extensions) {
        PluginContext ctx = PluginContext.create(appName(), plugin.pluginName(), testDir(), sharedData, extensions,
                                                 pluginCryptoContext());
        return (T) plugin.deployHook().onPreDeploy(plugin, ctx);
    }

    default <T extends Plugin> T postDeploy(T plugin, String deploymentId) {
        return (T) plugin.deployHook().onPostDeploy(plugin, plugin.pluginContext().deployId(deploymentId));
    }

    default CryptoContext pluginCryptoContext() {
        return CryptoContext.empty();
    }

    @NonNull
    static <T extends Plugin> Set<Extension> getExtensions(PluginProvider<T> provider, SharedDataLocalProxy sharedData,
                                                           String appName, Path appDir,
                                                           Map<Class<? extends Extension>, ExtensionConfig> configMap) {
        Map<Class<? extends Extension>, ExtensionConfig> c = configMap == null ? Collections.emptyMap() : configMap;
        return provider.extensions()
                       .stream()
                       .map(cls -> ExtensionTestHelper.createExt(sharedData, appName, appDir, cls, c.get(cls)))
                       .collect(Collectors.toSet());
    }

    interface PluginDeployTest<T extends Plugin> extends PluginTestHelper {

        PluginConfig initConfig();

        PluginProvider<T> initProvider();

    }

}
