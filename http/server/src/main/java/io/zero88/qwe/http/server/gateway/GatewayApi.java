package io.zero88.qwe.http.server.gateway;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.http.server.config.ApiGatewayConfig;
import io.zero88.qwe.http.server.rest.api.IRestEventApi;

public interface GatewayApi extends IRestEventApi<ApiGatewayConfig> {

    @Override
    GatewayApi setup(ApiGatewayConfig config, SharedDataLocalProxy sharedData);

}
