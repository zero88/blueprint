package io.zero88.qwe.eventbus.output;

import io.vertx.core.Future;
import io.zero88.qwe.eventbus.refl.MethodMeta;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class FutureIdentify implements OutputToFuture<Future> {

    @Override
    public Class<Future> outputClass() {
        return Future.class;
    }

    @Override
    public Future<Object> transform(MethodMeta methodMeta, Object response) {
        return (Future<Object>) response;
    }

}
