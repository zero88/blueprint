package io.zero88.qwe.http.client.handler;

import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebSocketClientWriter implements EventListener {

    private final WebSocket webSocket;

    @EBContract(action = "SEND")
    public void send(JsonObject data) {
        webSocket.write(data.toBuffer());
    }

}
