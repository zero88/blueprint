package io.zero88.qwe.sql;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jooq.SQLDialect;

import io.vertx.core.json.JsonObject;
import io.zero88.jooqx.spi.DBEmbeddedMode;
import io.zero88.qwe.PluginConfig.DynamicPluginConfig.DynamicPluginConfigImpl;
import io.zero88.qwe.PluginConfig.PluginDirConfig;
import io.zero88.qwe.sql.handler.JooqxBaseExtension;
import io.zero88.qwe.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public final class SQLPluginConfig extends DynamicPluginConfigImpl<SQLPluginConfig> implements PluginDirConfig {

    public static final String KEY = "__sql__";
    public static final String SQL_POOL_KEY = "__sql_pool__";
    public static final String SQL_CONN_KEY = "__sql_conn__";

    private String pluginDir;
    private SQLDialect dialect;
    /**
     * @see JooqxBaseExtension
     */
    private String jooqxExtensionClass;
    /**
     * Auto detect SQL lib to find appropriate Jooqx extension
     */
    private boolean autoDetect;
    /**
     * Defines Database embedded mode, use it conjunction with {@link #isAutoDetect()}
     *
     * @see DBEmbeddedMode
     */
    private DBEmbeddedMode embeddedMode;

    public SQLPluginConfig() {this(null);}

    @JsonCreator
    SQLPluginConfig(Map<String, Object> map) {
        super(map);
        this.pluginDir = Optional.ofNullable(remove(PluginDirConfig.PLUGIN_DIR_JSON_KEY))
                                 .map(Object::toString)
                                 .orElse(null);
        this.dialect = Optional.ofNullable(remove("dialect")).map(d -> SQLDialect.valueOf(d.toString())).orElse(null);
        this.jooqxExtensionClass = Optional.ofNullable(remove("jooqxExtensionClass"))
                                           .map(Object::toString)
                                           .orElse(null);
        this.autoDetect = Optional.ofNullable(remove("autoDetect"))
                                  .map(d -> Boolean.valueOf(d.toString()))
                                  .orElse(false);
        this.embeddedMode = Optional.ofNullable(remove("embeddedMode"))
                                    .map(d -> DBEmbeddedMode.valueOf(d.toString()))
                                    .orElse(null);
    }

    @Override
    public String configKey() {
        return KEY;
    }

    public JsonObject connectionOptions() {
        return lookup(SQL_CONN_KEY, JsonObject.class);
    }

    public SQLPluginConfig connectionOptions(JsonObject connectionOptions) {
        return put(SQL_CONN_KEY, connectionOptions);
    }

    public JsonObject poolOptions() {
        return lookup(SQL_POOL_KEY, JsonObject.class);
    }

    public SQLPluginConfig poolOptions(JsonObject poolOptions) {
        return put(SQL_POOL_KEY, poolOptions);
    }

    @Override
    public JsonObject toJson(@NonNull ObjectMapper mapper) {
        JsonObject kv = new JsonObject().put(PluginDirConfig.PLUGIN_DIR_JSON_KEY, pluginDir)
                                        .put("dialect", Objects.isNull(dialect) ? null : dialect.name())
                                        .put("jooqxExtensionClass", jooqxExtensionClass)
                                        .put("autoDetect", autoDetect)
                                        .put("embeddedMode", embeddedMode);
        return JsonUtils.putIfNotNull(super.toJson(mapper), kv.getMap());
    }

    public SQLPluginConfig validate() {
        Objects.requireNonNull(getDialect(), "Missing SQL dialect");
        return this;
    }

}
