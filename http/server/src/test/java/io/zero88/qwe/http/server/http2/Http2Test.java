package io.zero88.qwe.http.server.http2;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.zero88.qwe.KeyStoreProvider;
import io.zero88.qwe.TestHelper;
import io.zero88.qwe.crypto.CryptoContext;
import io.zero88.qwe.http.client.HttpClientConfig;
import io.zero88.qwe.http.client.HttpClientExtension;
import io.zero88.qwe.http.client.HttpClientWrapper;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.zero88.qwe.http.server.HttpServerRouter;

public class Http2Test extends HttpServerPluginTestBase {

    private KeyStoreProvider keyStoreProvider;

    @Override
    public void before(TestContext context) {
        vertx = Vertx.vertx(new VertxOptions().setAddressResolverOptions(
            new AddressResolverOptions().addServer("8.8.8.8")
                                        .setHostsValue(Buffer.buffer("127.0.0.1  http.tls.test.playio"))));
        keyStoreProvider = KeyStoreProvider.tls().init(vertx);
        client = createHttpClient();
        httpConfig = initConfig();
        requestOptions = new RequestOptions().setPort(httpConfig.getPort());
    }

    @Override
    protected String httpConfigFile() {
        return "http2.json";
    }

    protected HttpClientWrapper createHttpClient() {
        int timeout = TestHelper.TEST_TIMEOUT_SEC * 1000;
        HttpClientConfig conf = new HttpClientConfig().setHttp2Enabled(true)
                                                      .setOptions(new HttpClientOptions().setConnectTimeout(timeout));
        return initExtension(vertx, HttpClientExtension.class, conf,
                             new CryptoContext(null, keyStoreProvider.getCertificates())).entrypoint();
    }

    @Override
    public CryptoContext pluginCryptoContext() {
        return new CryptoContext(keyStoreProvider.getKeyCert("http"), null);
    }

    @Test
    public void test(TestContext testContext) {
        final Async async = testContext.async(2);
        startServer(testContext, new HttpServerRouter().addCustomBuilder((vertx, rootRouter, config, ctx) -> {
            rootRouter.route("/http2").handler(c -> c.response().end("hello"));
            return rootRouter;
        }));
        client.openRequest(requestOptions().setSsl(true).setHost("http.tls.test.playio").setURI("/http2"))
              .flatMap(HttpClientRequest::send)
              .onFailure(testContext::fail)
              .onSuccess(resp -> {
                  System.out.println(resp.headers());
                  testContext.assertEquals(HttpVersion.HTTP_2, resp.version());
                  testContext.assertEquals(200, resp.statusCode());
                  resp.bodyHandler(buff -> {
                      testContext.assertEquals("hello", buff.toString());
                      testComplete(async);
                  });
              });
        client.openRequest(requestOptions().setSsl(false).setHost("docs.oracle.com").setPort(80).setURI("/"))
              .flatMap(HttpClientRequest::send)
              .onFailure(testContext::fail)
              .onSuccess(resp -> {
                  System.out.println(resp.headers());
                  testContext.assertEquals(HttpVersion.HTTP_1_1, resp.version());
                  testContext.assertEquals(301, resp.statusCode());
                  resp.bodyHandler(buff -> {
                      System.out.println(buff.toString());
                      Assertions.assertNotNull(buff);
                      Assertions.assertEquals(0, buff.length());
                      testComplete(async);
                  });
              });
    }

}
