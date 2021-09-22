package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.ApisSystem;
import io.zero88.qwe.http.server.RouterConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ApiConfig extends AbstractRouterConfig implements RouterConfig, ApisSystem {

    public static final String NAME = "__api__";

    @JsonProperty(value = ApiProxyConfig.NAME)
    private ApiProxyConfig proxyConfig = new ApiProxyConfig();

    public ApiConfig() {
        super(NAME, HttpServerConfig.class);
        this.setEnabled(true);
    }

    @Override
    protected String defaultPath() {
        return "/api";
    }

}
