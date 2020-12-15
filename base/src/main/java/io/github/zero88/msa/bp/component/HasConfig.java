package io.github.zero88.msa.bp.component;

import io.github.zero88.msa.bp.IConfig;
import io.github.zero88.msa.bp.utils.Configs;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

/**
 * Mark a component that having Config
 *
 * @param <C> type of {@code IConfig}
 * @see IConfig
 */
interface HasConfig<C extends IConfig> {

    /**
     * Config class
     *
     * @return IConfig class
     */
    @NonNull Class<C> configClass();

    /**
     * Define a config file in classpath
     *
     * @return config file path
     */
    @NonNull String configFile();

    /**
     * Compute configure based on user input configuration and default unit configuration that defined in {@link
     * #configFile()}
     *
     * @param config given user configuration
     * @return config instance
     * @see IConfig
     */
    default C computeConfig(JsonObject config) {
        return IConfig.merge(IConfig.from(Configs.loadJsonConfig(configFile()), configClass()), config, configClass());
    }

}