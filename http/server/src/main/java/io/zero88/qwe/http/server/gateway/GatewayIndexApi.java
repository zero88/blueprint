package io.zero88.qwe.http.server.gateway;

import java.util.Collections;
import java.util.Set;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.http.ActionMethodMapping;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.http.server.config.ApiGatewayConfig;

import lombok.Getter;
import lombok.experimental.Accessors;

public final class GatewayIndexApi implements GatewayApi {

    @Getter
    @Accessors(fluent = true)
    private String address;

    @Override
    public GatewayIndexApi setup(ApiGatewayConfig config, SharedDataLocalProxy sharedData) {
        this.address = config.getAddress();
        return this;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.create("/index", "/:identifier", ActionMethodMapping.DQL_MAP));
    }

}
