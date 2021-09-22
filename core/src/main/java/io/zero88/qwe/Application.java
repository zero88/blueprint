package io.zero88.qwe;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxImpl;
import io.zero88.qwe.launcher.VersionCommand;
import io.zero88.qwe.crypto.CryptoContext;
import io.zero88.qwe.crypto.CryptoHolder;

import lombok.NonNull;

/**
 * Represents an {@code Application} contains a list of micro {@code plugins} function.
 * <p>
 * It's mostly deployed as a standalone application by {@code java -jar Application.jar}
 *
 * @see Plugin
 * @see HasConfig
 * @see ApplicationVerticle
 */
@SuppressWarnings("rawtypes")
public interface Application extends QWEVerticle<QWEAppConfig>, HasAppName, HasSharedKey {

    static String generateThreadName(Class<? extends Application> cls, String appName, String pluginName) {
        if (cls.getName().equals(appName)) {
            return cls.getSimpleName() + "-plugin-" + pluginName + "-thread";
        }
        return appName + "-plugin-" + pluginName + "-thread";
    }

    /**
     * Application name
     *
     * @return an application name
     * @apiNote To better identify, {@code Application Name} convention is {@code kebab-case} or {@code snake_case}.
     *     Should avoid {@code space} in name
     */
    default String appName() {
        return Optional.ofNullable(VersionCommand.getVersionOrNull())
                       .flatMap(v -> Optional.ofNullable(v.getName()))
                       .orElse(getClass().getName());
    }

    QWEAppConfig appConfig();

    @Override
    default Class<QWEAppConfig> configClass() {
        return QWEAppConfig.class;
    }

    @Override
    default String configFile() {
        return "app.json";
    }

    default String sharedKey() {
        return getClass().getName();
    }

    default String generatePluginThreadName(String pluginName) {
        return generateThreadName(getClass(), appName(), pluginName);
    }

    /**
     * Add plugin provider to startup
     *
     * @param <P>      Type of plugin
     * @param provider Unit provider
     * @return a reference to this, so the API can be used fluently
     */
    <P extends Plugin> Application addProvider(PluginProvider<P> provider);

    /**
     * Add extension class to startup
     *
     * @param extensionCls extension class
     * @param <E>          Type of extension
     * @return a reference to this, so the API can be used fluently
     */
    <E extends Extension> Application addExtension(Class<E> extensionCls);

    /**
     * Compute default plugin pool size
     *
     * @param nbOfPlugins Number of plugins will be installed under this {@code Application}
     * @return default plugin pool size
     */
    default int defaultPluginThreadPoolSize(int nbOfPlugins) {
        int poolSize = VertxOptions.DEFAULT_WORKER_POOL_SIZE;
        if (getVertx() instanceof VertxImpl) {
            poolSize = ((VertxImpl) getVertx()).getOrCreateContext()
                                               .getDeployment()
                                               .deploymentOptions()
                                               .getWorkerPoolSize();
        }
        return Math.max(poolSize / Math.max(nbOfPlugins, 2), 1);
    }

    /**
     * Install the registered {@code plugins} in {@code dedicated thread group} based on the given providers from {@link
     * #addProvider(PluginProvider)}
     * <p>
     * If any {@code plugin} verticle starts failed, {@code application} will be failed to start as well
     *
     * @return void future
     * @apiNote You can override {@code DeploymentOptions} per {@code plugin} by declared options with key {@link
     *     Plugin#deploymentKey()} under {@link QWEAppConfig}
     */
    Future<Void> installPlugins();

    /**
     * Uninstall a list of registered {@code plugins} when {@code application} is stopped
     *
     * @return void future
     */
    Future<Void> uninstallPlugins();

    /**
     * Install the registered {@code extensions} based on the given providers from {@link #addExtension(Class)}
     *
     * @return void future
     */
    Future<Void> installExtensions();

    /**
     * Uninstall a list of registered {@code extensions} when {@code application} is stopped
     *
     * @return void future
     */
    Future<Void> uninstallExtensions();

    /**
     * Raise event after all plugins/extensions are installed completely
     *
     * @param holder Context holder
     */
    void onInstallCompleted(@NonNull ApplicationContextHolder holder);

    /**
     * Lookup a cryptographic context per extension
     *
     * @param cryptoHolder a cryptographic holder
     * @param extension    an extension
     * @return a crypto context that associates to the given extension
     */
    default @NotNull CryptoContext lookupCryptoContext(@NotNull CryptoHolder cryptoHolder,
                                                       @NotNull Extension extension) {
        return cryptoHolder.lookup(extension);
    }

    /**
     * Lookup a cryptographic context per extension
     *
     * @param cryptoHolder a cryptographic holder
     * @param plugin       a plugin
     * @return a crypto context that associates to the given plugin
     */
    default @NotNull CryptoContext lookupCryptoContext(@NotNull CryptoHolder cryptoHolder, @NotNull Plugin plugin) {
        return cryptoHolder.lookup(plugin);
    }

}
