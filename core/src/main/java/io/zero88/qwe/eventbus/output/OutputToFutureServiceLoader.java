package io.zero88.qwe.eventbus.output;

import java.util.Collection;

import io.vertx.core.ServiceHelper;

import lombok.Getter;

/**
 * Represents the service loader {@code META-INF/services} for {@link OutputToFuture} handlers
 *
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public final class OutputToFutureServiceLoader {

    static OutputToFutureServiceLoader instance;

    @Getter
    private final Collection<OutputToFuture> handlers;

    private OutputToFutureServiceLoader() {
        this.handlers = ServiceHelper.loadFactories(OutputToFuture.class, getClass().getClassLoader());
    }

    public static OutputToFutureServiceLoader getInstance() {
        if (instance == null) {
            synchronized (OutputToFutureServiceLoader.class) {
                instance = new OutputToFutureServiceLoader();
            }
        }
        return instance;
    }

}
