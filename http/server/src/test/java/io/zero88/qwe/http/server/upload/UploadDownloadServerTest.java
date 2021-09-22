package io.zero88.qwe.http.server.upload;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.zero88.qwe.TestHelper;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.http.server.HttpServerPluginTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class UploadDownloadServerTest extends HttpServerPluginTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(3000);

    @Override
    protected String httpConfigFile() { return "uploadDownload.json"; }

    @Test
    @Ignore
    //TODO fix it when implementing HTTP client. For test, replace TEST_TIMEOUT_SEC to `3000`
    public void test(TestContext context) {
        Async async = context.async();
        startServer(context, new HttpServerRouter());
    }

}
