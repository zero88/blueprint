package io.zero88.qwe.http.server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.zero88.qwe.JsonHelper.Junit4;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.http.server.ws.WebSocketBridgeEventHandler;
import io.zero88.qwe.http.server.ws.WebSocketServerPlan;

import lombok.NonNull;

public interface WebSocketTestHelper extends HttpServerTestHelper {

    default Future<WebSocket> setupWSClient(TestContext context, String path) {
        return client().openWebSocket(wsOpt(requestOptions().setURI(Urls.combinePath(path, "websocket"))))
                       .onFailure(context::fail)
                       .map(ws -> ws.exceptionHandler(context::fail));
    }

    default WebSocket wsRegister(WebSocket webSocket, String address) {
        return wsWrite(webSocket, createWsMsg(address, BridgeEventType.REGISTER, (JsonObject) null).toBuffer());
    }

    default WebSocket wsSend(WebSocket webSocket, String address, EventMessage body) {
        return wsWrite(webSocket, createWsMsg(address, BridgeEventType.SEND, body).toBuffer());
    }

    default WebSocket wsWrite(WebSocket webSocket, JsonObject body) {
        return wsWrite(webSocket, body.toBuffer());
    }

    default WebSocket wsWrite(WebSocket webSocket, Buffer body) {
        webSocket.writeBinaryMessage(body);
        return webSocket;
    }

    default JsonObject createWsMsg(String address, BridgeEventType type, EventMessage body) {
        return createWsMsg(address, type, body.toJson());
    }

    default JsonObject createWsMsg(String address, BridgeEventType type, JsonObject body) {
        return new JsonObject().put("type", type == BridgeEventType.RECEIVE ? "rec" : type.name().toLowerCase())
                               .put("address", address)
                               .put("body", body);
    }

    default WebSocketConnectOptions wsOpt(@NonNull RequestOptions opt) {
        return new WebSocketConnectOptions(opt.setMethod(HttpMethod.GET).toJson());
    }

    default void doAssert(TestContext context, Async async, EventMessage openedMsg, JsonObject expected,
                          Buffer incomingMsg) {
        EventMessage msg = EventMessage.tryParse(incomingMsg.toJson(), false);
        if (EventAction.INIT.equals(msg.getAction())) {
            Junit4.assertJson(context, async, openedMsg.toJson(), msg.toJson());
            return;
        }
        Junit4.assertJson(context, async, expected, incomingMsg);
    }

    default EventMessage createOpenedMessage(WebSocketServerPlan... plans) {
        final List<String> inbounds = Arrays.stream(plans)
                                            .map(WebSocketServerPlan::inboundAddress)
                                            .filter(Strings::isNotBlank)
                                            .collect(Collectors.toList());
        final List<String> outbounds = Arrays.stream(plans)
                                             .map(WebSocketServerPlan::outboundAddress)
                                             .filter(Strings::isNotBlank)
                                             .collect(Collectors.toList());
        return EventMessage.success(EventAction.INIT,
                                    new JsonObject().put(WebSocketBridgeEventHandler.SEND_ADDRESSES, inbounds)
                                                    .put(WebSocketBridgeEventHandler.LISTEN_ADDRESSES, outbounds));
    }

}
