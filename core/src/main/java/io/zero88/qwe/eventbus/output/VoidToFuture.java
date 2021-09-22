package io.zero88.qwe.eventbus.output;

import io.vertx.core.Future;
import io.zero88.qwe.eventbus.refl.MethodMeta;

public final class VoidToFuture implements OutputToFuture<Void> {

    @Override
    public Class<Void> outputClass() {
        return Void.class;
    }

    @Override
    public boolean verify(MethodMeta methodMeta) {
        return methodMeta.method().getReturnType() == void.class;
    }

    @Override
    public Future<Object> transform(MethodMeta methodMeta, Object response) {
        return Future.succeededFuture();
    }

}
