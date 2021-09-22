package io.zero88.qwe.http.server.ws;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.bridge.EventBridgeExecutor;
import io.zero88.qwe.eventbus.bridge.EventBridgePlan;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.http.HttpUtils.HttpHeaderUtils;

import lombok.NonNull;

/**
 * Default WebSocket bridge event bus handler
 */
//TODO handle auth with socket header
public class DefaultWebSocketBridgeEventHandler implements WebSocketBridgeEventHandler {

    protected EventBridgeExecutor executor;
    protected Map<String, WebSocketServerPlan> c2sAddresses;
    protected Map<String, WebSocketServerPlan> s2cAddresses;

    @Override
    public WebSocketBridgeEventHandler setup(@NonNull SharedDataLocalProxy sharedDataProxy,
                                             @NonNull List<WebSocketServerPlan> wsPlans) {
        this.executor = () -> sharedDataProxy;
        this.c2sAddresses = wsPlans.stream()
                                   .filter(p -> !p.isOnlyOutbound())
                                   .collect(Collectors.toMap(EventBridgePlan::inboundAddress, Function.identity()));
        this.s2cAddresses = wsPlans.stream()
                                   .filter(p -> Strings.isNotBlank(p.outboundAddress()))
                                   .collect(Collectors.toMap(EventBridgePlan::outboundAddress, Function.identity()));
        return this;
    }

    @Override
    public Future<Void> opened(BridgeEvent event, SockJSSocket socket) {
        return socket.write(EventMessage.success(EventAction.INIT,
                                                 new JsonObject().put(SEND_ADDRESSES, c2sAddresses.keySet())
                                                                 .put(LISTEN_ADDRESSES, s2cAddresses.keySet()))
                                        .toJson()
                                        .toBuffer());
    }

    public Future<Void> clientToServer(BridgeEvent event, SockJSSocket socket) {
        final JsonObject raw = event.getRawMessage();
        final String address = raw.getString("address");
        final WebSocketServerPlan plan = c2sAddresses.get(address);
        if (Objects.isNull(plan)) {
            return Future.succeededFuture();
        }
        try {
            EventMessage msg = parseMessage(HttpHeaderUtils.serializeHeaders(socket.headers()), raw);
            logger().info(decor("Forward the bridge message [{}=>{}][{}]"), address, plan.processAddress(),
                          msg.getAction());
            return executor.execute(plan, msg).flatMap(resp -> socket.write(resp.toJson().toBuffer()));
        } catch (QWEException e) {
            return socket.write(EventMessage.error(EventAction.ACK, e).toJson().toBuffer());
        }
    }

    public Future<Void> serverToClient(BridgeEvent event, SockJSSocket socket) {
        final JsonObject raw = event.getRawMessage();
        final String address = raw.getString("address");
        final WebSocketServerPlan plan = s2cAddresses.get(address);
        if (Objects.isNull(plan)) {
            return Future.succeededFuture();
        }
        try {
            EventMessage msg = JsonData.from(raw.getJsonObject("body"), EventMessage.class,
                                             "Invalid WebSocket message");
            logger().info(decor("Publish the bridge message on [{}][{}=>{}]"), address, msg.getAction(),
                          msg.getPrevAction());
            event.setRawMessage(msg.toJson());
            return Future.succeededFuture();
        } catch (QWEException e) {
            event.setRawMessage(EventMessage.error(EventAction.ACK, e).toJson());
            return Future.succeededFuture();
        }
    }

    private EventMessage parseMessage(JsonObject headers, JsonObject raw) {
        final EventMessage body = JsonData.from(raw.getJsonObject("body"), EventMessage.class,
                                                "Invalid WebSocket message");
        return EventMessage.initial(body.getAction(), Functions.getOrDefault(() -> {
            final RequestData req = JsonData.from(body.getData(), RequestData.class);
            return req.setHeaders(headers.mergeIn(req.headers()));
        }, () -> RequestData.builder().body(body.getData()).headers(headers).build()));
    }

}
