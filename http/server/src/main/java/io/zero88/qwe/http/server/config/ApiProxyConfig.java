package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpSystem.ApisSystem;
import io.zero88.qwe.http.server.RouterConfig;

public final class ApiProxyConfig extends AbstractRouterConfig implements RouterConfig, ApisSystem {

    public static final String NAME = "__proxy__";

    public ApiProxyConfig() {
        super(NAME, ApiConfig.class);
    }

    @Override
    protected String defaultPath() {
        return "/s";
    }

}
