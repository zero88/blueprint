package io.zero88.qwe;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public class DeployContext<T extends Verticle> {

    public static final Function<Class<? extends Verticle>, Consumer<String>> DEFAULT_ASSERTER
        = cls -> id -> TestHelper.LOGGER.info(
        "DEPLOY VERTICLE [" + cls.getName() + "][" + Objects.requireNonNull(id) + "]");

    @NonNull
    private final T verticle;
    @Default
    private final int timeout = TestHelper.TEST_TIMEOUT_SEC;
    @Default
    private final DeploymentOptions options = new DeploymentOptions();
    private final Consumer<String> successAsserter;
    private final Consumer<Throwable> failedAsserter;

    public Consumer<String> successAsserter() {
        if (Objects.nonNull(failedAsserter)) {
            return null;
        }
        if (Objects.isNull(successAsserter)) {
            return DEFAULT_ASSERTER.apply(verticle.getClass());
        }
        return DEFAULT_ASSERTER.andThen(handler -> handler.andThen(successAsserter)).apply(verticle.getClass());
    }

}
