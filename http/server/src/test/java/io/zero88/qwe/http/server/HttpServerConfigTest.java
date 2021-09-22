package io.zero88.qwe.http.server;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.zero88.qwe.IConfig;
import io.zero88.qwe.utils.JsonUtils;

public class HttpServerConfigTest {

    @Test
    public void test_default() throws JSONException {
        HttpServerConfig config = new HttpServerConfig();
        System.out.println(config.toJson().encode());
        HttpServerConfig fromFile = IConfig.from(JsonUtils.loadJsonInClasspath("httpServer.json"), HttpServerConfig.class);
        JSONAssert.assertEquals(fromFile.toJson().encode(), config.toJson().encode(), JSONCompareMode.LENIENT);
    }

    @Test
    public void deserialize() {
        String jsonStr = "{\"__app__\":{\"__http__\":{\"publicServerUrl\":\"https://1.1.1.1\"," +
                         "\"host\":\"localhost\",\"port\":9090,\"__api__\":{\"enabled\":true,\"path\":\"/xyz\"}}}}";
        HttpServerConfig from = IConfig.from(jsonStr, HttpServerConfig.class);
        Assertions.assertNotNull(from);
        Assertions.assertEquals("localhost", from.getHost());
        Assertions.assertEquals(9090, from.getPort());
        Assertions.assertEquals("/xyz", from.getApiConfig().getPath());
        Assertions.assertEquals("https://1.1.1.1", from.getPublicServerUrl());
        Assertions.assertTrue(from.getApiConfig().isEnabled());
        Assertions.assertNotNull(from.getOptions());
        Assertions.assertTrue(from.getOptions().isCompressionSupported());
        Assertions.assertTrue(from.getOptions().isDecompressionSupported());
        Assertions.assertFalse(from.getWebSocketConfig().isEnabled());
        Assertions.assertFalse(from.getHttp2Cfg().isEnabled());
    }

}
