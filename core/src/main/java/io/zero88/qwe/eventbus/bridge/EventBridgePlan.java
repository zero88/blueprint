package io.zero88.qwe.eventbus.bridge;

import java.util.Optional;

import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventPattern;

import lombok.Getter;
import lombok.experimental.Accessors;

public interface EventBridgePlan {

    EventDirection inbound();

    /**
     * Defines an event {@code in-bound} direction in current program.
     * <p>
     * It is a public entrypoint that is bound in current program bridge, then any external program can send data to
     * current program via this address.
     *
     * @see EventDirection
     */
    default String inboundAddress() {
        return Optional.ofNullable(inbound()).map(EventDirection::getAddress).orElse(null);
    }

    EventDirection processor();

    /**
     * Defines a processor address
     *
     * @return a processor address
     */
    default String processAddress() {
        return processor().getAddress();
    }

    default EventPattern processPattern() {
        return processor().getPattern();
    }

    EventDirection outbound();

    /**
     * Defines an event {@code out-bound} direction in current program.
     * <p>
     * It is a public egress point that is bound in current program bridge, then any internal data can publish to
     * external program via this address.
     */
    default String outboundAddress() {
        return Optional.ofNullable(outbound()).map(EventDirection::getAddress).orElse(null);
    }

    @Getter
    @Accessors(fluent = true)
    abstract class AbstractEventBridgePlan<T extends EventBridgePlan> implements EventBridgePlan {

        private final EventDirection inbound;
        private final EventDirection processor;
        private final EventDirection outbound;

        protected AbstractEventBridgePlan(EventDirection inbound, EventDirection processor, EventDirection outbound) {
            this.inbound = inbound;
            this.processor = processor;
            this.outbound = outbound;
        }

        protected abstract T validate();

    }

}
