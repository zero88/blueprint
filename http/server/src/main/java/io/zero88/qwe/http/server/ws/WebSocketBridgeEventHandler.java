package io.zero88.qwe.http.server.ws;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.http.server.HttpSystem.WebSocketSystem;

import lombok.NonNull;

/**
 * Represents for WebSocket sockJS bridge event handler.
 *
 * @apiNote Almost WebSocket data exchange between {@code WebSocket server} and {@code WebSocket} client will be as
 *     {@link EventMessage} structure.
 * @see BridgeEvent
 */
public interface WebSocketBridgeEventHandler extends Handler<BridgeEvent>, HasLogger, WebSocketSystem {

    /**
     * Represents a message data key to identify a {@code EventBus} inbound address bind to {@code WebSocket} path that
     * is shared to client side after opened {@code WebSocket connection} between server and client
     */
    String SEND_ADDRESSES = "sendAddresses";
    /**
     * Represents a message data key to identify a {@code EventBus} outbound address bind to {@code WebSocket} path that
     * is shared to client side after opened {@code WebSocket connection} between server and client
     */
    String LISTEN_ADDRESSES = "listenAddresses";

    @Override
    default Logger logger() {
        return LoggerFactory.getLogger(WebSocketBridgeEventHandler.class);
    }

    /**
     * Setup the handler
     *
     * @param sharedDataProxy   The shared data proxy
     * @param socketServerPlans The list of WebSocket server plan
     * @return a reference to this for fluent API
     * @see WebSocketServerPlan
     */
    WebSocketBridgeEventHandler setup(@NonNull SharedDataLocalProxy sharedDataProxy,
                                      @NonNull List<WebSocketServerPlan> socketServerPlans);

    @Override
    default void handle(BridgeEvent event) {
        Future<Void> future;
        if (event.type() == BridgeEventType.SOCKET_CREATED) {
            future = opened(event, logEvent(event, true));
        } else if (event.type() == BridgeEventType.SEND) {
            future = clientToServer(event, logEvent(event, false));
        } else if (event.type() == BridgeEventType.RECEIVE) {
            future = serverToClient(event, logEvent(event, false));
        } else {
            future = handleOthers(event, logEvent(event, true));
        }
        future.map(i -> true).onComplete(event);
    }

    default SockJSSocket logEvent(BridgeEvent event, boolean debug) {
        final SockJSSocket socket = event.socket();
        final String msg = decor("RemoteAddr[{}] - SocketAddr[{}] - Path[{}] - Event[{}]");
        if (debug) {
            logger().debug(msg, socket.remoteAddress(), socket.localAddress(), socket.uri(), event.type());
        } else {
            logger().info(msg, socket.remoteAddress(), socket.localAddress(), socket.uri(), event.type());
        }
        return socket;
    }

    /**
     * After opened new socket connection between server and client, server will send the WebSocket instruction to
     * client that helps a client side can easier do further step later.
     *
     * @param event  the bridge event
     * @param socket the sockjs socket
     * @return void future
     * @apiNote the WebSocket instruction is as {@link EventMessage} structure, with {@link EventAction#INIT} and
     *     body contains 2 keys {@code sendAddresses} and {@code listenAddresses}
     * @see BridgeEventType#SOCKET_CREATED
     * @see #SEND_ADDRESSES
     * @see #LISTEN_ADDRESSES
     */
    Future<Void> opened(BridgeEvent event, SockJSSocket socket);

    /**
     * Receive message from client then process and response/publish to one or more WebSocket client.
     *
     * @param event  the bridge event
     * @param socket the sockjs socket
     * @return void future
     * @see BridgeEventType#SEND
     */
    Future<Void> clientToServer(BridgeEvent event, SockJSSocket socket);

    /**
     * Publish data from server to client
     *
     * @param event  the bridge event
     * @param socket the sockjs socket
     * @return void future
     * @see BridgeEventType#RECEIVE
     */
    Future<Void> serverToClient(BridgeEvent event, SockJSSocket socket);

    /**
     * Handle other {@code bridge event} rather than {@link #opened(BridgeEvent, SockJSSocket)}, {@link
     * #clientToServer(BridgeEvent, SockJSSocket)} and {@link #serverToClient(BridgeEvent, SockJSSocket)}
     *
     * @param event  the bridge event
     * @param socket the sockjs socket
     * @see BridgeEventType
     */
    default Future<Void> handleOthers(BridgeEvent event, SockJSSocket socket) {
        return Future.succeededFuture();
    }

}
