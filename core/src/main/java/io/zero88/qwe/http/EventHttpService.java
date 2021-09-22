package io.zero88.qwe.http;

import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.launcher.VersionCommand;

/**
 * Represents an Event HTTP service that handles an incoming request from RESTful API.
 *
 * @see EventHttpApi
 * @see EventListener
 * @since 1.0.0
 */
public interface EventHttpService extends EventHttpApi, EventListener {

    /**
     * Declares {@code API Service} name.
     *
     * @return service name
     * @since 1.0.0
     */
    default String api() {
        return VersionCommand.getVersion().getName() + "::" + resource();
    }

    /**
     * Declares EventBus Address.
     *
     * @return service address. Default: {@code current class full-qualified name}
     * @since 1.0.0
     */
    default String address() {
        return getClass().getName();
    }

}
