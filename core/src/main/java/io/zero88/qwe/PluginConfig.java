package io.zero88.qwe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@code Plugin} configuration
 *
 * @see Plugin
 */
public interface PluginConfig extends IConfig {

    String PLUGIN_DEPLOY_CONFIG_KEY = "__deployment__";

    @Override
    default Class<? extends IConfig> parent() {
        return QWEAppConfig.class;
    }

    /**
     * If a {@code Plugin} need to use local data storage then use this interface for its configuration.
     *
     * @see PluginConfig
     */
    interface PluginDirConfig extends PluginConfig {

        String PLUGIN_DIR_JSON_KEY = "pluginDir";

        /**
         * Defines plugin dir configuration
         * <p>
         * If a relative path, then the actual plugin path will be child folder of {@code application data dir}.
         *
         * @return plugin dir
         */
        @JsonProperty(PLUGIN_DIR_JSON_KEY)
        @Nullable String getPluginDir();

    }


    /**
     * If a {@code Plugin} defines a domain configuration that contains many configuration part.
     *
     * @param <C> Type of DynamicPluginConfig
     * @see IOtherConfig
     * @see PluginConfig
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    interface DynamicPluginConfig<C extends DynamicPluginConfig> extends PluginConfig, IOtherConfig<C> {

        /**
         * Find correct plugin config map
         *
         * @param map json data
         * @return plugin config
         */
        default Map<String, Object> find(Map<String, Object> map) {
            if (map.containsKey(QWEConfig.APP_CONF_KEY)) {
                return find((Map<String, Object>) map.get(QWEConfig.APP_CONF_KEY));
            }
            if (map.containsKey(configKey())) {
                return (Map<String, Object>) map.get(configKey());
            }
            return map;
        }

        abstract class DynamicPluginConfigImpl<C extends DynamicPluginConfig> extends HasOtherConfig<C>
            implements DynamicPluginConfig<C> {

            public DynamicPluginConfigImpl(Map<String, Object> other) {
                super();
                this.putAll(find(Optional.ofNullable(other).orElseGet(HashMap::new)));
            }

        }

    }

}
