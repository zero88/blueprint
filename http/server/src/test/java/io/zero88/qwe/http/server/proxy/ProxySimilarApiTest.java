package io.zero88.qwe.http.server.proxy;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.zero88.qwe.http.server.proxy.mock.SimilarApiService;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

public class ProxySimilarApiTest extends ProxyServiceTestBase {

    @Override
    @SuppressWarnings("unchecked")
    protected SimilarApiService service() {
        return new SimilarApiService();
    }

    @Test
    public void test_get_definition_by_gateway(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"apis\":[{\"name\":\"ems-5\",\"status\":\"UP\",\"endpoint\":\"test.SimilarApiService.1\"," +
            "\"paths\":[{\"method\":\"GET\",\"path\":\"/client/:cId/site/:sId\"},{\"method\":\"DELETE\"," +
            "\"path\":\"/client/:cId/site/:sId\"},{\"method\":\"GET\",\"path\":\"/client/:cId/site\"}," +
            "{\"method\":\"POST\",\"path\":\"/client/:cId/site\"},{\"method\":\"PATCH\"," +
            "\"path\":\"/client/:cId/site/:sId\"},{\"method\":\"PUT\",\"path\":\"/client/:cId/site/:sId\"}]}," +
            "{\"name\":\"ems-5\",\"status\":\"UP\",\"endpoint\":\"test.SimilarApiService.2\"," +
            "\"paths\":[{\"method\":\"GET\",\"path\":\"/client/:cId/site/:sId/product/:pId\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/client/:cId/site/:sId/product/:pId\"},{\"method\":\"GET\"," +
            "\"path\":\"/client/:cId/site/:sId/product\"},{\"method\":\"POST\"," +
            "\"path\":\"/client/:cId/site/:sId/product\"},{\"method\":\"PATCH\"," +
            "\"path\":\"/client/:cId/site/:sId/product/:pId\"}," +
            "{\"method\":\"PUT\",\"path\":\"/client/:cId/site/:sId/product/:pId\"}]}]}");
        sendToApiThenAssert(context, HttpMethod.GET, "/gw/index", 200, expected, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_list_from_site(TestContext context) {
        sendToApiThenAssert(context, HttpMethod.GET, "/api/s/client/123/site", 200,
                            new JsonObject().put("from", "GET_LIST From site [123]"));
    }

    @Test
    public void test_get_list_from_product(TestContext context) {
        sendToApiThenAssert(context, HttpMethod.GET, "/api/s/client/123/site/abc/product", 200,
                            new JsonObject().put("from", "GET_LIST From product"));
    }

}
