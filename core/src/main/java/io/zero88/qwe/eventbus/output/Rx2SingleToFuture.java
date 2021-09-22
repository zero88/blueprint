package io.zero88.qwe.eventbus.output;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.reactivex.SingleHelper;
import io.zero88.qwe.eventbus.refl.MethodMeta;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Rx2SingleToFuture implements OutputToFuture<Single> {

    @Override
    public Class<Single> outputClass() {
        return Single.class;
    }

    @Override
    public Future<Object> transform(MethodMeta methodMeta, Object response) {
        return SingleHelper.toFuture((Single<Object>) response);
    }

}
