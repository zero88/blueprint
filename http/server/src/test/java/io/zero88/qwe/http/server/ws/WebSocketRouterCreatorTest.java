package io.zero88.qwe.http.server.ws;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.zero88.qwe.http.server.config.WebSocketConfig;
import io.zero88.qwe.http.server.mock.MockWebSocketEvent;

public class WebSocketRouterCreatorTest {

    @Test
    public void test_customize_root() {
        final WebSocketRouterCreator creator = new WebSocketRouterCreator().register(Collections.emptySet());
        final String s = creator.mountPoint(new WebSocketConfig());
        Assertions.assertEquals("/ws", s);
    }

    @Test
    public void test_one_metadata() {
        WebSocketServerPlan metadata = WebSocketServerPlan.createInbound(MockWebSocketEvent.INBOUND,
                                                                         MockWebSocketEvent.PROCESSOR);
        Assertions.assertEquals(1, new WebSocketRouterCreator().register(Collections.singletonList(metadata))
                                                               .getSocketsByPath()
                                                               .size());
    }

    @Test
    public void test_register_many_metadata_with_same_path() {
        WebSocketServerPlan metadata1 = WebSocketServerPlan.createInbound("xy", MockWebSocketEvent.INBOUND,
                                                                          MockWebSocketEvent.PROCESSOR);
        WebSocketServerPlan metadata2 = WebSocketServerPlan.createInbound("xy", MockWebSocketEvent.INBOUND,
                                                                          MockWebSocketEvent.PROCESSOR);
        Assertions.assertEquals(1, new WebSocketRouterCreator().register(Arrays.asList(metadata1, metadata2))
                                                               .getSocketsByPath()
                                                               .size());
    }

    @Test
    public void test_register_many_metadata_with_different_path() {
        WebSocketServerPlan metadata1 = WebSocketServerPlan.createInbound("xy", MockWebSocketEvent.INBOUND,
                                                                          MockWebSocketEvent.PROCESSOR);
        WebSocketServerPlan metadata2 = WebSocketServerPlan.createInbound("abc", MockWebSocketEvent.INBOUND,
                                                                          MockWebSocketEvent.PROCESSOR);
        Assertions.assertEquals(2, new WebSocketRouterCreator().register(Arrays.asList(metadata1, metadata2))
                                                               .getSocketsByPath()
                                                               .size());
    }

}
