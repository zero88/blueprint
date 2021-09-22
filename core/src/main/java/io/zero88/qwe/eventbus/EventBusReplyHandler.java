package io.zero88.qwe.eventbus;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.exceptions.HiddenException;
import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.eventbus.Message;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.eventbus.EventBusLogSystem.EventReplyLogSystem;
import io.zero88.qwe.exceptions.ServiceUnavailable;

import lombok.NonNull;

public interface EventBusReplyHandler extends HasLogger, EventReplyLogSystem {

    static EventBusReplyHandler create() {
        return () -> EventBusMessageConverter.DEFAULT;
    }

    static EventBusReplyHandler create(Class<EventBusReplyHandler> clazz) {
        return Objects.isNull(clazz) ? create() : ReflectionClass.createObject(clazz);
    }

    @Override
    default Logger logger() {
        return LoggerFactory.getLogger(EventBusReplyHandler.class);
    }

    @NonNull EventBusMessageConverter converter();

    /**
     * Handle a reply message in success case
     *
     * @param replyMsg a reply message
     * @return event message
     */
    default EventMessage succeed(Message replyMsg) {
        return converter().from(replyMsg);
    }

    /**
     * Handle a reply exception, mostly is timeout in fail case
     *
     * @param action the request action
     * @param error  the error
     * @return an event message
     * @see EventMessage#replyError(EventAction, ErrorMessage)
     * @see EventMessage#replyError(EventAction, Throwable)
     */
    default EventMessage error(@NonNull EventAction action, @NonNull Throwable error) {
        return EventMessage.replyError(action, new ServiceUnavailable("No response", new HiddenException(error)));
    }

}
