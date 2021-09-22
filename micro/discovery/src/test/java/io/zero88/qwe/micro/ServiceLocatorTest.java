package io.zero88.qwe.micro;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.JsonHelper.Junit5;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.EventStatus;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.micro.filter.ByPredicateFactory;
import io.zero88.qwe.micro.filter.ServiceFilterParam;
import io.zero88.qwe.micro.transfomer.RecordTransformer.ViewType;

public class ServiceLocatorTest extends BaseDiscoveryPluginTest {

    @Test
    public void test_get_not_found(VertxTestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.SERVICE_NOT_FOUND.code())
                                              .put("message", "Not found service by given parameters " +
                                                              "[{\"by\":\"name\",\"identifier\":\"not.found\"}]");
        queryOneButFailed(context, RequestData.builder()
                                              .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "not.found"))
                                              .filter(new JsonObject().put(ServiceFilterParam.BY, "name"))
                                              .build(), expected);
    }

    @Test
    public void test_get_by_name(VertxTestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"endpoint\":\"ea1\",\"name\":\"er1\",\"status\":\"UP\",\"paths\":[" +
            "{\"method\":\"POST\",\"path\":\"/path\"},{\"method\":\"PUT\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"PATCH\",\"path\":\"/path/:param\"},{\"method\":\"GET\",\"path\":\"/path\"}," +
            "{\"method\":\"GET\",\"path\":\"/path/:param\"},{\"method\":\"DELETE\",\"path\":\"/path/:param\"}]}");
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "er1"))
                                               .filter(new JsonObject().put(ServiceFilterParam.BY, "name"))
                                               .build();
        discovery.register(RecordHelper.create("er1", "ea1", EventMethodDefinition.createDefault("/path", "/:param")),
                           RecordHelper.create("er2", "ea2", EventMethodDefinition.createDefault("/p", "/:r")))
                 .flatMap(r -> invokeThenAssert(context, reqData, EventAction.GET_ONE, expected));
    }

    @Test
    public void test_get_by_group(VertxTestContext context) {
        JsonObject value = new JsonObject("{\"endpoint\":\"https://1.1.1.1:1234/api\",\"name\":\"http.test\"," +
                                          "\"type\":\"http-endpoint\",\"status\":\"UP\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "http"))
                                         .filter(new JsonObject().put(ServiceFilterParam.BY, "group"))
                                         .build();
        discovery.register(RecordHelper.create("http.test", new HttpLocation().setHost("1.1.1.1")
                                                                              .setPort(1234)
                                                                              .setSsl(true)
                                                                              .setRoot("/api")))
                 .flatMap(r -> invokeThenAssert(context, reqData, EventAction.GET_ONE, value));
    }

    @Test
    public void test_get_by_path(VertxTestContext context) {
        JsonObject expected = new JsonObject(
            "{\"endpoint\":\"ea2\",\"paths\":[{\"method\":\"POST\",\"path\":\"/xy\"},{\"method\":\"PUT\"," +
            "\"path\":\"/xy/:z\"},{\"method\":\"PATCH\",\"path\":\"/xy/:z\"},{\"method\":\"GET\",\"path\":\"/xy\"}," +
            "{\"method\":\"GET\",\"path\":\"/xy/:z\"},{\"method\":\"DELETE\",\"path\":\"/xy/:z\"}],\"name\":\"er2\"," +
            "\"status\":\"UP\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "/xy"))
                                         .filter(new JsonObject().put(ServiceFilterParam.BY, ByPredicateFactory.BY_PATH)
                                                                 .put(RequestFilter.PRETTY, true))
                                         .build();
        discovery.register(RecordHelper.create("er2", "ea2", EventMethodDefinition.createDefault("/xy", "/:z")))
                 .flatMap(r -> invokeThenAssert(context, reqData, EventAction.GET_ONE, expected));
    }

    @Test
    public void test_get_by_name_with_technical_view(VertxTestContext context) {
        JsonObject value = new JsonObject(
            "{\"name\":\"er1\",\"status\":\"UP\",\"endpoint\":\"ea1\",\"paths\":[{\"action\":\"GET_LIST\"," +
            "\"method\":\"GET\",\"capturePath\":\"/path\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/path\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\"," +
            "\"method\":\"GET\",\"capturePath\":\"/path/:param\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
            "\"capturePath\":\"/path/:param\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\",\"capturePath\":\"/path/:param\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"REMOVE\"," +
            "\"method\":\"DELETE\",\"capturePath\":\"/path/:param\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "er1"))
                                         .filter(new JsonObject().put(ServiceFilterParam.BY, "name")
                                                                 .put(ServiceFilterParam.VIEW, ViewType.TECHNICAL))
                                         .build();
        discovery.register(RecordHelper.create("er1", "ea1", EventMethodDefinition.createDefault("/path", "/:param")))
                 .flatMap(r -> invokeThenAssert(context, reqData, EventAction.GET_ONE, value));
    }

    @Test
    public void test_list_by_group(VertxTestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"apis\":[{\"name\":\"g.er1\",\"status\":\"UP\",\"endpoint\":\"ea1\"," +
            "\"paths\":[{\"method\":\"POST\",\"path\":\"/path\"},{\"method\":\"PUT\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"PATCH\",\"path\":\"/path/:param\"},{\"method\":\"GET\",\"path\":\"/path\"}," +
            "{\"method\":\"GET\",\"path\":\"/path/:param\"},{\"method\":\"DELETE\",\"path\":\"/path/:param\"}]}," +
            "{\"name\":\"g.er2\",\"status\":\"UP\",\"endpoint\":\"ea2\"," +
            "\"paths\":[{\"method\":\"POST\",\"path\":\"/xy\"},{\"method\":\"PUT\",\"path\":\"/xy/:z\"}," +
            "{\"method\":\"PATCH\",\"path\":\"/xy/:z\"},{\"method\":\"GET\",\"path\":\"/xy\"},{\"method\":\"GET\"," +
            "\"path\":\"/xy/:z\"},{\"method\":\"DELETE\",\"path\":\"/xy/:z\"}]}]}");
        final RequestData reqData = RequestData.builder()
                                               .filter(new JsonObject().put(ServiceFilterParam.BY, "group")
                                                                       .put(ServiceFilterParam.IDENTIFIER, "g"))
                                               .build();
        discovery.register(RecordHelper.create("g.er1", "ea1", EventMethodDefinition.createDefault("/path", "/:param")),
                           RecordHelper.create("g.er2", "ea2", EventMethodDefinition.createDefault("/xy", "/:z")))
                 .flatMap(cf -> invokeThenAssert(context, reqData, EventAction.GET_LIST, expected));
    }

    @Test
    public void test_get_one_but_many_record_should_failed(VertxTestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "g"))
                                               .filter(new JsonObject().put(ServiceFilterParam.BY, "group"))
                                               .build();
        final JsonObject failed = new JsonObject().put("code", "SERVICE_ERROR")
                                                  .put("message", "Service conflict | Cause(More than one service by " +
                                                                  "given parameters [{\"by\":\"group\"," +
                                                                  "\"identifier\":\"g\"}]) - Code(CONFLICT_ERROR)");
        discovery.register(RecordHelper.create("g.er1", "ea1", EventMethodDefinition.createDefault("/a", "/:b")),
                           RecordHelper.create("g.er2", "ea2", EventMethodDefinition.createDefault("/x", "/:y")))
                 .flatMap(cf -> queryOneButFailed(context, reqData, failed));
    }

    @Test
    public void test_get_by_path_combine_name(VertxTestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"endpoint\":\"ea2\",\"paths\":[{\"method\":\"POST\",\"path\":\"/a\"},{\"method\":\"PUT\"," +
            "\"path\":\"/a/:c\"},{\"method\":\"PATCH\",\"path\":\"/a/:c\"},{\"method\":\"GET\",\"path\":\"/a\"}," +
            "{\"method\":\"GET\",\"path\":\"/a/:c\"},{\"method\":\"DELETE\",\"path\":\"/a/:c\"}],\"name\":\"api2\"," +
            "\"status\":\"UP\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER, "/a"))
                                         .filter(new JsonObject().put(ServiceFilterParam.BY, ByPredicateFactory.BY_PATH)
                                                                 .put(ServiceFilterParam.NAME, "api1")
                                                                 .put(RequestFilter.PRETTY, true))
                                         .build();
        discovery.register(RecordHelper.create("api1", "ea1", EventMethodDefinition.createDefault("/a/b", "/:c")),
                           RecordHelper.create("api2", "ea2", EventMethodDefinition.createDefault("/a", "/:c")))
                 .flatMap(cf -> invokeThenAssert(context, reqData, EventAction.GET_ONE, expected));
    }

    @Test
    public void test_update_but_no_registration(VertxTestContext context) {
        final JsonObject failed = new JsonObject().put("code", "INVALID_ARGUMENT")
                                                  .put("message", "Missing record identifier[registration]");
        discovery.register(RecordHelper.create("g.er1", "ea1", EventMethodDefinition.createDefault("/a", "/:b")))
                 .flatMap(cf -> queryOneButFailed(context, RequestData.empty(), failed));
    }

    @Test
    public void test_unregister_by_registration(VertxTestContext context) {
        Checkpoint cp = context.checkpoint(2);
        discovery.register(RecordHelper.create("g.er1", "r1", EventMethodDefinition.createDefault("/a", "/:b")))
                 .map(r -> new JsonObject().put(ServiceFilterParam.IDENTIFIER, r.getRegistration()))
                 .flatMap(f -> invokeThenAssert(context, RequestData.builder().body(f).build(), EventAction.REMOVE,
                                                new JsonObject().put("filter", f)
                                                                .put("total", 1)
                                                                .put("removed", 1)
                                                                .put("errors", new JsonArray())))
                 .onSuccess(c -> cp.flag())
                 .flatMap(r -> ebClient.request(getGatewayConfig().getIndexAddress(),
                                                EventMessage.initial(EventAction.GET_LIST, RequestData.empty())))
                 .onSuccess(msg -> context.verify(() -> {
                     Assertions.assertNotNull(msg.getData());
                     Assertions.assertEquals(0, msg.getData().getJsonArray("apis").size());
                 }))
                 .onSuccess(msg -> cp.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_unregister_many(VertxTestContext context) {
        RequestData req = RequestData.builder()
                                     .filter(new JsonObject().put(ServiceFilterParam.BY, ByPredicateFactory.BY_ENDPOINT)
                                                             .put(ServiceFilterParam.IDENTIFIER, "r1"))
                                     .build();
        JsonObject expected = new JsonObject().put("filter", req.filter())
                                              .put("total", 2)
                                              .put("removed", 2)
                                              .put("errors", new JsonArray());
        Record r1 = RecordHelper.create("g.er1", "r1", EventMethodDefinition.createDefault("/a", "/:b"));
        Record r2 = RecordHelper.create("g.er2", "r1", EventMethodDefinition.createDefault("/ab", "/:b"));
        Record r3 = RecordHelper.create("http.test.1",
                                        new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/abc"));
        Checkpoint cp = context.checkpoint(2);
        discovery.register(r1, r2, r3)
                 .flatMap(cf -> invokeThenAssert(context, req, EventAction.BATCH_DELETE, expected))
                 .onSuccess(c -> cp.flag())
                 .flatMap(r -> ebClient.request(getGatewayConfig().getIndexAddress(),
                                                EventMessage.initial(EventAction.GET_LIST, RequestData.empty())))
                 .onSuccess(msg -> context.verify(() -> {
                     Assertions.assertNotNull(msg.getData());
                     Assertions.assertEquals(1, msg.getData().getJsonArray("apis").size());
                 }))
                 .onSuccess(msg -> cp.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_batch_update(VertxTestContext context) {
        final Record rec1 = RecordHelper.create("http.test.1",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/abc"));
        final Record rec2 = RecordHelper.create("http.test.2",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/xyz"));
        final JsonObject expected = new JsonObject(
            "{\"apis\":[{\"name\":\"http.test.1\",\"type\":\"http-endpoint\",\"status\":\"UP\"," +
            "\"endpoint\":\"http://127.0.0.1:1111/abc\"}," +
            "{\"name\":\"http.test.2\",\"type\":\"http-endpoint\",\"status\":\"UP\"," +
            "\"endpoint\":\"http://127.0.0.1:1111/xyz\"}]}");
        RequestFilter filter = new RequestFilter().put(ServiceFilterParam.IDENTIFIER, "http.test")
                                                  .put(ServiceFilterParam.BY, ByPredicateFactory.BY_GROUP);
        RequestData reqData = RequestData.builder()
                                         .filter(filter)
                                         .body(new JsonObject().put("location", new JsonObject().put("port", 1111)))
                                         .build();
        Checkpoint cp = context.checkpoint(2);
        discovery.register(rec1, rec2)
                 .flatMap(rr -> invokeThenAssert(context, reqData, EventAction.BATCH_UPDATE, expected))
                 .onSuccess(c -> cp.flag())
                 .flatMap(r -> ebClient.request(getGatewayConfig().getIndexAddress(),
                                                EventMessage.initial(EventAction.GET_LIST, RequestData.empty())))
                 .onSuccess(msg -> context.verify(() -> {
                     Assertions.assertNotNull(msg.getData());
                     Assertions.assertEquals(2, msg.getData().getJsonArray("apis").size());
                 }))
                 .onSuccess(msg -> cp.flag())
                 .onFailure(context::failNow);
    }

    protected Future<EventMessage> invokeThenAssert(VertxTestContext context, RequestData reqData, EventAction action,
                                                    JsonObject expected) {
        return test(context, reqData, action, expected, EventStatus.SUCCESS);
    }

    protected Future<EventMessage> queryOneButFailed(VertxTestContext context, RequestData reqData,
                                                     JsonObject expected) {
        return test(context, reqData, EventAction.GET_ONE, expected, EventStatus.FAILED);
    }

    protected Future<EventMessage> test(VertxTestContext ctx, RequestData reqData, EventAction action,
                                        JsonObject expected, EventStatus status) {
        final Checkpoint async = ctx.checkpoint();
        final String dataKey = status == EventStatus.FAILED ? "error" : "data";
        final JsonObject resp = new JsonObject().put("status", status)
                                                .put("action", EventAction.REPLY.action())
                                                .put("prevAction", action.action())
                                                .put(dataKey, expected);
        return ebClient.request(getGatewayConfig().getIndexAddress(), EventMessage.initial(action, reqData))
                       .onSuccess(m -> Junit5.assertJson(ctx, async, resp, m.toJson(), JSONCompareMode.NON_EXTENSIBLE))
                       .onSuccess(c -> ctx.completeNow());
    }

}
