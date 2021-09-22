package io.zero88.qwe.eventbus;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.zero88.qwe.HasSharedData;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.Wrapper;
import io.zero88.qwe.transport.Transporter;

import lombok.NonNull;

/**
 * Represents EventBus that {@code send}/{@code request}/{@code publish} EventMessage. It also help to register an
 * {@code EventBus listener}
 *
 * @see EventListener
 * @see EventMessage
 * @see EventBusReplyHandler
 */
@VertxGen
public interface EventBusClient extends Transporter, HasSharedData, Wrapper<EventBus> {

    @GenIgnore
    static EventBusClient create(@NonNull SharedDataLocalProxy localDataProxy) {
        return create(localDataProxy, null);
    }

    @GenIgnore
    static EventBusClient create(@NonNull SharedDataLocalProxy localDataProxy,
                                 Class<EventBusReplyHandler> replyHandlerClass) {
        return new EventBusClientImpl(localDataProxy, replyHandlerClass);
    }

    /**
     * Send message to a specific address
     *
     * @param address Address
     * @param message Event message
     * @return future message
     * @see EventPattern#POINT_2_POINT
     * @see EventBus#send(String, Object)
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default EventBusClient send(@NonNull String address, @NonNull EventMessage message) {
        return send(address, message, null);
    }

    @Fluent
    @GenIgnore
    EventBusClient send(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options);

    /**
     * Send message to specific address then wait and handle response
     *
     * @param address Address
     * @param message Event message
     * @return future message
     * @see EventPattern#REQUEST_RESPONSE
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default Future<EventMessage> request(@NonNull String address, @NonNull EventMessage message) {
        return request(address, message, (DeliveryOptions) null);
    }

    /**
     * Same as {@link #request(String, EventMessage)} but with an {@code handler} called when the operation completes
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default EventBusClient request(@NonNull String address, @NonNull EventMessage message,
                                   @NonNull Handler<AsyncResult<EventMessage>> handler) {
        request(address, message).onComplete(handler);
        return this;
    }

    /**
     * Send message to specific address then wait and handle response
     *
     * @param address Address
     * @param message Event message
     * @param options Delivery options
     * @return future message
     * @see EventPattern#REQUEST_RESPONSE
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    Future<EventMessage> request(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options);

    /**
     * Same as {@link #request(String, EventMessage, DeliveryOptions)} but with an {@code handler} called when the
     * operation completes
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default EventBusClient request(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options,
                                   @NonNull Handler<AsyncResult<EventMessage>> handler) {
        request(address, message, options).onComplete(handler);
        return this;
    }

    /**
     * Publish message to specific address
     *
     * @param address Eventbus address
     * @param message Request data message
     * @see EventPattern#PUBLISH_SUBSCRIBE
     * @see EventBus#publish(String, Object)
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default EventBusClient publish(@NonNull String address, @NonNull EventMessage message) {
        return publish(address, message, null);
    }

    /**
     * Publish message to specific address
     *
     * @param address Eventbus address
     * @param message Request data message
     * @param options Delivery options
     * @see EventPattern#PUBLISH_SUBSCRIBE
     * @see EventBus#publish(String, Object)
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    EventBusClient publish(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options);

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default Future<EventMessage> fire(@NonNull String address, @NonNull EventPattern pattern,
                                      @NonNull EventMessage message) {
        if (pattern == EventPattern.REQUEST_RESPONSE) {
            return request(address, message);
        }
        if (pattern == EventPattern.POINT_2_POINT) {
            send(address, message);
            return Future.succeededFuture(EventMessage.success(EventAction.ACK));
        }
        if (pattern == EventPattern.PUBLISH_SUBSCRIBE) {
            publish(address, message);
            return Future.succeededFuture(EventMessage.success(EventAction.ACK));
        }
        throw new IllegalArgumentException("Unknown EventBus pattern [" + pattern + "]");
    }

    /**
     * Register event listener
     * <p>
     * It is equivalent to call {@link #register(String, boolean, EventListener)} with {@code local} is {@code true}
     *
     * @param address  Event bus address
     * @param listener listener to handle the received message
     * @return a reference to this, so the API can be used fluently
     * @see EventListener
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    default EventBusClient register(String address, @NonNull EventListener listener) {
        return this.register(address, true, listener);
    }

    /**
     * Register event listener
     *
     * @param address  Event bus address
     * @param local    If {@code true}, only register for local event address
     * @param listener listener to handle the received message
     * @return a reference to this, so the API can be used fluently
     * @see EventListener
     * @see #register(String, EventListener)
     */
    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    EventBusClient register(String address, boolean local, @NonNull EventListener listener);

    @Override
    default Vertx getVertx() {
        return sharedData().getVertx();
    }

    /**
     * @return vertx EventBus
     */
    default EventBus unwrap() {
        return getVertx().eventBus();
    }

}
