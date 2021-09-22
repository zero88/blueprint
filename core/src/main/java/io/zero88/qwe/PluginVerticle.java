package io.zero88.qwe;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class PluginVerticle<C extends PluginConfig, T extends PluginContext> extends AbstractVerticle
    implements Plugin<C, T>, PluginDeployHook, VerticleLifecycleHooks {

    @Getter
    protected C pluginConfig;
    @Getter
    private T pluginContext;

    @Override
    public final void start() {
        this.pluginConfig = computeConfig(config());
        this.onStart();
    }

    @Override
    public final void start(Promise<Void> promise) {
        logger().debug("Start Plugin[{}]...", pluginName());
        QWEVerticle.asyncRun(vertx, promise, this::start, this::onAsyncStart);
    }

    @Override
    public final void stop() {
        this.onStop();
    }

    @Override
    public final void stop(Promise<Void> promise) {
        logger().debug("Stop Plugin[{}]...", pluginName());
        QWEVerticle.asyncRun(vertx, promise, this::stop, this::onAsyncStop);
    }

    @Override
    public @NonNull PluginDeployHook deployHook() {
        return this;
    }

    @Override
    public final Plugin<C, T> setup(T context) {
        this.pluginContext = context;
        return this;
    }

}
