package io.zero88.qwe.http.server.handler;

import io.vertx.ext.web.RoutingContext;

/**
 * A response interceptor is attached to the request dispatcher, and handle the dispatcher result then response to
 * client side.
 *
 * @param <T> Type of response
 */
public interface ResponseInterceptor<T> {

    void response(RoutingContext ctx, T resp);

}
