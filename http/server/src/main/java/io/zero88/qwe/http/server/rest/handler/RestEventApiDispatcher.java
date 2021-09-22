package io.zero88.qwe.http.server.rest.handler;

import java.util.Objects;

import io.github.zero88.repl.ReflectionClass;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.eventbus.DeliveryEvent;
import io.zero88.qwe.http.server.handler.EventBusProxyDispatcher;

/**
 * Represents for pushing data via {@code EventBus} then listen {@code reply message}
 */
public interface RestEventApiDispatcher extends EventBusProxyDispatcher {

    static RestEventApiDispatcher create(Class<RestEventApiDispatcher> cls) {
        if (cls == null) {
            return ReflectionClass.createObject(RestEventApiDispatcherImpl.class);
        }
        return Objects.requireNonNull(ReflectionClass.createObject(cls), "Unable create REST dispatcher");
    }

    RestEventApiDispatcher setup(String sharedKey, ReqAuthDefinition authDefinition, DeliveryEvent deliveryEvent);

}
