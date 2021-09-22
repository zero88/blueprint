package io.zero88.qwe.http;

import java.util.Set;

/**
 * Represents an RESTful API proxy that dispatches a client request to a specific {@code EventBus consumer}
 *
 * @since 1.0.0
 */
public interface EventHttpApi {

    /**
     * Declares a REST Resource name
     *
     * @return the resource name
     * @since 1.0.0
     */
    default String resource() {
        return getClass().getSimpleName();
    }

    /**
     * Declares an EventBus consumer address.
     *
     * @return the consumer address
     * @since 1.0.0
     */
    String address();

    /**
     * Declares the HTTP router definitions.
     * <p>
     * One {@code EventBus} address can have one or more HTTP definition(s) because one resource can be accessed via one
     * or more HTTP path, for example:
     *
     * <ul>
     *   <li>{@code direct}: {@code /product/:productId}</li>
     *   <li>{@code transitive}: {@code /client/:clientId/product/:productId}</li>
     * </ul>
     *
     * @return router mapping between {@code EventAction} and {@code HttpMethod}
     * @apiNote one resource can be accessed via one or more HTTP path
     * @see EventMethodDefinition
     * @since 1.0.0
     */
    Set<EventMethodDefinition> definitions();

}
