package io.zero88.qwe.http.server.ws;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.zero88.utils.Urls;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.UpgradeRejectedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zero88.qwe.JsonHelper.Junit4;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.http.server.WebSocketTestHelper;
import io.zero88.qwe.http.server.mock.MockWebSocketEvent;
import io.zero88.qwe.http.server.mock.MockWebSocketEventListener;

@RunWith(VertxUnitRunner.class)
public class WebSocketEventServerTest extends HttpServerPluginTestBase implements WebSocketTestHelper {

    @Rule
    public Timeout timeout = Timeout.seconds(20);

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.httpConfig.getApiConfig().setEnabled(false);
        this.httpConfig.getWebSocketConfig().setEnabled(true);
        EventBusClient.create(createSharedData(vertx))
                      .register(MockWebSocketEvent.PROCESSOR.getAddress(), new MockWebSocketEventListener());
    }

    @Test
    public void test_greeting(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.GREETING));
        Async async = context.async(2);
        assertGreeting(context, async, "/ws/");
        assertGreeting(context, async, "/ws");
    }

    @Test
    public void test_not_found(TestContext context) {
        Async async = context.async(3);
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.GREETING));
        assertNotFound(context, async, "/socket1");
        assertNotFound(context, async, "/ws//");
        assertNotFound(context, async, "/ws/xyz");
    }

    @Test
    public void test_wsClient_send_invalid_json(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final JsonObject expected = new JsonObject("{\"type\":\"err\",\"body\":\"invalid_json\"}");
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected, b)))
            .onSuccess(ws -> wsWrite(ws, Buffer.buffer("xx")));
    }

    @Test
    public void test_wsClient_send_invalid_wsMessage(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final EventMessage expected = EventMessage.error(EventAction.ACK, ErrorCode.INVALID_ARGUMENT,
                                                         "Invalid WebSocket message");
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        final JsonObject req = createWsMsg(MockWebSocketEvent.NO_OUTBOUND.inboundAddress(), BridgeEventType.SEND,
                                           new JsonObject("{\"type\":\"err\"}"));
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected.toJson(), b)))
            .onSuccess(ws -> wsWrite(ws, req));
    }

    @Test
    public void test_wsClient_send_to_unknown_address(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final JsonObject expected = new JsonObject("{\"type\":\"err\",\"body\":\"access_denied\"}");
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected, b)))
            .onSuccess(ws -> wsSend(ws, "123", EventMessage.initial(EventAction.GET_ONE)));
    }

    @Test
    public void test_wsClient_request_then_server_response(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final EventMessage resp = EventMessage.replySuccess(EventAction.GET_LIST,
                                                            new JsonObject().put("data", Arrays.asList("1", "2", "3")));
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, resp.toJson(), b)))
            .onSuccess(ws -> wsSend(ws, MockWebSocketEvent.NO_OUTBOUND.inboundAddress(),
                                    EventMessage.initial(EventAction.GET_LIST)));
    }

    @Test
    public void test_wsClient_send_then_server_publish(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.FULL_PLAN));
        final Async async = context.async(4);
        final EventMessage outboundExpected = EventMessage.replySuccess(EventAction.GET_ONE,
                                                                        new JsonObject().put("data", "1"));
        final EventMessage ackExpected = EventMessage.success(EventAction.ACK);
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.FULL_PLAN);
        final String path = wsPath(MockWebSocketEvent.FULL_PLAN);
        this.setupWSClient(context, path)
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, outboundExpected.toJson(), b)))
            .map(ws -> wsRegister(ws, MockWebSocketEvent.FULL_PLAN.outboundAddress()))
            .flatMap(ignore -> this.setupWSClient(context, path))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, ackExpected.toJson(), b)))
            .onSuccess(ws -> wsSend(ws, MockWebSocketEvent.FULL_PLAN.inboundAddress(),
                                    EventMessage.initial(EventAction.GET_ONE)));
    }

    @Test
    public void test_wsClient_request_then_server_error(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final EventMessage expected = EventMessage.replyError(EventAction.DISCOVER,
                                                              ErrorMessage.parse(ErrorCode.INVALID_ARGUMENT, "Error"));
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected.toJson(), b)))
            .onSuccess(ws -> wsSend(ws, MockWebSocketEvent.NO_OUTBOUND.inboundAddress(),
                                    EventMessage.initial(EventAction.DISCOVER)));
    }

    @Test
    public void test_wsClient_request_unsupported_server_event(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.NO_OUTBOUND));
        final Async async = context.async(1);
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.NO_OUTBOUND);
        final EventMessage expected = EventMessage.replyError(EventAction.REMOVE,
                                                              ErrorMessage.parse(ErrorCode.SERVICE_NOT_FOUND,
                                                                                 "Service not found | Cause: " +
                                                                                 "Unsupported event [REMOVE] - Error " +
                                                                                 "Code: UNSUPPORTED"));
        this.setupWSClient(context, wsPath(MockWebSocketEvent.NO_OUTBOUND))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected.toJson(), b)))
            .onSuccess(ws -> wsSend(ws, MockWebSocketEvent.NO_OUTBOUND.inboundAddress(),
                                    EventMessage.initial(EventAction.REMOVE)));
    }

    @Test
    public void test_wsClient_send_unsupported_server_event(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.FULL_PLAN));
        final Async async = context.async(2);
        final EventMessage ackExpected = EventMessage.replyError(EventAction.CREATE,
                                                                 ErrorMessage.parse(ErrorCode.SERVICE_NOT_FOUND,
                                                                                    "Service not found | Cause" +
                                                                                    "(Unsupported event [CREATE]) - " +
                                                                                    "Code(UNSUPPORTED)"));
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.FULL_PLAN);
        final String path = wsPath(MockWebSocketEvent.FULL_PLAN);
        this.setupWSClient(context, path)
            .map(ws -> wsRegister(ws, MockWebSocketEvent.FULL_PLAN.outboundAddress()))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, new JsonObject().put("err", "Not here"), b)))
            .flatMap(ignore -> setupWSClient(context, path))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, ackExpected.toJson(), b)))
            .onSuccess(ws -> wsSend(ws, MockWebSocketEvent.FULL_PLAN.inboundAddress(),
                                    EventMessage.initial(EventAction.CREATE)));
        TestHelper.LOGGER.info("SLEEP 1.5s to ensure no publish if error");
        TestHelper.sleep(1500);
        TestHelper.testComplete(async);
    }

    @Test
    public void test_wsServer_publish(TestContext context) {
        final WebSocketServerPlan plan = MockWebSocketEvent.ONLY_OUTBOUND;
        startServer(context, new HttpServerRouter().registerEventBusSocket(plan));
        final Async async = context.async(4);
        final AtomicInteger c = new AtomicInteger();
        final String path = wsPath(plan);
        final EventMessage openedMsg = createOpenedMessage(plan);
        Supplier<EventMessage> expected = () -> EventMessage.initial(EventAction.parse("TICK"),
                                                                     new JsonObject().put("c", c.get()));
        Supplier<EventMessage> req = () -> EventMessage.initial(EventAction.parse("TICK"),
                                                                new JsonObject().put("c", c.incrementAndGet()));

        this.setupWSClient(context, path)
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected.get().toJson(), b)))
            .map(ws -> wsRegister(ws, plan.outboundAddress()))
            .flatMap(ignore -> setupWSClient(context, path))
            .map(ws -> ws.handler(b -> doAssert(context, async, openedMsg, expected.get().toJson(), b)))
            .onSuccess(ws -> wsRegister(ws, plan.outboundAddress()));
        vertx().setPeriodic(1000, t -> EventBusClient.create(createSharedData(vertx()))
                                                     .fire(plan.outboundAddress(), plan.outbound().getPattern(),
                                                           req.get()));
    }

    @Test
    public void test_wsServer_register_same_path_but_diff_eventbus_addr(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebSocketEvent.ONLY_OUTBOUND,
                                                                           MockWebSocketEvent.SAME_ADDR_ONLY_OUTBOUND));
        final Async async = context.async(1);
        final String path = wsPath(MockWebSocketEvent.ONLY_OUTBOUND);
        final EventMessage openedMsg = createOpenedMessage(MockWebSocketEvent.ONLY_OUTBOUND,
                                                           MockWebSocketEvent.SAME_ADDR_ONLY_OUTBOUND);

        this.setupWSClient(context, path)
            .map(ws -> ws.handler(b -> Junit4.assertJson(context, async, openedMsg.toJson(), b)));
    }

    @Test
    public void test_wsServer_register_same_inbound_outbound(TestContext context) {
        final WebSocketServerPlan plan = MockWebSocketEvent.SAME_ADDR_INBOUND_OUTBOUND;
        startServer(context, new HttpServerRouter().registerEventBusSocket(plan));
        final Async async = context.async(4);
        final EventMessage req = EventMessage.initial(EventAction.GET_ONE);
        //TODO why flashback a request data in publish address???
        final List<EventMessage> expected = Arrays.asList(createOpenedMessage(plan),
                                                          EventMessage.success(EventAction.ACK),
                                                          EventMessage.replySuccess(EventAction.GET_ONE,
                                                                                    new JsonObject().put("data", "1")),
                                                          req);
        final AtomicInteger c = new AtomicInteger(0);
        this.setupWSClient(context, wsPath(plan))
            .map(ws -> wsRegister(ws, plan.outboundAddress()))
            .map(ws -> ws.handler(b -> {
                TestHelper.LOGGER.info("Assert time [{}][{}]", c.get(), b);
                Junit4.assertJson(context, async, expected.get(c.getAndIncrement()).toJson(), b);
            }))
            .onSuccess(ws -> wsSend(ws, plan.inboundAddress(), req));
    }

    private void assertGreeting(TestContext context, Async async, String uri) {
        client().openRequest(requestOptions().setURI(uri).setMethod(HttpMethod.GET))
                .onFailure(context::fail)
                .flatMap(HttpClientRequest::send)
                .onFailure(context::fail)
                .onSuccess(resp -> {
                    context.assertEquals(200, resp.statusCode());
                    context.assertEquals("text/plain; charset=UTF-8", resp.getHeader("content-type"));
                    resp.bodyHandler(buff -> {
                        context.assertEquals("Welcome to SockJS!\n", buff.toString());
                        testComplete(async);
                    });
                });
    }

    private void assertNotFound(TestContext context, Async async, String uri) {
        client().openWebSocket(wsOpt(requestOptions().setURI(uri)))
                .onSuccess(ws -> context.fail("Failed test should not success"))
                .onFailure(t -> {
                    try {
                        if (t.getCause() instanceof UpgradeRejectedException) {
                            context.assertEquals(404, ((UpgradeRejectedException) t.getCause()).getStatus());
                        }
                    } finally {
                        testComplete(async);
                    }
                });
    }

    private String wsPath(WebSocketServerPlan fullPlan) {
        return Urls.combinePath(httpConfig.getWebSocketConfig().getPath(), fullPlan.getPath());
    }

}
