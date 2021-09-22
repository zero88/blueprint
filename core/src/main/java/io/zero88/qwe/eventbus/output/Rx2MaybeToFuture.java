package io.zero88.qwe.eventbus.output;

import io.reactivex.Maybe;
import io.vertx.core.Future;
import io.vertx.reactivex.MaybeHelper;
import io.zero88.qwe.eventbus.refl.MethodMeta;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Rx2MaybeToFuture implements OutputToFuture<Maybe> {

    @Override
    public Class<Maybe> outputClass() {
        return Maybe.class;
    }

    @Override
    public Future<Object> transform(MethodMeta methodMeta, Object response) {
        return MaybeHelper.toFuture((Maybe<Object>) response);
    }

}
