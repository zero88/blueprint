package io.zero88.qwe.http.server;

import java.util.Set;

import io.zero88.qwe.http.server.rest.api.RestApi;
import io.zero88.qwe.http.server.rest.api.RestEventApi;
import io.zero88.qwe.http.server.ws.WebSocketServerPlan;

public interface HttpRuntimeConfig {

    Set<Class<? extends RestApi>> getRestApiClasses();

    Set<Class<? extends RestEventApi>> getRestEventApiClasses();

    Set<WebSocketServerPlan> getWebSocketEvents();

    Class<? extends RestEventApi> getGatewayApiClass();

}