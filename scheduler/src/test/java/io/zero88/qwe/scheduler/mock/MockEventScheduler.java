package io.zero88.qwe.scheduler.mock;

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.event.EBContract;
import io.zero88.qwe.event.EventAction;
import io.zero88.qwe.event.EventListener;
import io.zero88.qwe.event.EventModel;
import io.zero88.qwe.event.EventPattern;

public class MockEventScheduler {

    public static final EventModel PROCESS_EVENT = EventModel.builder()
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .addEvents(EventAction.CREATE)
                                                             .local(true)
                                                             .address("event.job.model.test")
                                                             .build();
    public static final EventModel CALLBACK_EVENT = EventModel.builder()
                                                              .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                              .addEvents(EventAction.PUBLISH)
                                                              .local(true)
                                                              .address("event.job.model.callback.test")
                                                              .build();


    public static class MockProcessEventSchedulerListener implements EventListener {

        private AtomicInteger count = new AtomicInteger(0);

        @EBContract(action = "CREATE")
        public JsonObject increaseNumber() {
            return new JsonObject().put("count", count.getAndIncrement());
        }

    }


    public static class FailureProcessEventSchedulerListener implements EventListener {

        @EBContract(action = "CREATE")
        public JsonObject increaseNumber() {
            throw new IllegalArgumentException("Failed");
        }

    }

}
