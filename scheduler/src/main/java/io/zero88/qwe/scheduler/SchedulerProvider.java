package io.zero88.qwe.scheduler;

import io.zero88.qwe.ComponentProvider;
import io.zero88.qwe.SharedDataLocalProxy;

public final class SchedulerProvider implements ComponentProvider<SchedulerVerticle> {

    @Override
    public Class<SchedulerVerticle> componentClass() {
        return SchedulerVerticle.class;
    }

    @Override
    public SchedulerVerticle provide(SharedDataLocalProxy proxy) {
        return new SchedulerVerticle(proxy);
    }

}
