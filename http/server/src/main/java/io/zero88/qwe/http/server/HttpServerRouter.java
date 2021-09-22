package io.zero88.qwe.http.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.zero88.qwe.http.server.gateway.GatewayApi;
import io.zero88.qwe.http.server.gateway.GatewayIndexApi;
import io.zero88.qwe.http.server.rest.api.RestApi;
import io.zero88.qwe.http.server.rest.api.RestEventApi;
import io.zero88.qwe.http.server.ws.WebSocketServerPlan;

import lombok.Getter;

//TODO: Use Builder: WebsocketEventBuilder, RestEventApisBuilder
@Getter
public final class HttpServerRouter implements HttpRuntimeConfig {

    private final Set<Class<? extends RestApi>> restApiClasses = new HashSet<>();
    private final Set<Class<? extends RestEventApi>> restEventApiClasses = new HashSet<>();
    private final Set<WebSocketServerPlan> webSocketEvents = new HashSet<>();
    private final Set<Class<? extends GatewayApi>> gatewayApiClasses = new HashSet<>(
        Collections.singleton(GatewayIndexApi.class));
    private RouterBuilder customBuilder = RouterBuilder.NONE;

    @SafeVarargs
    public final HttpServerRouter registerApi(Class<? extends RestApi>... apiClass) {
        restApiClasses.addAll(Arrays.stream(apiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final HttpServerRouter registerEventBusApi(Class<? extends RestEventApi>... eventBusApiClass) {
        restEventApiClasses.addAll(
            Arrays.stream(eventBusApiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final HttpServerRouter registerGatewayApi(Class<? extends GatewayApi>... gatewayApiClass) {
        gatewayApiClasses.addAll(Arrays.stream(gatewayApiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public HttpServerRouter registerEventBusSocket(WebSocketServerPlan... eventBusSocket) {
        webSocketEvents.addAll(Arrays.stream(eventBusSocket).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public HttpServerRouter addCustomBuilder(RouterBuilder routerBuilder) {
        if (routerBuilder != null) {
            this.customBuilder = routerBuilder;
        }
        return this;
    }

}
