package io.zero88.qwe;

import io.zero88.qwe.crypto.CryptoRequest;

import lombok.NonNull;

/**
 * Represents for a {@code Plugin} that keeps a single duty function.
 *
 * @param <C> Type of Plugin Config
 * @param <T> Type of Plugin Context
 * @see PluginConfig
 * @see PluginContext
 * @see PluginVerticle
 */
public interface Plugin<C extends PluginConfig, T extends PluginContext>
    extends QWEVerticle<C>, HasPluginName, HasConfigKey, CryptoRequest {

    /**
     * Expresses a functional that this plugin brings to. For example: {@code http-server}, {@code sql-mysql}
     *
     * @return the plugin function name
     * @apiNote To better identify, {@code plugin name} convention is {@code kebab-case}
     */
    @Override
    default String pluginName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Defines a particular deployment options for plugin.
     * <p>
     * If a {@code plugin} want to declare its deployment options, the config must be used this key and attach under
     * {@link QWEAppConfig}
     *
     * @return a deployment key
     */
    default String deploymentKey() {
        return PluginConfig.PLUGIN_DEPLOY_CONFIG_KEY + configKey();
    }

    /**
     * Plugin config
     *
     * @return a plugin config
     */
    C pluginConfig();

    /**
     * Plugin context
     *
     * @return a plugin context
     */
    @NonNull T pluginContext();

    /**
     * Declares a deployment hook that helps {@code Application} inject a general context information into {@code
     * Plugin}
     *
     * @return a deployment hook
     * @see PluginDeployHook
     * @see Application#installPlugins()
     */
    @NonNull PluginDeployHook deployHook();

    /**
     * Register a pre-plugin-context before installing {@code Plugin} and a post-plugin-context after installed {@code
     * Plugin}
     * <p>
     * This method will be called 2 times automatically in application deployment workflow that thanks to {@link
     * #deployHook()}
     *
     * @param context shared data key
     * @return a reference to this, so the API can be used fluently
     * @see Application
     */
    Plugin<C, T> setup(T context);

    @Override
    default @NonNull SharedDataLocalProxy sharedData() {
        return pluginContext().sharedData();
    }

}
