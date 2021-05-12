package io.zero88.qwe.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zero88.qwe.SharedDataLocalProxy;
import io.vertx.ext.web.Router;

import lombok.NonNull;

public interface RouterCreator<T extends RouterConfig> {

    default Logger log() {
        return LoggerFactory.getLogger(RouterCreator.class);
    }

    /**
     * Mount sub router into root router
     *
     * @param rootRouter root router
     * @param config     router config
     * @param sharedData shared data
     * @return a reference to root router for fluent API
     */
    default @NonNull Router mount(@NonNull Router rootRouter, @NonNull T config,
                                  @NonNull SharedDataLocalProxy sharedData) {
        if (!config.isEnabled()) {
            return rootRouter;
        }
        rootRouter.mountSubRouter(mountPoint(config), router(config, sharedData));
        return rootRouter;
    }

    default @NonNull String mountPoint(@NonNull T config) {
        return config.basePath();
    }

    /**
     * Create new router based on configuration. It will be mounted as sub router in root router
     *
     * @param config     Router config
     * @param sharedData shared data
     * @return router
     * @see #mount(Router, RouterConfig, SharedDataLocalProxy)
     */
    @NonNull Router router(@NonNull T config, @NonNull SharedDataLocalProxy sharedData);

}
