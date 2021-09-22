package io.zero88.qwe.mock;

import io.vertx.core.Handler;
import io.zero88.qwe.ApplicationContextHolder;
import io.zero88.qwe.ApplicationVerticle;

import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true)
public final class MockApplication extends ApplicationVerticle {

    private boolean errorOnStart;
    private boolean errorOnCompleted;
    private Handler<ApplicationContextHolder> onCompleted;

    @Override
    public void onStart() {
        if (errorOnStart) {
            throw new RuntimeException("Error when starting");
        }
    }

    @Override
    public void onInstallCompleted(ApplicationContextHolder holder) {
        if (errorOnCompleted) {
            throw new IllegalArgumentException("Error onInstallCompleted");
        }
        if (onCompleted != null) {
            onCompleted.handle(holder);
        }
    }

    @Override
    public String configFile() {
        return "mock-app.json";
    }

}
