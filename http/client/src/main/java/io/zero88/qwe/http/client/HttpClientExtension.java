package io.zero88.qwe.http.client;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.zero88.qwe.Extension;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.SharedDataLocalProxy;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public final class HttpClientExtension implements Extension<HttpClientConfig, HttpClientWrapper>, HasLogger {

    private int id;
    @Getter
    @Accessors(fluent = true)
    private HttpClientConfig extConfig;
    private final Map<Integer, HttpClientWrapper> registries = new ConcurrentHashMap<>();

    @Override
    public String configKey() {
        return HttpClientConfig.KEY;
    }

    @Override
    public @NonNull Class<HttpClientConfig> configClass() {
        return HttpClientConfig.class;
    }

    @Override
    public @NonNull String configFile() {
        return "httpClient.json";
    }

    @Override
    public HttpClientExtension setup(SharedDataLocalProxy sharedData, String appName, Path appDir,
                                     HttpClientConfig config) {
        this.extConfig = config == null ? new HttpClientConfig() : config;
        HttpClientWrapper wrapper = new HttpClientWrapperImpl(sharedData, appName, appDir, extConfig);
        this.id = wrapper.id();
        this.registries.put(wrapper.id(), wrapper);
        return this;
    }

    @Override
    public void stop() {
        Promise<Object> promise = Promise.promise();
        CompositeFuture.join(registries.values()
                                       .stream()
                                       .map(HttpClientWrapper::unwrap)
                                       .map(HttpClient::close)
                                       .collect(Collectors.toList()))
                       .onSuccess(c -> logger().debug("Closed [{}/{}] HTTP client(s)",
                                                      c.size() - c.causes().stream().filter(Objects::nonNull).count(),
                                                      c.size()))
                       .onFailure(t -> logger().debug("Something error when closing http client", t))
                       .mapEmpty()
                       .recover(t -> Future.succeededFuture())
                       .onComplete(promise);
    }

    @Override
    public HttpClientWrapper entrypoint() {
        return registries.get(id);
    }

}