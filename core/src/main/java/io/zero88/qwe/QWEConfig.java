package io.zero88.qwe;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * A configuration per each {@code Application}
 *
 * @see Application
 */
@Getter
@Setter(value = AccessLevel.PACKAGE)
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public final class QWEConfig implements IConfig {

    public static final String APP_CONF_KEY = "__app__";
    public static final String BOOT_CONF_KEY = "__system__";
    public static final String DEPLOY_CONF_KEY = "__deploy__";

    @JsonProperty(value = BOOT_CONF_KEY)
    private QWEBootConfig bootConfig;
    @Default
    @JsonProperty(value = DEPLOY_CONF_KEY)
    private DeploymentOptions deployConfig = new QWEDeployConfig();
    @Default
    @JsonProperty(value = APP_CONF_KEY)
    private QWEAppConfig appConfig = new QWEAppConfig();

    /**
     * Create default configuration
     *
     * @return QWE configuration
     */
    public static QWEConfig create() {return QWEConfig.create(new JsonObject());}

    static QWEConfig create(@NonNull JsonObject appConfig) {
        return QWEConfig.builder().appConfig(IConfig.from(appConfig, QWEAppConfig.class)).build();
    }

    @Override
    public String configKey() {return null;}

    @Override
    public Class<? extends IConfig> parent() {return null;}

    public static boolean isInstance(@NonNull JsonObject json) {
        return json.containsKey(BOOT_CONF_KEY) || json.containsKey(DEPLOY_CONF_KEY) || json.containsKey(APP_CONF_KEY);
    }

    public static final class QWEDeployConfig extends DeploymentOptions implements IConfig {

        @Override
        public String configKey() {return DEPLOY_CONF_KEY;}

        @Override
        public Class<? extends IConfig> parent() {return QWEConfig.class;}

    }

}
