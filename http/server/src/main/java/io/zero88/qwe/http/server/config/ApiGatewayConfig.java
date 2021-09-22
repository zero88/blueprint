package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.GatewaySystem;
import io.zero88.qwe.http.server.RouterConfig;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class ApiGatewayConfig extends AbstractRouterConfig implements RouterConfig, GatewaySystem {

    public static final String NAME = "__api_gateway__";

    private String address;

    public ApiGatewayConfig() {
        super(NAME, HttpServerConfig.class);
    }

    @Override
    protected @NonNull String defaultPath() {
        return "/gw";
    }

}
