package io.zero88.qwe.http.server;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.zero88.qwe.PluginContext;
import io.zero88.qwe.PluginVerticle;
import io.zero88.qwe.exceptions.InitializerError;
import io.zero88.qwe.exceptions.QWEExceptionConverter;
import io.zero88.qwe.http.server.config.CorsOptions;
import io.zero88.qwe.http.server.download.DownloadRouterCreator;
import io.zero88.qwe.http.server.gateway.GatewayRouterCreator;
import io.zero88.qwe.http.server.handler.FailureContextHandler;
import io.zero88.qwe.http.server.handler.NotFoundContextHandler;
import io.zero88.qwe.http.server.rest.ProxyServiceApisCreator;
import io.zero88.qwe.http.server.rest.RestApisCreator;
import io.zero88.qwe.http.server.rest.RestEventApisCreator;
import io.zero88.qwe.http.server.upload.UploadRouterCreator;
import io.zero88.qwe.http.server.web.StaticWebRouterCreator;
import io.zero88.qwe.http.server.ws.WebSocketRouterCreator;

import lombok.NonNull;

public final class HttpServerPlugin extends PluginVerticle<HttpServerConfig, HttpServerPluginContext> {

    private final HttpServerRouter httpRouter;
    private HttpServer httpServer;

    HttpServerPlugin(@NonNull HttpServerRouter router) {
        this.httpRouter = router;
    }

    @Override
    public String pluginName() {
        return "http-server";
    }

    @Override
    public Class<HttpServerConfig> configClass() {return HttpServerConfig.class;}

    @Override
    public String configKey() {return HttpServerConfig.KEY;}

    @Override
    public String configFile() {return "httpServer.json";}

    @Override
    public void onStart() {
        super.onStart();
        if (this.pluginConfig.getApiConfig().getProxyConfig().isEnabled()) {
            this.pluginConfig.getApiConfig().setEnabled(true);
        }
        this.pluginConfig.setRuntimeConfig(httpRouter);
    }

    @Override
    public Future<Void> onAsyncStart() {
        return vertx.createHttpServer(createHttpServerOptions())
                    .requestHandler(initRouter())
                    .listen()
                    .onSuccess(server -> {
                        httpServer = server;
                        pluginConfig.setPort(httpServer.actualPort());
                        logger().info("HTTP Server started [{}:{}]", pluginConfig.getHost(), pluginConfig.getPort());
                        sharedData().addData(HttpServerPluginContext.SERVER_INFO_DATA_KEY,
                                             ServerInfo.create(pluginConfig, (Router) httpServer.requestHandler()));
                    })
                    .recover(t -> Future.failedFuture(QWEExceptionConverter.friendlyOrKeep(t)))
                    .mapEmpty();
    }

    @Override
    public Future<Void> onAsyncStop() {
        return Optional.ofNullable(httpServer).map(s -> httpServer.close()).orElseGet(Future::succeededFuture);
    }

    @Override
    public HttpServerPluginContext enrichContext(@NonNull PluginContext pluginContext, boolean isPostStep) {
        if (!isPostStep) {
            return new HttpServerPluginContext(pluginContext);
        }
        final ServerInfo info = sharedData().getData(HttpServerPluginContext.SERVER_INFO_DATA_KEY);
        return ((HttpServerPluginContext) pluginContext).setServerInfo(info);
    }

    private HttpServerOptions createHttpServerOptions() {
        final HttpServerOptions options = new HttpServerOptions(pluginConfig.getOptions());
        if (pluginConfig.getHttp2Cfg().isEnabled() || options.isSsl()) {
            final KeyCertOptions keyCertOptions = pluginContext().cryptoContext().getKeyCertOptions();
            if (Objects.isNull(keyCertOptions)) {
                throw new InitializerError("Missing key cert options to enable HTTP 2");
            }
            options.setUseAlpn(pluginConfig.getHttp2Cfg().isEnabled()).setSsl(true).setKeyCertOptions(keyCertOptions);
        }
        return options.setHost(pluginConfig.getHost())
                      .setPort(pluginConfig.getPort())
                      .setTrustOptions(pluginContext().cryptoContext().getTrustOptions());
    }

    private Router initRouter() {
        try {
            Router root = Router.router(vertx);
            CorsOptions corsOptions = pluginConfig.getCorsOptions();
            CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOriginPattern())
                                                 .allowedMethods(corsOptions.allowedMethods())
                                                 .allowedHeaders(corsOptions.getAllowedHeaders())
                                                 .allowCredentials(corsOptions.isAllowCredentials())
                                                 .exposedHeaders(corsOptions.getExposedHeaders())
                                                 .maxAgeSeconds(corsOptions.getMaxAgeSeconds());
            root.allowForward(pluginConfig.getAllowForwardHeaders())
                .route()
                .handler(corsHandler)
                //TODO Add LoggerHandlerProvider configuration
                .handler(LoggerHandler.create())
                .handler(ResponseContentTypeHandler.create())
                .handler(ResponseTimeHandler.create())
                .failureHandler(ResponseTimeHandler.create())
                .failureHandler(new FailureContextHandler());
            root = Stream.concat(
                             Stream.<Class<? extends RouterBuilder>>of(WebSocketRouterCreator.class,
                                                                       RestApisCreator.class,
                                                                       RestEventApisCreator.class,
                                                                       ProxyServiceApisCreator.class,
                                                                       GatewayRouterCreator.class,
                                                                       UploadRouterCreator.class,
                                                                       DownloadRouterCreator.class,
                                                                       StaticWebRouterCreator.class)
                                   .map(ReflectionClass::createObject), Stream.of(httpRouter.getCustomBuilder()))
                         .reduce(root, (r, b) -> b.setup(vertx, r, pluginConfig(), pluginContext()), (r1, r2) -> r2);
            root.routeWithRegex("(?!" + pluginConfig.getFileUploadConfig().getPath() + ").+")
                .handler(BodyHandler.create(false).setBodyLimit(pluginConfig.maxBodySize()));
            root.route().last().handler(new NotFoundContextHandler());
            return root;
        } catch (Exception e) {
            throw new InitializerError("Error when initializing HTTP Server route", e);
        }
    }

}
