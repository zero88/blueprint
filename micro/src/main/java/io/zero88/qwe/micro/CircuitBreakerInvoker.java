package io.zero88.qwe.micro;

import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.zero88.qwe.micro.MicroConfig.CircuitBreakerConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CircuitBreakerInvoker implements Supplier<CircuitBreaker> {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerInvoker.class);

    private final CircuitBreaker circuitBreaker;

    static CircuitBreakerInvoker create(Vertx vertx, CircuitBreakerConfig cfg) {
        if (cfg.isEnabled()) {
            logger.info("Circuit Breaker Config : {}", cfg.toJson().encode());
            return new CircuitBreakerInvoker(CircuitBreaker.create(cfg.getCircuitName(), vertx, cfg.getOptions()));
        }
        logger.info("Skip setup circuit breaker");
        return new CircuitBreakerInvoker(null);
    }

    @Override
    public CircuitBreaker get() {
        return Objects.requireNonNull(this.circuitBreaker, "Circuit breaker is not enabled");
    }

    public <T> Future<T> wrap(Future<T> command) {
        if (Objects.isNull(circuitBreaker)) {
            return command;
        }
        return get().execute(event -> event.handle(command));
    }

}
