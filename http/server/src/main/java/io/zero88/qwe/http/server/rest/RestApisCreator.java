package io.zero88.qwe.http.server.rest;

import java.nio.file.Path;
import java.util.function.Function;

import io.vertx.ext.web.Router;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.http.server.HttpRuntimeConfig;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.config.ApiConfig;
import io.zero88.qwe.http.server.rest.api.RestApi;

import lombok.NonNull;

public final class RestApisCreator extends ApisCreator<RestApi, ApiConfig> {

    @Override
    public Function<HttpServerConfig, ApiConfig> lookupConfig() {
        return HttpServerConfig::getApiConfig;
    }

    @Override
    public @NonNull Router subRouter(@NonNull SharedDataLocalProxy sharedData, @NonNull Path pluginDir,
                                     @NonNull ApiConfig config) {
        Object[] classes = getApis().toArray(new Class[] {});
        //TODO register RestAPI
        return Router.router(sharedData.getVertx());
    }

    @Override
    protected String subFunction() {
        return "REST-API";
    }

    @Override
    protected void register(HttpRuntimeConfig runtimeConfig) {
        this.register(runtimeConfig.getRestApiClasses());
    }

}
