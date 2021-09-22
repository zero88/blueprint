package io.zero88.qwe.http.server.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.zero88.qwe.http.server.HttpRuntimeConfig;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpServerPluginContext;
import io.zero88.qwe.http.server.HttpSystem.ApisSystem;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.RouterCreator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class ApisCreator<X, C extends RouterConfig> implements RouterCreator<C>, ApisSystem {

    @Getter(value = AccessLevel.PROTECTED)
    private final Set<Class<? extends X>> apis = new HashSet<>();

    @Override
    public Router setup(Vertx vertx, Router rootRouter, HttpServerConfig config, HttpServerPluginContext context) {
        register(config.getRuntimeConfig());
        return RouterCreator.super.setup(vertx, rootRouter, config, context);
    }

    @Override
    public String routerName() {
        return subFunction();
    }

    @Override
    public boolean validate(C config) {
        return !apis.isEmpty();
    }

    protected final ApisCreator<X, C> register(@NonNull Collection<Class<? extends X>> restApis) {
        restApis.stream().filter(Objects::nonNull).forEach(apis::add);
        return this;
    }

    protected abstract String subFunction();

    protected abstract void register(HttpRuntimeConfig runtimeConfig);

}
