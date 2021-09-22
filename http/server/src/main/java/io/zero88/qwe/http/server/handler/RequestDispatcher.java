package io.zero88.qwe.http.server.handler;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.auth.UserInfo;

/**
 * Represents for the HTTP request handler that dispatches an incoming request to the appropriate backend
 *
 * @param <I> Type of request
 * @param <R> Type of response
 */
public interface RequestDispatcher<I, R> extends Handler<RoutingContext> {

    RequestInterceptor<I> validator();

    AuthInterceptor authInterceptor();

    ResponseInterceptor<R> responseInterceptor();

    Future<R> proceed(RoutingContext context, UserInfo userInfo, I reqData);

    @Override
    default void handle(RoutingContext context) {
        authInterceptor().filter(context)
                         .flatMap(u -> validator().filter(context).flatMap(d -> proceed(context, u, d)))
                         .onSuccess(r -> responseInterceptor().response(context, r))
                         .onFailure(context::fail);
    }

}
