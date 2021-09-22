package io.zero88.qwe.http.server.gateway;

import java.util.function.Function;

import io.zero88.qwe.http.server.HttpRuntimeConfig;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.config.ApiGatewayConfig;
import io.zero88.qwe.http.server.rest.RestEventApisCreatorImpl;

public final class GatewayRouterCreator extends RestEventApisCreatorImpl<GatewayApi, ApiGatewayConfig> {

    @Override
    public String function() {
        return "GatewayAPI";
    }

    @Override
    public Function<HttpServerConfig, ApiGatewayConfig> lookupConfig() {
        return HttpServerConfig::getApiGatewayConfig;
    }

    @Override
    protected String subFunction() {
        return "GatewayAPI";
    }

    @Override
    protected void register(HttpRuntimeConfig runtimeConfig) {
        register(runtimeConfig.getGatewayApiClasses());
    }

}
