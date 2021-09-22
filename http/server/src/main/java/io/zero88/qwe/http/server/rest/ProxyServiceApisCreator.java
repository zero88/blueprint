package io.zero88.qwe.http.server.rest;

import java.nio.file.Path;
import java.util.function.Function;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Urls;
import io.vertx.ext.web.Router;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.exceptions.InitializerError;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.ApisSystem;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.http.server.config.ApiConfig;

import lombok.NonNull;

public final class ProxyServiceApisCreator implements RouterCreator<ApiConfig>, ApisSystem {

    @Override
    public String function() {
        return "Proxy-Service-API";
    }

    @Override
    public String routerPath(@NonNull ApiConfig config) {
        return Urls.combinePath(config.getPath(), config.getProxyConfig().getPath());
    }

    @Override
    public Function<HttpServerConfig, ApiConfig> lookupConfig() {
        return HttpServerConfig::getApiConfig;
    }

    @Override
    public boolean validate(ApiConfig config) {
        return config.getProxyConfig().isEnabled();
    }

    @Override
    public @NonNull String mountPoint(@NonNull ApiConfig config) {
        return config.getProxyConfig().getPath();
    }

    @Override
    public @NonNull Router subRouter(@NonNull SharedDataLocalProxy sharedData, @NonNull Path pluginDir,
                                     @NonNull ApiConfig config) {
        if (!ReflectionClass.hasClass("io.zero88.qwe.micro.DiscoveryContext")) {
            throw new InitializerError("To enabled dynamic route, you have to use on qwe-micro-discovery.jar plugin");
        }
        Router proxyRouter = Router.router(sharedData.getVertx());
        proxyRouter.route(RouterCreator.addWildcards(config.getProxyConfig().getPath())).disable();
        return proxyRouter;
    }

}
