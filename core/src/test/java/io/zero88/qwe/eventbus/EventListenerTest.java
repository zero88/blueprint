package io.zero88.qwe.eventbus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.SecurityFilter;
import io.zero88.qwe.eventbus.mock.MockAuthListener;
import io.zero88.qwe.eventbus.mock.MockEventListener;
import io.zero88.qwe.eventbus.mock.MockFailedListener;
import io.zero88.qwe.eventbus.mock.MockFutureListener;
import io.zero88.qwe.eventbus.mock.MockReceiveSendOrPublishListener;
import io.zero88.qwe.eventbus.mock.MockRx2Listener;
import io.zero88.qwe.eventbus.mock.MockWithContextListener;
import io.zero88.qwe.eventbus.mock.MockWithVariousParamsListener;
import io.zero88.qwe.exceptions.ErrorCode;

@ExtendWith(VertxExtension.class)
public class EventListenerTest {

    EventBusClient eventBusClient;
    private String address;

    @BeforeEach
    void setup(Vertx vertx) {
        address = "test.request";
        eventBusClient = EventBusClient.create(SharedDataLocalProxy.create(vertx, EventListenerTest.class.getName()));
    }

    @Test
    void test_request_error(VertxTestContext testContext) {
        final EventAction err = EventAction.parse("ERR");
        eventBusClient.register(address, new MockFailedListener())
                      .request(address, EventMessage.initial(err))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(err, msg.getPrevAction());
                          Assertions.assertTrue(msg.isError());
                          Assertions.assertEquals(ErrorCode.INVALID_ARGUMENT, msg.getError().getCode());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_success(VertxTestContext testContext) {
        eventBusClient.register(address, new MockWithVariousParamsListener())
                      .request(address, EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", "123")))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(EventAction.GET_ONE, msg.getPrevAction());
                          Assertions.assertEquals(new JsonObject().put("data", 123), msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_then_void_resp(VertxTestContext testContext) {
        eventBusClient.register(address, new MockWithVariousParamsListener())
                      .request(address, EventMessage.initial(EventAction.NOTIFY, new JsonObject().put("id", "123")))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(EventAction.NOTIFY, msg.getPrevAction());
                          Assertions.assertNull(msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_then_async_resp(VertxTestContext testContext) {
        eventBusClient.register(address, new MockFutureListener())
                      .request(address, EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", 111)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(EventAction.GET_ONE, msg.getPrevAction());
                          Assertions.assertEquals(new JsonObject().put("resp", 111), msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_then_async_void_resp(VertxTestContext testContext) {
        eventBusClient.register(address, new MockFutureListener())
                      .request(address, EventMessage.initial(EventAction.CREATE, new JsonObject().put("id", 123)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNull(msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_then_failed_future(VertxTestContext testContext) {
        final EventAction errorEvent = MockEventListener.ERROR_EVENT;
        eventBusClient.register(address, new MockFutureListener())
                      .request(address, EventMessage.initial(errorEvent, new JsonObject().put("id", 1)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(errorEvent, msg.getPrevAction());
                          Assertions.assertTrue(msg.isError());
                          Assertions.assertEquals(ErrorCode.TIMEOUT_ERROR, msg.getError().getCode());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_unsupported_action(VertxTestContext testContext) {
        final EventAction any = EventAction.parse("ANY");
        eventBusClient.register(address, new MockFutureListener())
                      .request(address, EventMessage.initial(any, new JsonObject().put("id", 1)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(any, msg.getPrevAction());
                          Assertions.assertTrue(msg.isError());
                          Assertions.assertEquals(ErrorCode.SERVICE_NOT_FOUND, msg.getError().getCode());
                          Assertions.assertEquals(
                              "Service not found | Cause(Unsupported event [ANY]) - Code(UNSUPPORTED)",
                              msg.getError().getMessage());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_with_context_then_async_resp(VertxTestContext testContext) {
        final JsonObject req = new JsonObject().put("tik", 123);
        eventBusClient.register(address, new MockWithContextListener());
        eventBusClient.request(address, EventMessage.initial(EventAction.parse("EB"), req))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertEquals(new JsonObject().put("received", req), msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_request_with_context_then_use_future_then_async_resp(VertxTestContext testContext) {
        final JsonObject req = new JsonObject().put("tik", 123);
        eventBusClient.register(address, new MockWithContextListener())
                      .request(address, EventMessage.initial(EventAction.parse("INVOKE"), req))
                      .onSuccess(msg -> testContext.verify(() -> {
                          System.out.println(msg.toJson());
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNotNull(msg.getData());
                          JsonHelper.assertJson(new JsonObject("{\"path\":\"/tmp/qwe-\",\"body\":{\"tik\":123}}"),
                                                msg.getData(), Customization.customization("path",
                                                                                           (o1, o2) -> ((String) o1).startsWith(
                                                                                               (String) o2)));
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_send(VertxTestContext testContext) {
        final String address = "test.send";
        final Checkpoint checkpoint = testContext.checkpoint();
        eventBusClient.register(address, new MockReceiveSendOrPublishListener("id1", checkpoint))
                      .send(address, EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", 111)));
    }

    @Test
    void test_publish(VertxTestContext testContext) {
        final String address = "test.publish";
        final Checkpoint checkpoint = testContext.checkpoint(2);
        eventBusClient.register(address, new MockReceiveSendOrPublishListener("id1", checkpoint))
                      .register(address, new MockReceiveSendOrPublishListener("id2", checkpoint))
                      .publish(address, EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", 111)));
    }

    @Test
    void test_rx2_single(VertxTestContext testContext) {
        final String address = "test.single";
        final EventAction action = EventAction.parse("SINGLE");
        eventBusClient.register(address, new MockRx2Listener())
                      .request(address, EventMessage.initial(action, new JsonObject().put("id", 111)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          System.out.println(msg.toJson());
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(action, msg.getPrevAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNotNull(msg.getData());
                          JsonHelper.assertJson(new JsonObject("{\"resp\":111}"), msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_rx2_maybe(VertxTestContext testContext) {
        final String address = "test.maybe";
        final EventAction action = EventAction.parse("MAYBE");
        eventBusClient.register(address, new MockRx2Listener())
                      .request(address, EventMessage.initial(action, new JsonObject().put("id", 111)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          System.out.println(msg.toJson());
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(action, msg.getPrevAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNull(msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_rx2_completable(VertxTestContext testContext) {
        final String address = "test.completable";
        final EventAction action = EventAction.parse("COMPLETABLE");
        eventBusClient.register(address, new MockRx2Listener())
                      .request(address, EventMessage.initial(action, new JsonObject().put("id", 111)))
                      .onSuccess(msg -> testContext.verify(() -> {
                          System.out.println(msg.toJson());
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(action, msg.getPrevAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNull(msg.getData());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_has_auth_but_not_security_filter_should_failed(VertxTestContext testContext) {
        final String address = "test.sec.failed";
        final EventAction check = EventAction.parse("CHECK");
        eventBusClient.register(address, new MockAuthListener())
                      .request(address, EventMessage.initial(check))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(check, msg.getPrevAction());
                          Assertions.assertTrue(msg.isError());
                          Assertions.assertEquals(ErrorCode.SECURITY_ERROR, msg.getError().getCode());
                          Assertions.assertEquals("Missing security filter", msg.getError().getMessage());
                          testContext.completeNow();
                      }));
    }

    @Test
    void test_eventbus_with_authn_should_success(VertxTestContext testContext) {
        final String address = "test.sec.authn";
        final EventAction check = EventAction.parse("CHECK");
        final SecurityFilter securityFilter = (sharedData, userInfo, reqDefinition) -> Future.succeededFuture();
        eventBusClient.register(address, new MockAuthListener(securityFilter))
                      .request(address, EventMessage.initial(check))
                      .onSuccess(msg -> testContext.verify(() -> {
                          Assertions.assertEquals(EventAction.REPLY, msg.getAction());
                          Assertions.assertEquals(check, msg.getPrevAction());
                          Assertions.assertTrue(msg.isSuccess());
                          Assertions.assertNotNull(msg.getData());
                          Assertions.assertEquals(new JsonObject().put("data", "success"), msg.getData());
                          testContext.completeNow();
                      }));
    }

}
