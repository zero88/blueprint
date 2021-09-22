package io.zero88.qwe.example;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Counter;
import io.zero88.qwe.ApplicationVerticle;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;

public abstract class CounterProducer extends ApplicationVerticle implements CounterApp {

    @Override
    public void onStart() {
        EventBusClient client = EventBusClient.create(sharedData());
        vertx.setPeriodic(2000, event -> vertx.sharedData()
                                              .getCounter("Periodic")
                                              .flatMap(Counter::getAndIncrement)
                                              .onSuccess(c -> client.publish(address(), msg(c))));
    }

    protected EventMessage msg(Long c) {
        logger().info(appName() + " count [" + c + "]");
        return EventMessage.initial(EventAction.NOTIFY, new JsonObject().put("count", c).put("app", appName()));
    }

}
