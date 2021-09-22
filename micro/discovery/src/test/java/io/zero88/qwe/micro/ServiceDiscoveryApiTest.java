package io.zero88.qwe.micro;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.micro.filter.ByPredicateFactory;
import io.zero88.qwe.micro.filter.ServiceFilterParam;
import io.zero88.qwe.micro.filter.ServiceStatusPredicateFactory;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.micro.mock.MockEventBusService;
import io.zero88.qwe.micro.mock.MockServiceListener;

public class ServiceDiscoveryApiTest extends BaseDiscoveryPluginTest {

    @Test
    public void test_register_eventbus_service(VertxTestContext context) {
        final JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"address1\"},\"metadata\":{\"service" +
                                                   ".interface\":\"io.zero88.qwe.micro.mock.MockEventBusService\"}," +
                                                   "\"name\":\"test\",\"status\":\"UP\"," +
                                                   "\"type\":\"eventbus-service-proxy\"}");
        final Record rec = EventBusService.createRecord("test", "address1", MockEventBusService.class);
        registerThenAssert(context, expected, rec);
    }

    @Test
    public void test_register_http(VertxTestContext context) {
        final JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"http://127.0.0.1:1111/xyz\"," +
                                                   "\"host\":\"127.0.0.1\",\"port\":1111,\"root\":\"/xyz\"," +
                                                   "\"ssl\":false},\"metadata\":{\"meta\":\"test\"},\"name\":\"http" +
                                                   ".test\",\"status\":\"UP\",\"type\":\"http-endpoint\"}");
        final Record rec = RecordHelper.create("http.test",
                                               new HttpLocation().setHost("127.0.0.1").setPort(1111).setRoot("/xyz"),
                                               new JsonObject().put("meta", "test"));
        registerThenAssert(context, expected, rec);
    }

    @Test
    public void test_register_eventMessage(VertxTestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"location\":{\"regexPath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/abc\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc/:ka\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/abc/:ka\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/abc/:ka\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/abc/:ka\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}],\"endpoint\":\"address.1\"},\"metadata\":{}," +
            "\"name\":\"event-message\",\"status\":\"UP\",\"type\":\"eventbus-2-http\"}");
        final Record rec = RecordHelper.create("event-message", "address.1",
                                               EventMethodDefinition.createDefault("/abc", "/:ka"));
        registerThenAssert(context, expected, rec);
    }

    @Test
    public void test_register_similar_eb_2_http(VertxTestContext context) {
        final Checkpoint checkpoint = context.checkpoint();
        final RequestFilter filter = new RequestFilter().put(ServiceFilterParam.IDENTIFIER, "/c/123/s/abc/p")
                                                        .put(ServiceFilterParam.BY, ByPredicateFactory.BY_PATH)
                                                        .put(ServiceFilterParam.NAME, "ems")
                                                        .put(ServiceFilterParam.TYPE, "eventbus-2-http");
        final JsonObject expected = new JsonObject(
            "{\"location\":{\"regexPath\":\"/c/[^/]+/s/[^/]+/p\",\"mapping\":[{\"action\":\"GET_LIST\"," +
            "\"method\":\"GET\",\"capturePath\":\"/c/:cId/s/:sId/p\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"CREATE\",\"method\":\"POST\"," +
            "\"capturePath\":\"/c/:cId/s/:sId/p\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/c/:cId/s/:sId/p/:pId\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"UPDATE\"," +
            "\"method\":\"PUT\",\"capturePath\":\"/c/:cId/s/:sId/p/:pId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/c/:cId/s/:sId/p/:pId\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/c/:cId/s/:sId/p/:pId\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}],\"endpoint\":\"addr2\"}," +
            "\"metadata\":{},\"name\":\"ems\",\"registration\":\"ignore\"," +
            "\"status\":\"UP\",\"type\":\"eventbus-2-http\"}");
        discovery.register(
            RecordHelper.create("ems", "addr1", EventMethodDefinition.createDefault("/c/:cId/s", "/:sId")),
            RecordHelper.create("ems", "addr2", EventMethodDefinition.createDefault("/c/:cId/s/:sId/p", "/:pId")))
                 .flatMap(ignore -> discovery.findOne(filter))
                 .onSuccess(record -> context.verify(
                     () -> JsonHelper.assertJson(expected, record.toJson(), JsonHelper.ignore("registration"))))
                 .onSuccess(event -> checkpoint.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_register_many(VertxTestContext context) {
        final Record rec1 = RecordHelper.create("http.test.1",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/xyz"));
        final Record rec2 = RecordHelper.create("http.test.2",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/abc"));

        final Checkpoint checkpoint = context.checkpoint();
        RequestFilter filter = new RequestFilter().put(ServiceFilterParam.IDENTIFIER, "http.test")
                                                  .put(ServiceFilterParam.BY, ByPredicateFactory.BY_GROUP)
                                                  .put(ServiceFilterParam.STATUS, ServiceStatusPredicateFactory.ALL);
        discovery.register(rec1, rec2)
                 .flatMap(cf -> discovery.batchUpdate(filter, new JsonObject().put("status", "DOWN")))
                 .onSuccess(cf -> Assertions.assertEquals(2, cf.size()))
                 .flatMap(cf -> discovery.findMany(filter))
                 .onSuccess(records -> context.verify(() -> {
                     Assertions.assertEquals(2, records.size());
                     Assertions.assertEquals(Status.DOWN, records.get(0).getStatus());
                     Assertions.assertEquals(Status.DOWN, records.get(1).getStatus());
                 }))
                 .onSuccess(event -> checkpoint.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_update_without_id_should_failed() {
        Record r = RecordHelper.create("http.test.1",
                                       new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/xyz"));
        Assertions.assertEquals("Missing record identifier[registration]",
                                Assertions.assertThrows(IllegalArgumentException.class, () -> discovery.update(r))
                                          .getMessage());
    }

    @Test
    public void test_update(VertxTestContext context) {
        final Record rec1 = RecordHelper.create("http.test.1",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/xyz"));
        final Checkpoint checkpoint = context.checkpoint();
        RequestFilter filter = new RequestFilter().put(ServiceFilterParam.IDENTIFIER, "http.test.1")
                                                  .put(ServiceFilterParam.BY, ByPredicateFactory.BY_NAME)
                                                  .put(ServiceFilterParam.STATUS, ServiceStatusPredicateFactory.ALL);
        discovery.register(rec1)
                 .map(r -> new Record(new JsonObject().put("status", "DOWN").put("registration", r.getRegistration())))
                 .flatMap(discovery::update)
                 .flatMap(cf -> discovery.findOne(filter))
                 .onSuccess(records -> context.verify(() -> Assertions.assertEquals(Status.DOWN, records.getStatus())))
                 .onSuccess(event -> checkpoint.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_unregister_by_id(VertxTestContext context) {
        final Record rec1 = RecordHelper.create("http.test.1",
                                                new HttpLocation().setHost("127.0.0.1").setPort(1234).setRoot("/xyz"));
        final Checkpoint checkpoint = context.checkpoint();
        final JsonObject expected = new JsonObject().put("total", 1)
                                                    .put("removed", 1)
                                                    .put("filter", new JsonObject().put("identifier", "ignore"))
                                                    .put("errors", new JsonArray());
        discovery.register(rec1)
                 .map(r -> new RequestFilter().put(ServiceFilterParam.IDENTIFIER, r.getRegistration()))
                 .flatMap(filter -> discovery.unregister(filter))
                 .onSuccess(json -> context.verify(
                     () -> JsonHelper.assertJson(expected, json, JsonHelper.ignore("filter.identifier"))))
                 .onSuccess(event -> checkpoint.flag())
                 .onFailure(context::failNow);
    }

    @Test
    public void test_execute_event_http(VertxTestContext context) {
        ebClient.register("ar1", new MockServiceListener());
        RequestFilter filter = new RequestFilter().put(ServiceFilterParam.BY, ByPredicateFactory.BY_NAME)
                                                  .put(ServiceFilterParam.IDENTIFIER, "g.er1")
                                                  .put(ServiceFilterParam.ACTION, EventAction.CREATE.action());
        RequestData reqData = RequestData.builder()
                                         .headers(new JsonObject().put(GatewayHeaders.X_REQUEST_BY, "test"))
                                         .build();
        JsonObject expected = new JsonObject("{\"headers\":{\"status\":\"SUCCESS\",\"action\":\"REPLY\"," +
                                             "\"prevAction\":\"CREATE\"},\"body\":{\"X-Request-By\":\"test\"," +
                                             "\"action\":\"CREATE\"}}");
        Checkpoint cp = context.checkpoint();
        discovery.register(RecordHelper.create("g.er1", "ar1", EventMethodDefinition.createDefault("/a", "/:b")))
                 .flatMap(r -> discovery.execute(filter, reqData))
                 .onSuccess(resp -> context.verify(() -> JsonHelper.assertJson(expected, resp.toJson())))
                 .onSuccess(v -> cp.flag())
                 .onFailure(context::failNow);
    }

    private void registerThenAssert(VertxTestContext context, JsonObject expected, Record rec) {
        final Checkpoint checkpoint = context.checkpoint();
        discovery.register(rec)
                 .flatMap(r1 -> discovery.findOne(
                     new RequestFilter().put(ServiceFilterParam.IDENTIFIER, r1.getRegistration()))
                                         .onSuccess(r2 -> context.verify(() -> {
                                             Assertions.assertEquals(r2, r1);
                                             JsonHelper.assertJson(expected, r1.toJson(),
                                                                   JsonHelper.ignore("registration"));
                                         })))
                 .onSuccess(msg -> checkpoint.flag())
                 .onFailure(context::failNow);
    }

}
