package io.zero88.qwe.eventbus;

import java.util.function.Supplier;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.shareddata.Shareable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EventBusDeliveryOption implements Shareable, Supplier<DeliveryOptions> {

    private final DeliveryOptions options;

    @Override
    public Shareable copy() {
        return new EventBusDeliveryOption(new DeliveryOptions(this.options));
    }

    @Override
    public DeliveryOptions get() {
        return options;
    }

}
