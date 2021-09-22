package io.zero88.qwe.http.client;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.netty.resolver.dns.DnsNameResolverException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.UpgradeRejectedException;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.http.impl.HttpClientImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.zero88.qwe.ApplicationVersion;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.ResponseData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.exceptions.TimeoutException;
import io.zero88.qwe.http.HttpException;
import io.zero88.qwe.http.HttpUtils.HttpHeaderUtils;
import io.zero88.qwe.http.HttpUtils.HttpRequestUtils;
import io.zero88.qwe.http.client.handler.HttpClientJsonResponseHandler;
import io.zero88.qwe.http.client.handler.WebSocketClientDispatcher;
import io.zero88.qwe.http.client.handler.WebSocketClientErrorHandler;
import io.zero88.qwe.http.client.handler.WebSocketClientPlan;
import io.zero88.qwe.http.client.handler.WebSocketClientWriter;
import io.zero88.qwe.launcher.VersionCommand;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
class HttpClientWrapperImpl implements HttpClientWrapperInternal {

    private final int id;
    private final String userAgent;
    private final Path appDir;
    private final EventBusClient transporter;
    private final HttpClientConfig extConfig;
    private HttpClient client;

    HttpClientWrapperImpl(SharedDataLocalProxy sharedData, String appName, Path appDir, HttpClientConfig config) {
        this.extConfig = config;
        this.id = config.toJson().hashCode();
        this.client = sharedData.getVertx().createHttpClient(config.getOptions());
        this.transporter = EventBusClient.create(sharedData);
        this.appDir = appDir;
        ApplicationVersion version = VersionCommand.getVersionOrFake();
        this.userAgent = String.join("/", appName, version.getVersion() + "-" + version.getHashVersion(),
                                     version.getCoreVersion());
    }

    HttpClientWrapperImpl(HttpClient client, String userAgent) {
        this.extConfig = new HttpClientConfig().setUserAgent(userAgent);
        if (client instanceof HttpClientImpl) {
            extConfig.setOptions(((HttpClientImpl) client).getOptions());
        }
        this.id = extConfig.toJson().hashCode();
        this.userAgent = userAgent;
        this.client = client;
        this.transporter = null;
        this.appDir = null;
    }

    @Override
    public HttpClient unwrap() {
        return client;
    }

    @Override
    public Future<HttpClientRequest> openRequest(RequestOptions options) {
        return client.request(options)
                     .recover(t -> recover(t, c -> c.request(options)))
                     .map(req -> req.putHeader("User-Agent", userAgent));
    }

    @Override
    public Future<ResponseData> request(RequestOptions options, RequestData reqData, boolean swallowError) {
        MultiMap headers = Optional.ofNullable(reqData)
                                   .map(RequestData::headers)
                                   .map(HttpHeaderUtils::deserializeHeaders)
                                   .orElseGet(MultiMap::caseInsensitiveMultiMap);
        Buffer payload = Optional.ofNullable(reqData).map(RequestData::body).map(JsonObject::toBuffer).orElse(null);
        String query = Optional.ofNullable(reqData)
                               .flatMap(r -> Optional.ofNullable(HttpRequestUtils.serializeQuery(r.filter())))
                               .orElse(null);
        if (Strings.isNotBlank(query)) {
            options.setURI(Urls.buildURL(options.getURI(), query));
        }
        return openRequest(options).map(req -> {
                                       req.headers().addAll(headers);
                                       return req;
                                   })
                                   .flatMap(req -> payload == null ? req.send() : req.send(payload))
                                   .recover(this::wrapError)
                                   .flatMap(HttpClientJsonResponseHandler.create(swallowError,
                                                                                 extConfig.getHttpHandlers()
                                                                                          .getRespTextHandlerCls()));
    }

    @Override
    public Future<ResponseData> upload(String path, String uploadFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<ResponseData> push(String path, ReadStream readStream, HttpMethod method) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<AsyncFile> download(String path, AsyncFile saveFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<WriteStream> pull(String path, WriteStream writeStream) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<WebSocket> openWebSocket(WebSocketConnectOptions options) {
        return client.webSocket(options.addHeader("User-Agent", userAgent))
                     .recover(t -> recover(t, c -> c.webSocket(options)));
    }

    @Override
    public Future<EventMessage> openWebSocket(WebSocketConnectOptions options, WebSocketClientPlan plan) {
        return openWebSocket(options).map(ws -> {
            transporter.register(plan.outbound().getAddress(), new WebSocketClientWriter(ws));
            EventDirection inbound = plan.inbound();
            WebSocketHandlersConfig h = extConfig.getWebSocketHandlers();
            ws.handler(WebSocketClientDispatcher.create(transporter, inbound, h.getDispatcherCls()))
              .exceptionHandler(WebSocketClientErrorHandler.create(transporter, inbound, h.getErrorHandlerCls()));
            return EventMessage.success(EventAction.parse("OPEN"),
                                        new JsonObject().put("binaryHandlerID", ws.binaryHandlerID())
                                                        .put("textHandlerID", ws.textHandlerID())
                                                        .put("headers",
                                                             HttpHeaderUtils.serializeHeaders(ws.headers())));
        });
    }

    private <T> Future<T> recover(Throwable error, Function<HttpClient, Future<T>> fun) {
        if (error instanceof IllegalStateException && "Client is closed".equals(error.getMessage())) {
            client = transporter.getVertx().createHttpClient(extConfig.getOptions());
            return fun.apply(client);
        }
        return wrapError(error);
    }

    private <T> Future<T> wrapError(Throwable error) {
        if (error instanceof HttpException) {
            return Future.failedFuture(error);
        }
        if (error instanceof VertxException && error.getMessage().equals("Connection was closed") ||
            error instanceof DnsNameResolverTimeoutException) {
            return Future.failedFuture(new TimeoutException("Request timeout", error));
        }
        if (error instanceof UpgradeRejectedException) {
            final int status = ((UpgradeRejectedException) error).getStatus();
            return Future.failedFuture(new HttpException(status, error.getMessage(), error));
        }
        if (error instanceof UnknownHostException || error instanceof DnsNameResolverException) {
        }
        return Future.failedFuture(new HttpException(error));
    }

    //    private RequestData decorator(RequestData requestData) {
    //        RequestData reqData = Objects.isNull(requestData) ? RequestData.builder().build() : requestData;
    //        final JsonObject headers = reqData.headers();
    //        if (!headers.containsKey(HttpUtils.NONE_CONTENT_TYPE) &&
    //            !headers.containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
    //            headers.put(HttpHeaders.CONTENT_TYPE.toString(), HttpUtils.JSON_CONTENT_TYPE);
    //        }
    //        headers.remove(HttpUtils.NONE_CONTENT_TYPE);
    //        if (!headers.containsKey(HttpHeaders.USER_AGENT.toString())) {
    //            headers.put(HttpHeaders.USER_AGENT.toString(), this.getUserAgent());
    //        }
    //        return reqData;
    //    }
    //
    //    private Future<ResponseData> onConnectionSuccess(HttpClientRequest req, RequestData reqData, boolean
    //    swallowError) {
    //        if (logger().isDebugEnabled()) {
    //            logger().debug("Send HTTP request [{}][{}][{}]", req.getMethod(), req.absoluteURI(), reqData.toJson
    //            ());
    //        } else {
    //            logger().info("Send HTTP request [{}][{}]", req.getMethod(), req.absoluteURI());
    //        }
    //        return HttpRequestMessageComposer.create(getHandlersConfig().getReqComposerCls())
    //                                         .apply(req, reqData)
    //                                         .send()
    //                                         .flatMap(HttpResponseTextHandler.create(swallowError,
    //                                                                                 getHandlersConfig()
    //                                                                                 .getRespTextHandlerCls()));
    //    }
}
