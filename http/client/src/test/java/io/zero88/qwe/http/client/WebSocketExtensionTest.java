package io.zero88.qwe.http.client;

import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertx.core.http.UpgradeRejectedException;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.JsonHelper.Junit5;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventBusProxy;
import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.http.HttpException;
import io.zero88.qwe.http.client.handler.WebSocketClientPlan;

import lombok.RequiredArgsConstructor;

public class WebSocketExtensionTest extends HttpExtensionTestBase {

    private static final EventDirection LISTENER = EventDirection.builder()
                                                                 .address("ws.listener")
                                                                 .local(true)
                                                                 .pattern(EventPattern.POINT_2_POINT)
                                                                 .build();
    private static final String PUBLISHER_ADDRESS = "ws.publisher";

    @Test
    public void test_not_found(VertxTestContext context) {
        Checkpoint cp = context.checkpoint();
        WebSocketConnectOptions options = new WebSocketConnectOptions().setHost("google.com").setURI("/xxx");
        extension.entrypoint()
                 .openWebSocket(options, WebSocketClientPlan.create(LISTENER, PUBLISHER_ADDRESS))
                 .onFailure(t -> context.verify(() -> {
                     Assertions.assertTrue(t instanceof HttpException);
                     Assertions.assertTrue(t.getCause() instanceof UpgradeRejectedException);
                     Assertions.assertEquals(404, ((HttpException) t).getStatusCode().code());
                     cp.flag();
                 }))
                 .onSuccess(msg -> context.failNow("Failed test should not success"));
    }

    @Test
    public void test_connect_failed_due_unknown_dns(VertxTestContext context) {
        Checkpoint cp = context.checkpoint();
        WebSocketConnectOptions options = new WebSocketConnectOptions().setHost("echo.websocket.test").setURI("/xxx");
        extension.entrypoint()
                 .openWebSocket(options, WebSocketClientPlan.create(LISTENER, PUBLISHER_ADDRESS))
                 .onFailure(t -> context.verify(() -> {
                     Assertions.assertTrue(t instanceof HttpException);
                     Assertions.assertTrue(t.getCause() instanceof UnknownHostException);
                     Assertions.assertEquals(
                         "failed to resolve 'echo.websocket.test'. Exceeded max queries per resolve 4 ",
                         t.getCause().getMessage());
                     cp.flag();
                 }))
                 .onSuccess(msg -> context.failNow("Failed test should not success"));
    }

    @Test
    @Disabled
    //TODO need to setup simple websocket echo server: https://github.com/jmalloc/echo-server
    //echo.websocket.org is no longer available
    public void test_connect_and_send(VertxTestContext context) {
        Checkpoint cp = context.checkpoint();
        EventBusClient eb = ((EventBusProxy) extension.entrypoint()).transporter();
        eb.register(LISTENER.getAddress(), new EventAsserter(context, cp, new JsonObject().put("k", 1)));
        extension.entrypoint()
                 .openWebSocket(new WebSocketConnectOptions().setHost("echo.websocket.org").setURI("/echo"),
                                WebSocketClientPlan.create(LISTENER, PUBLISHER_ADDRESS))
                 .onSuccess(msg -> TestHelper.LOGGER.info("{}", msg.toJson()))
                 .onSuccess(msg -> eb.unwrap()
                                     .send(msg.getData().getString("binaryHandlerID"),
                                           EventMessage.initial(EventAction.SEND, new JsonObject().put("k", 1))
                                                       .toJson()
                                                       .toBuffer()))
                 .onFailure(context::failNow);
    }

    @RequiredArgsConstructor
    static class EventAsserter implements EventListener {

        private final VertxTestContext context;
        private final Checkpoint cp;
        private final JsonObject expected;

        @EBContract(action = "SEND")
        public void send(JsonObject data) {
            context.verify(() -> Junit5.assertJson(context, cp, expected, data));
        }

    }

}
