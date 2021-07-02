package io.zero88.qwe;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PluginVerticle<C extends PluginConfig, T extends PluginContext> extends AbstractVerticle
    implements Plugin<C, T>, PluginDeployHook<T>, VerticleLifecycleHooks {

    @Getter
    @NonNull
    private final SharedDataLocalProxy sharedData;
    @Getter
    protected C pluginConfig;
    @Getter
    private T pluginContext;

    @Override
    public final void start() {
        logger().debug("Start plugin [{}]...", pluginName());
        this.pluginConfig = computeConfig(config());
        this.onStart();
    }

    @Override
    public final void start(Promise<Void> promise) {
        QWEVerticle.asyncRun(vertx, promise, this::start, this::onAsyncStart);
    }

    @Override
    public final void stop() {
        logger().debug("Stop plugin [{}]...", pluginName());
        this.onStop();
    }

    @Override
    public final void stop(Promise<Void> promise) {
        QWEVerticle.asyncRun(vertx, promise, this::stop, this::onAsyncStop);
    }

    @Override
    public @NonNull PluginDeployHook<T> hook() {
        return this;
    }

    @Override
    public final T setup(T context) {
        return this.pluginContext = context;
    }

}
