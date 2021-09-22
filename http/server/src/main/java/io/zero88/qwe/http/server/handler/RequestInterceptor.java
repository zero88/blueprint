package io.zero88.qwe.http.server.handler;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public interface RequestInterceptor<T> {

    RequestInterceptor<RoutingContext> IDENTITY = Future::succeededFuture;

    Future<T> filter(RoutingContext ctx);

}
