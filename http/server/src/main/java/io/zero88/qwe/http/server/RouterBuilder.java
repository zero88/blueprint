package io.zero88.qwe.http.server;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@FunctionalInterface
public interface RouterBuilder {

    RouterBuilder NONE = (vertx, rootRouter, config, context) -> rootRouter;

    Router setup(@NotNull Vertx vertx, @NotNull Router rootRouter, @NotNull HttpServerConfig config,
                 @NotNull HttpServerPluginContext context);

}
