package io.zero88.qwe.http.server.mock;

import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.http.server.ws.WebSocketServerPlan;

public class MockWebSocketEvent {

    public static final EventDirection INBOUND = EventDirection.builder().address("ws.client2server").build();
    public static final EventDirection PROCESSOR = EventDirection.builder().address("server.processor").build();
    public static final EventDirection OUTBOUND = EventDirection.builder()
                                                                .address("ws.server2client")
                                                                .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                .build();
    public static final EventDirection SAME = EventDirection.builder().address("same").build();

    public static final WebSocketServerPlan GREETING = WebSocketServerPlan.createInbound(INBOUND, PROCESSOR);
    public static final WebSocketServerPlan FULL_PLAN = WebSocketServerPlan.createInbound("rt1", INBOUND, PROCESSOR,
                                                                                          OUTBOUND);
    public static final WebSocketServerPlan NO_OUTBOUND = WebSocketServerPlan.createInbound("rt2", INBOUND, PROCESSOR);
    public static final WebSocketServerPlan ONLY_OUTBOUND = WebSocketServerPlan.createOutbound("rt3", OUTBOUND);
    public static final WebSocketServerPlan SAME_ADDR_ONLY_OUTBOUND = WebSocketServerPlan.createInbound("rt3", SAME,
                                                                                                        PROCESSOR);
    public static final WebSocketServerPlan SAME_ADDR_INBOUND_OUTBOUND = WebSocketServerPlan.createInbound("xx", SAME,
                                                                                                           PROCESSOR,
                                                                                                           SAME);

}
