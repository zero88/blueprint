package io.zero88.qwe.http.client;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.zero88.qwe.ExtensionTestHelper;

@ExtendWith(VertxExtension.class)
public abstract class HttpExtensionTestBase implements ExtensionTestHelper {

    @TempDir
    Path tmp;
    protected HttpClientExtension extension;

    @Override
    public Path testDir() {
        return tmp;
    }

    @BeforeEach
    void before(Vertx vertx) {
        extension = initExtension(vertx, HttpClientExtension.class, new HttpClientConfig());
    }

    @AfterEach
    void after() {
        extension.stop();
    }

}
