package io.zero88.qwe.http.server.handler;

import java.util.function.BiFunction;

import io.github.zero88.utils.Strings;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.http.HttpUtils.HttpRequestUtils;

import lombok.NonNull;

public interface RequestDataInterceptor extends RequestInterceptor<RequestData> {

    /**
     * Create the request data interceptor
     *
     * @return an interceptor instance
     */
    static RequestDataInterceptor create() {
        return new RequestDataInterceptorImpl(true, false);
    }

    /**
     * Create the request data interceptor without using HTTP headers and HTTP queries
     *
     * @return an interceptor instance
     */
    static RequestDataInterceptor createSlim() {
        return new RequestDataInterceptorImpl(false, false);
    }

    /**
     * Create the request data interceptor for upload purpose
     *
     * @return an interceptor instance
     */
    static RequestDataInterceptor createForUpload() {
        return new RequestDataInterceptorImpl(false, true);
    }

    RequestDataInterceptor andThen(BiFunction<RoutingContext, RequestData, RequestData> andThen);

    static RequestData convert(@NonNull ServerWebSocket context) {
        return RequestData.builder()
                          .filter(Strings.isNotBlank(context.query())
                                  ? HttpRequestUtils.deserializeQuery(context.query())
                                  : new JsonObject())
                          .build();
    }

}
