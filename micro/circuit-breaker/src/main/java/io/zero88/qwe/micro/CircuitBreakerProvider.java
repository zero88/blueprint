package io.zero88.qwe.micro;

import java.util.Objects;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.zero88.qwe.ExtensionEntrypoint;
import io.zero88.qwe.Wrapper;

public interface CircuitBreakerProvider extends Wrapper<CircuitBreaker>, ExtensionEntrypoint<CircuitBreakerConfig> {

    default <T> Future<T> execute(Future<T> command) {
        if (Objects.isNull(unwrap())) {
            return command;
        }
        return unwrap().execute(command::onComplete);
    }

}
