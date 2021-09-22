package io.zero88.qwe.rpc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.JsonHelper.Junit4;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.micro.BaseDiscoveryPluginTest;
import io.zero88.qwe.micro.GatewayHeaders;
import io.zero88.qwe.micro.mock.MockServiceListener;
import io.zero88.qwe.rpc.mock.MockServiceInvoker;

@Disabled
public class GatewayServiceInvokerTest extends BaseDiscoveryPluginTest {

    public static final String EVENT_RECORD_1 = "event.record.1";
    public static final String EVENT_ADDRESS_1 = "event.address.1";

    @BeforeEach
    public void tearUp(Vertx vertx, VertxTestContext ctx) {
        super.tearUp(vertx, ctx);
        ebClient.register(EVENT_ADDRESS_1, new MockServiceListener());
    }

    @Test
    public void test_get_not_found_service(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(getGatewayConfig().getIndexAddress(), ebClient,
                                                            EVENT_RECORD_1 + "...");
        invoker.execute(EventAction.CREATE, RequestData.builder().build()).onSuccess(d -> {
            System.out.println(d);
            context.fail("Expected failed");
        }).onFailure(t -> {
            context.assertTrue(t instanceof QWEException);
            assert t instanceof QWEException;
            QWEException e = (QWEException) t;
            context.assertEquals(ErrorCode.SERVICE_NOT_FOUND, e.errorCode());
            context.assertEquals(
                invoker.serviceLabel() + " is not found or out of service. Try again later | Error: SERVICE_NOT_FOUND",
                e.getMessage());
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_get_not_found_action(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(getGatewayConfig().getIndexAddress(), ebClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UNKNOWN, RequestData.builder().build()).onSuccess(d -> {
            System.out.println(d);
            context.fail("Expected failed");
        }).onFailure(t -> {
            context.assertTrue(t instanceof QWEException);
            assert t instanceof QWEException;
            QWEException e = (QWEException) t;
            context.assertEquals(ErrorCode.SERVICE_NOT_FOUND, e.errorCode());
            context.assertEquals(
                invoker.serviceLabel() + " is not found or out of service. Try again later | Error: SERVICE_NOT_FOUND",
                e.getMessage());
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_execute_service_failed(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(getGatewayConfig().getIndexAddress(), ebClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UPDATE, RequestData.builder().build())
               .onSuccess(d -> Junit4.assertJson(context, async,
                                                 new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT.code())
                                                                 .put("message", "hey"), d))
               .onFailure(context::fail);
    }

    @Test
    public void test_execute_service_success(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(getGatewayConfig().getIndexAddress(), ebClient,
                                                            EVENT_RECORD_1);
        final JsonObject expected = new JsonObject().put(GatewayHeaders.X_REQUEST_BY,
                                                         "service/" + invoker.requester())
                                                    .put("action", EventAction.CREATE.action());
        invoker.execute(EventAction.CREATE, RequestData.builder().build())
               .onSuccess(d -> Junit4.assertJson(context, async, expected, d))
               .onFailure(context::fail);
    }

}
