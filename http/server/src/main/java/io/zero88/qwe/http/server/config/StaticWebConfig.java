package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.WebSystem;
import io.zero88.qwe.http.server.RouterConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
public final class StaticWebConfig extends AbstractRouterConfig implements RouterConfig, WebSystem {

    public static final String NAME = "__static__";
    private boolean inResource = true;
    private String webRoot = "webroot";

    public StaticWebConfig() {
        super(NAME, HttpServerConfig.class);
    }

    @Override
    protected @NonNull String defaultPath() {
        return "/web";
    }

}
