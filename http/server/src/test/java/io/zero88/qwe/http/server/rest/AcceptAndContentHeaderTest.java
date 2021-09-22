package io.zero88.qwe.http.server.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.http.HttpUtils;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.http.server.RestApiTestHelper;
import io.zero88.qwe.http.server.mock.MockRestAPI;

@RunWith(VertxUnitRunner.class)
//TODO FIX `javax.ws.rs`
public class AcceptAndContentHeaderTest extends HttpServerPluginTestBase implements RestApiTestHelper {

    private static final JsonObject SUCCESS_EXPECTED = new JsonObject("{\"abc\":\"xxx\"}");
    private static final String PATH = "/api/test";
    private static final String XML_CONTENT_TYPE = "/application/xml";

    @Before
    public void before(TestContext context) {
        super.before(context);
        startServer(context, new HttpServerRouter().registerApi(MockRestAPI.class));
    }

    @Test
    public void test_accept_json(TestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpHeaders.ACCEPT.toString(),
                                                                             HttpUtils.JSON_CONTENT_TYPE))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 200, SUCCESS_EXPECTED);
    }

    @Test
    public void test_accept_json_utf8(TestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpHeaders.ACCEPT.toString(),
                                                                             HttpUtils.JSON_UTF8_CONTENT_TYPE))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 200, SUCCESS_EXPECTED);
    }

    @Test
    public void test_accept_xml_should_failed(TestContext context) {
        JsonObject expected = notFoundResponse(httpConfig.getPort(), PATH);
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpHeaders.ACCEPT.toString(),
                                                                             XML_CONTENT_TYPE))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 404, expected);
    }

    @Test
    public void test_accept_none(TestContext context) {
        sendToApiThenAssert(context, HttpMethod.GET, PATH, 200, SUCCESS_EXPECTED);
    }

    @Test
    public void test_content_type_none(TestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpUtils.NONE_CONTENT_TYPE, true))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 200, SUCCESS_EXPECTED);
    }

    @Test
    public void test_content_type_json_utf8(TestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                                             HttpUtils.JSON_UTF8_CONTENT_TYPE))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 200, SUCCESS_EXPECTED);
    }

    @Test
    public void test_content_type_xml_should_success(TestContext context) {
        final RequestData reqData = RequestData.builder()
                                               .headers(new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                                             XML_CONTENT_TYPE))
                                               .build();
        sendToApiThenAssert(context, HttpMethod.GET, PATH, reqData, 200, SUCCESS_EXPECTED);
    }

}
