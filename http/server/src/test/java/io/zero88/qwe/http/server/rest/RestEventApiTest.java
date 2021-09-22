package io.zero88.qwe.http.server.rest;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.zero88.qwe.http.server.RestApiTestHelper;
import io.zero88.qwe.http.server.mock.MockEventErrorListener;
import io.zero88.qwe.http.server.mock.MockEventSuccessListener;
import io.zero88.qwe.http.server.mock.MockRestEventApi;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RestEventApiTest extends HttpServerPluginTestBase implements RestApiTestHelper {

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Test
    public void test_api_eventbus_not_found(TestContext context) {
        String path = "/api/test/event";
        JsonObject expected = notFoundResponse(httpConfig.getPort(), path);
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.GET, path, 404, expected);
    }

    @Test
    public void test_api_eventbus_error_unexpected(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventErrorListener());
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("code", ErrorCode.UNKNOWN_ERROR.code())
                                              .put("message", "UNKNOWN_ERROR | Cause(xxx)");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.GET, path, 500, expected);
    }

    @Test
    public void test_api_eventbus_error_from_server(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventErrorListener());
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("code", ErrorCode.ENGINE_ERROR.code()).put("message", "Engine error");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.POST, path, 500, expected);
    }

    @Test
    public void test_api_eventbus_error_from_user(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventErrorListener());
        String path = "/api/test/events/:event_id";
        JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT.code()).put("message", "invalid");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.PUT, path, 400, expected);
    }

    @Test
    public void test_api_eventbus_no_reply(TestContext context) {
        String path = "/api/test/events/:event_id";
        JsonObject expected = new JsonObject().put("code", ErrorCode.SERVICE_UNAVAILABLE)
                                              .put("message", "No response");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.GET, path, 503, expected);
    }

    @Test
    public void test_api_eventbus_success_data_list(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventSuccessListener());
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("data", Arrays.asList("1", "2", "3"));
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_other(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventSuccessListener());
        String path = "/api/test/events/1";
        JsonObject expected = new JsonObject().put("data", 1);
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_json(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventSuccessListener());
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("create", "success");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.POST, path, 201, expected);
    }

    @Test
    public void test_api_eventbus_success_data_single(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventSuccessListener());
        String path = "/api/test/events/1";
        JsonObject expected = new JsonObject().put("data", "success");
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.PUT, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_single_json(TestContext context) {
        EventBusClient.create(createSharedData(vertx)).register("http.server.test", new MockEventSuccessListener());
        String path = "/api/test/events/1";
        JsonObject expected = new JsonObject().put("patch", "success").put("event_id", 1);
        startServer(context, new HttpServerRouter().registerEventBusApi(MockRestEventApi.class));
        sendToApiThenAssert(context, HttpMethod.PATCH, path, 200, expected);
    }

}
