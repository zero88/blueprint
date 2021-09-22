package io.zero88.qwe.eventbus;

import java.util.Objects;

import io.vertx.core.eventbus.Message;
import io.zero88.qwe.QWEConverter;
import io.zero88.qwe.exceptions.UnsupportedException;

/**
 * One way converter from {@link Message} to {@link EventMessage}
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventBusMessageConverter extends QWEConverter<Message, EventMessage> {

    EventBusMessageConverter DEFAULT = msg -> {
        if (Objects.isNull(msg)) {
            return EventMessage.initial(EventAction.UNKNOWN);
        }
        EventMessage em = EventMessage.tryParse(msg.body());
        return msg.headers().contains("action")
               ? EventMessage.override(em, EventAction.parse(msg.headers().get("action")))
               : em;
    };

    @Override
    default Message to(EventMessage eventMessage) {
        throw new UnsupportedException(
            "Unsupported convert from [" + fromClass().getName() + "] to [" + toClass().getName() + "]");
    }

    @Override
    default Class<EventMessage> toClass() {
        return EventMessage.class;
    }

    @Override
    default Class<Message> fromClass() {
        return Message.class;
    }

}
