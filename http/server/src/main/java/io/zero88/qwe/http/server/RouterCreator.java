package io.zero88.qwe.http.server;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.utils.Urls;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.SharedDataLocalProxy;

import lombok.NonNull;

public interface RouterCreator<C extends RouterConfig> extends RouterBuilder, HasLogger, HttpSystem {

    static Route addContentType(Route route, List<String> contentTypes) {
        return contentTypes.stream().reduce(route, Route::produces, (r1, r2) -> r1);
    }

    static String addWildcards(String path) {
        return Urls.combinePath(path, "*");
    }

    default Logger logger() {
        return LoggerFactory.getLogger(RouterCreator.class);
    }

    @Override
    default Router setup(Vertx vertx, Router rootRouter, HttpServerConfig config, HttpServerPluginContext context) {
        C cfg = lookupConfig().apply(config);
        if (!cfg.isEnabled() || !validate(cfg)) {
            return rootRouter;
        }
        logger().info(decor("Setup route [{}][{}]"), routerName(), addWildcards(routerPath(cfg)));
        rootRouter.mountSubRouter(mountPoint(cfg), subRouter(context.sharedData(),
                                                             Objects.requireNonNull(context.dataDir(),
                                                                                    "Missing HTTP plugin dir"), cfg));
        return rootRouter;
    }

    default boolean validate(C config) {
        return true;
    }

    default String routerName() {
        return function();
    }

    default String routerPath(@NonNull C config) {
        return mountPoint(config);
    }

    default @NonNull String mountPoint(@NonNull C config) {
        return config.getPath();
    }

    Function<HttpServerConfig, C> lookupConfig();

    /**
     * Create new sub router based on configuration. It will be mounted as sub router in root router
     *
     * @param sharedData Shared data proxy
     * @param pluginDir  the plugin dir
     * @param config     Router config
     * @return the sub router
     * @see RouterBuilder#setup(Vertx, Router, HttpServerConfig, HttpServerPluginContext)
     */
    @NonNull Router subRouter(@NonNull SharedDataLocalProxy sharedData, @NonNull Path pluginDir, @NonNull C config);

}
