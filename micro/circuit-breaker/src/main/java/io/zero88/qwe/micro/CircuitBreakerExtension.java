package io.zero88.qwe.micro;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.Extension;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.crypto.CryptoContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class CircuitBreakerExtension
    implements CircuitBreakerProvider, Extension<CircuitBreakerConfig, CircuitBreakerProvider> {

    private CircuitBreakerConfig extConfig;
    private CircuitBreaker circuitBreaker;

    @Override
    public String configKey() {
        return CircuitBreakerConfig.KEY;
    }

    @Override
    public @NonNull Class<CircuitBreakerConfig> configClass() {
        return CircuitBreakerConfig.class;
    }

    @Override
    public @NonNull String configFile() {
        return "circuitBreaker.json";
    }

    @Override
    public CircuitBreakerExtension entrypoint() {
        return this;
    }

    @Override
    public CircuitBreakerExtension setup(SharedDataLocalProxy sharedData, String appName, Path appDir,
                                         @NotNull JsonObject config, @NotNull CryptoContext cryptoContext) {
        extConfig = computeConfig(config);
        circuitBreaker = CircuitBreaker.create(appName + "-" + extConfig.getExtName(), sharedData.getVertx(),
                                               extConfig.getOptions());
        return this;
    }

    @Override
    public void stop() {
        circuitBreaker.close();
    }

    @Override
    public CircuitBreaker unwrap() {
        return this.circuitBreaker;
    }

}
