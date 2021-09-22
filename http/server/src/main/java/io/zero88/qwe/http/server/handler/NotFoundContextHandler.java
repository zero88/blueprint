package io.zero88.qwe.http.server.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.http.HttpUtils;

public final class NotFoundContextHandler implements ResponseErrorHandler {

    @Override
    public void handle(RoutingContext event) {
        HttpServerRequest request = event.request();
        JsonObject result = new JsonObject().put("uri", request.absoluteURI()).put("message", "Resource not found");
        event.response()
             .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
             .putHeader(HttpHeaders.CONTENT_TYPE, HttpUtils.JSON_UTF8_CONTENT_TYPE)
             .end(HttpUtils.prettify(request, result));
    }

}
