package io.zero88.qwe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.zero88.utils.FileUtils;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.IOtherConfig.HasOtherConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * An {@code Application} configuration contains itself configuration and its {@code plugin} or {@code extension} configurations
 *
 * @see Application
 * @see PluginConfig
 * @see ExtensionConfig
 */
public final class QWEAppConfig extends HasOtherConfig<QWEAppConfig> implements IConfig {

    public static final String DELIVERY_OPTIONS = "__delivery__";

    public static final String DATA_DIR = "dataDir";
    public static final Path DEFAULT_DATADIR = FileUtils.defaultDatadir(".playio");
    /**
     * Application data dir
     */
    @Getter
    @Accessors(fluent = true)
    @JsonProperty(value = DATA_DIR)
    private final Path dataDir;
    /**
     * EventBus delivery options
     */
    @Getter
    @JsonIgnore
    private final DeliveryOptions deliveryOptions;

    public QWEAppConfig() {
        this.dataDir = DEFAULT_DATADIR;
        this.deliveryOptions = new DeliveryOptions();
    }

    @JsonCreator
    public QWEAppConfig(Map<String, Object> map) {
        Map<String, Object> m = Optional.ofNullable(map).orElseGet(HashMap::new);
        this.dataDir = Optional.ofNullable(m.remove(QWEAppConfig.DATA_DIR))
                               .map(o -> Paths.get(o.toString()))
                               .orElse(DEFAULT_DATADIR);
        this.deliveryOptions = Optional.ofNullable(m.remove(QWEAppConfig.DELIVERY_OPTIONS))
                                       .map(o -> new DeliveryOptions(JsonObject.mapFrom(o)))
                                       .orElseGet(DeliveryOptions::new);
        this.putAll(m);
    }

    @Override
    public String configKey() {return QWEConfig.APP_CONF_KEY;}

    @Override
    public Class<? extends IConfig> parent() {return QWEConfig.class;}

    /**
     * Application data dir
     */
    public String getDataDir() {return dataDir().toAbsolutePath().toString();}

    /**
     * Other {@code Application} configurations or {@code Plugin} configuration
     *
     * @see PluginConfig
     */
    @Override
    public JsonObject other() {
        return super.other();
    }

    @Override
    public JsonObject toJson(ObjectMapper mapper) {
        return super.toJson(mapper).put(DELIVERY_OPTIONS, this.deliveryOptions.toJson());
    }

}
