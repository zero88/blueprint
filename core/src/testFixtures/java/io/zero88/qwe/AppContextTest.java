package io.zero88.qwe;

import java.nio.file.Path;

import io.vertx.core.Vertx;

public interface AppContextTest {

    Path testDir();

    String appName();

    default String sharedKey() {
        return getClass().getName();
    }

    default SharedDataLocalProxy createSharedData(Vertx vertx) {
        return SharedDataLocalProxy.create(vertx, sharedKey());
    }

}
