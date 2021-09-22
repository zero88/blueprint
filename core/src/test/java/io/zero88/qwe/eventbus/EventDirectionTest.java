package io.zero88.qwe.eventbus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventDirectionTest {

    @Test
    public void test_no_address() {
        Assertions.assertThrows(NullPointerException.class, () -> EventDirection.builder().build());
    }

    @Test
    public void test_only_address() {
        EventDirection model = EventDirection.builder().address("1").build();
        Assertions.assertEquals("1", model.getAddress());
        Assertions.assertEquals(EventPattern.REQUEST_RESPONSE, model.getPattern());
        Assertions.assertTrue(model.isLocal());
    }

    @Test
    public void test_full() {
        EventDirection model = EventDirection.builder()
                                             .address("1")
                                             .local(false)
                                             .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                             .build();
        Assertions.assertEquals("1", model.getAddress());
        Assertions.assertEquals(EventPattern.PUBLISH_SUBSCRIBE, model.getPattern());
        Assertions.assertFalse(model.isLocal());
    }

}
