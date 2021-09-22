package io.zero88.qwe.http.server.handler;

import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.dto.msg.ResponseData;
import io.zero88.qwe.http.HttpStatusMappingLoader;
import io.zero88.qwe.http.HttpUtils;

public final class ResponseDataInterceptor implements ResponseInterceptor<ResponseData> {

    @Override
    public void response(RoutingContext ctx, ResponseData resp) {
        ctx.response()
           .setStatusCode(HttpStatusMappingLoader.getInstance().get().success(ctx.request().method()).code())
           .end(HttpUtils.prettify(ctx.request(), resp.body()));
    }

}
