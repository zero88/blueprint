package io.zero88.qwe.http.server.handler;

import java.util.Objects;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.http.HttpStatusMapping;
import io.zero88.qwe.http.HttpStatusMappingLoader;
import io.zero88.qwe.http.HttpUtils;

/**
 * Rest response end handler for {@code eventbus}
 *
 * @see EventMessage
 */
public final class ResponseEventInterceptor implements ResponseInterceptor<EventMessage> {

    @Override
    public void response(RoutingContext ctx, EventMessage resp) {
        if (Objects.isNull(resp)) {
            ctx.next();
            return;
        }
        ctx.addHeadersEndHandler(
            v -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpUtils.JSON_UTF8_CONTENT_TYPE));
        HttpStatusMapping mapping = HttpStatusMappingLoader.getInstance().get();
        if (resp.isSuccess()) {
            ctx.response()
               .setStatusCode(mapping.success(ctx.request().method()).code())
               .end(HttpUtils.prettify(ctx.request(), resp.getData()));
        } else {
            ctx.response()
               .setStatusCode(mapping.error(ctx.request().method(), resp.getError().getCode()).code())
               .end(HttpUtils.prettify(ctx.request(), resp.getError()));
        }
    }

}
