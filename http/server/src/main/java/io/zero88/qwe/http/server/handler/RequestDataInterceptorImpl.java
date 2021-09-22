package io.zero88.qwe.http.server.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.http.HttpUtils.HttpHeaderUtils;
import io.zero88.qwe.http.HttpUtils.HttpRequestUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestDataInterceptorImpl implements RequestDataInterceptor {

    private final boolean useRequestData;
    private final boolean useForUpload;
    private BiFunction<RoutingContext, RequestData, RequestData> andThen;

    @Override
    public RequestDataInterceptor andThen(BiFunction<RoutingContext, RequestData, RequestData> andThen) {
        this.andThen = andThen;
        return this;
    }

    @Override
    public Future<RequestData> filter(RoutingContext ctx) {
        return Future.succeededFuture(extract(ctx))
                     .map(req -> Optional.ofNullable(andThen).map(at -> at.apply(ctx, req)).orElse(req));
    }

    private RequestData extract(RoutingContext ctx) {
        if (useForUpload) {
            return RequestData.builder()
                              .body(HttpHeaderUtils.serializeHeaders(ctx.request().formAttributes()))
                              .headers(HttpHeaderUtils.serializeHeaders(ctx.request()))
                              .build();
        }
        return useRequestData ? convert(ctx) : RequestData.builder().body(body(ctx)).build();
    }

    private RequestData convert(@NonNull RoutingContext context) {
        return RequestData.builder()
                          .headers(HttpHeaderUtils.serializeHeaders(context.request()))
                          .body(body(context))
                          .sort(HttpRequestUtils.sort(context.request()))
                          .filter(HttpRequestUtils.query(context.request()))
                          .pagination(HttpRequestUtils.pagination(context.request()))
                          .build();
    }

    private JsonObject body(@NonNull RoutingContext context) {
        //TODO re-check vertx-4 has path params `*`
        final Map<String, String> obj = context.pathParams()
                                               .entrySet()
                                               .stream()
                                               .filter(entry -> !entry.getKey().equals("*"))
                                               .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        final JsonObject params = JsonObject.mapFrom(obj);
        final JsonObject body = Optional.ofNullable(context.getBody())
                                        .map(b -> JsonData.tryParse(b).toJson())
                                        .orElseGet(JsonObject::new);
        return params.mergeIn(body, true);
    }

}
