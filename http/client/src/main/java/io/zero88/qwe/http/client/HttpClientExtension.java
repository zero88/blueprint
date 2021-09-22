package io.zero88.qwe.http.client;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.Extension;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.crypto.CryptoContext;

import lombok.NonNull;

public final class HttpClientExtension implements Extension<HttpClientConfig, HttpClientWrapper>, HasLogger {

    private int id;
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
                                     @NotNull JsonObject config, @NotNull CryptoContext cryptoContext) {
        final HttpClientConfig clientConf = computeConfig(config);
        final HttpClientOptions options = clientConf.getOptions();
        if (clientConf.isHttp2Enabled()) {
            options.setProtocolVersion(HttpVersion.HTTP_2).setUseAlpn(true);
        }
        options.setKeyCertOptions(cryptoContext.getKeyCertOptions()).setTrustOptions(cryptoContext.getTrustOptions());
        HttpClientWrapper wrapper = new HttpClientWrapperImpl(sharedData, appName, appDir, clientConf);
        this.id = wrapper.id();
        this.registries.put(wrapper.id(), wrapper);
        return this;
    }

    @Override
    public void stop() {
        HttpClientWrapperInternal entrypoint = (HttpClientWrapperInternal) entrypoint();
        Promise<Object> promise = null;
        if (Objects.nonNull(entrypoint.transporter()) && entrypoint.transporter().getVertx() instanceof VertxInternal) {
            promise = ((VertxInternal) entrypoint.transporter().getVertx()).promise();
        }
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
                       .onComplete(Optional.ofNullable(promise).orElseGet(Promise::promise));
    }

    @Override
    public HttpClientWrapper entrypoint() {
        return registries.get(id);
    }

}
