package io.zero88.qwe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.FileUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.PluginConfig.PluginDirConfig;
import io.zero88.qwe.eventbus.EventBusDeliveryOption;
import io.zero88.qwe.exceptions.QWEExceptionConverter;
import io.zero88.qwe.launcher.BootCommand;
import io.zero88.qwe.crypto.CryptoContext;
import io.zero88.qwe.crypto.CryptoHolder;
import io.zero88.qwe.crypto.CryptoHolderProviderLoader;
import io.zero88.qwe.utils.NetworkUtils;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @see Application
 */
@SuppressWarnings({"rawtypes"})
public abstract class ApplicationVerticle extends AbstractVerticle
    implements Application, SharedDataLocalProxy, VerticleLifecycleHooks {

    private final Map<Class<? extends Plugin>, PluginProvider<? extends Plugin>> pluginProviders = new HashMap<>();
    private final Set<Class<? extends Extension>> extensions = new HashSet<>();
    private final ApplicationContextHolderInternal holder = ApplicationContextHolderInternal.create();
    @Getter
    @Accessors(fluent = true)
    private QWEAppConfig appConfig;
    private CryptoHolder cryptoHolder;

    @Override
    public final void start() {
        logger().info("Start Application[{}]...", appName());
        this.appConfig = computeConfig(config());
        this.addData(SharedDataLocalProxy.EVENTBUS_DELIVERY_OPTION_KEY,
                     new EventBusDeliveryOption(appConfig.getDeliveryOptions()));
        this.addData(SharedDataLocalProxy.PUBLIC_IPV4_KEY, NetworkUtils.getPublicIpv4());
        this.onStart();
    }

    @Override
    public final void start(Promise<Void> promise) {
        QWEVerticle.asyncRun(vertx, promise, () -> {
            this.start();
            final Context context = vertx.getOrCreateContext();
            this.cryptoHolder = new CryptoHolderProviderLoader().setup(context.processArgs(),
                                                                       context.get(BootCommand.KEY_STORE_CONFIG));
        }, () -> onAsyncStart().flatMap(ignore -> installExtensions())
                               .flatMap(ignore -> installPlugins())
                               .onSuccess(i -> deployAllSuccess())
                               .onFailure(e -> logError(e, "start"))
                               .mapEmpty());
    }

    public final void stop() {
        this.onStop();
    }

    @Override
    public final void stop(Promise<Void> promise) {
        logger().info("Stop Application[{}]...", appName());
        QWEVerticle.asyncRun(vertx, promise, this::stop, () -> onAsyncStop().eventually(ignore -> uninstallPlugins())
                                                                            .eventually(ignore -> uninstallExtensions())
                                                                            .onFailure(e -> logError(e, "stop")));
    }

    @Override
    public final <T extends Plugin> Application addProvider(PluginProvider<T> provider) {
        pluginProviders.put(provider.pluginClass(), provider);
        if (Objects.nonNull(provider.extensions())) {
            extensions.addAll(provider.extensions());
        }
        return this;
    }

    @Override
    public final <E extends Extension> Application addExtension(Class<E> extensionCls) {
        extensions.add(extensionCls);
        return this;
    }

    @Override
    public final SharedDataLocalProxy sharedData() {
        return this;
    }

    @Override
    public final Future<Void> installPlugins() {
        final int defaultPoolSize = defaultPluginThreadPoolSize(pluginProviders.size());
        return CompositeFuture.all(pluginProviders.values()
                                                  .stream()
                                                  .map(provider -> deployPlugin(provider, defaultPoolSize))
                                                  .collect(Collectors.toList())).onSuccess(r -> {
            logger().info("Deployed [{}] plugin(s)", r.size() - r.causes().stream().filter(Objects::nonNull).count());
            pluginProviders.clear();
        }).mapEmpty();
    }

    @Override
    public final Future<Void> uninstallPlugins() {
        return CompositeFuture.join(holder.plugins().stream().map(this::undeployPlugin).collect(Collectors.toList()))
                              .onSuccess(ar -> logger().info("Uninstalled [{}] plugin(s)", ar.size()))
                              .mapEmpty();
    }

    @Override
    public Future<Void> installExtensions() {
        return vertx.executeBlocking(h -> {
            extensions.stream().map(ReflectionClass::createObject).filter(Objects::nonNull).map(ext -> {
                logger().info("Setting up Extension[{}]...", ext.extName());
                return ext.setup(sharedData(), appName(), appConfig().dataDir(),
                                 appConfig().lookupJson(ext.configKey()), lookupCryptoContext(cryptoHolder, ext));
            }).forEach(holder::addExtension);
            logger().info("Deployed [{}] extensions(s)", extensions.size());
            extensions.clear();
            h.complete();
        });
    }

    @Override
    public Future<Void> uninstallExtensions() {
        return vertx.executeBlocking(h -> {
            holder.extensions().forEach(Extension::stop);
            h.complete();
        });
    }

    @Override
    public void onInstallCompleted(ApplicationContextHolder holder) {}

    Future<Void> undeployPlugin(PluginContext ctx) {
        return vertx.undeploy(ctx.deployId())
                    .onSuccess(unused -> logger().info("Uninstalled Plugin[{}][{}]", ctx.deployId(), ctx.pluginName()));
    }

    Future<Void> deployPlugin(PluginProvider<? extends Plugin> provider, int defaultPoolSize) {
        Plugin plugin = provider.get();
        String pluginName = plugin.pluginName();
        logger().info("Deploying Plugin[{}]...", pluginName);
        JsonObject pluginCfg = appConfig().lookupJson(plugin.configKey());
        Path pluginDir = null;
        if (ReflectionClass.assertDataType(plugin.configClass(), PluginDirConfig.class) && Objects.nonNull(pluginCfg)) {
            pluginDir = Paths.get(FileUtils.createFolder(appConfig().dataDir(),
                                                         pluginCfg.getString(PluginDirConfig.PLUGIN_DIR_JSON_KEY)));
        }
        CryptoContext cr = lookupCryptoContext(cryptoHolder, plugin);
        Set<Extension> extensions = holder.extensions()
                                          .stream()
                                          .filter(s -> provider.extensions().contains(s.getClass()))
                                          .collect(Collectors.toSet());
        return vertx.deployVerticle(plugin.deployHook()
                                          .onPreDeploy(plugin, PluginContext.create(appName(), pluginName, pluginDir,
                                                                                    sharedData(), extensions, cr)),
                                    createPluginDeploymentOptions(plugin, pluginCfg, defaultPoolSize))
                    .onSuccess(id -> logger().info("Setting-up Plugin[{}][{}]...", pluginName, id))
                    .map(id -> plugin.deployHook()
                                     .onPostDeploy(plugin, plugin.pluginContext().deployId(id))
                                     .pluginContext())
                    .onSuccess(holder::addPlugin)
                    .recover(t -> Future.failedFuture(QWEExceptionConverter.friendlyOrKeep(t)))
                    .mapEmpty();
    }

    void deployAllSuccess() {
        try {
            //TODO: add liveness event then readiness event
            this.onInstallCompleted(holder);
        } catch (Exception e) {
            //TODO: add failure readiness event
            logger().warn("Failed after completed plugin deployment", QWEExceptionConverter.friendlyOrKeep(e));
        }
    }

    DeploymentOptions createPluginDeploymentOptions(Plugin plugin, JsonObject pluginConfig, int defaultPoolSize) {
        DeploymentOptions options = Optional.ofNullable(appConfig.lookup(plugin.deploymentKey()))
                                            .map(s -> new DeploymentOptions(JsonObject.mapFrom(s)))
                                            .orElseGet(DeploymentOptions::new);
        int workerPoolSize = options.getWorkerPoolSize() == VertxOptions.DEFAULT_WORKER_POOL_SIZE
                             ? defaultPoolSize
                             : options.getWorkerPoolSize();
        String workerPool = Optional.ofNullable(options.getWorkerPoolName())
                                    .orElseGet(() -> generatePluginThreadName(plugin.pluginName()));
        options.setWorkerPoolName(workerPool).setWorkerPoolSize(workerPoolSize).setConfig(pluginConfig);
        if (logger().isDebugEnabled()) {
            logger().debug("Plugin deployment options [{}][{}]", plugin.pluginName(), options.toJson());
        }
        return options;
    }

    private void logError(Throwable e, String action) {
        logger().error("Failed to {} Application[{}]", action, appName(), e);
    }

}
