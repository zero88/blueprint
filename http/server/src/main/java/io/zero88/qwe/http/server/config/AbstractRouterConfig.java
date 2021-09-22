package io.zero88.qwe.http.server.config;

import java.util.Objects;

import io.github.zero88.exceptions.InvalidUrlException;
import io.github.zero88.utils.Urls;
import io.zero88.qwe.IConfig;
import io.zero88.qwe.http.server.HttpSystem;
import io.zero88.qwe.http.server.RouterConfig;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public abstract class AbstractRouterConfig implements IConfig, RouterConfig, HttpSystem {

    @Accessors(fluent = true)
    private final String configKey;
    @Accessors(fluent = true)
    private final Class<? extends IConfig> parent;
    @Accessors(chain = true)
    private boolean enabled;
    @Accessors(chain = true)
    private String path;

    protected abstract @NonNull String defaultPath();

    protected AbstractRouterConfig(String configKey, Class<? extends IConfig> parent) {
        this(configKey, parent, false, null);
    }

    protected AbstractRouterConfig(String configKey, Class<? extends IConfig> parent, boolean enabled, String path) {
        this.configKey = configKey;
        this.parent = parent;
        this.enabled = enabled;
        this.path = Urls.combinePath(Objects.toString(path, defaultPath()));
        if (!Urls.validatePath(this.path)) {
            throw new InvalidUrlException(function() + "[" + this.path + "] is not valid");
        }
    }

}
