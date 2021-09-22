package io.zero88.qwe.http.client.handler;

import java.util.Objects;

import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.eventbus.bridge.EventBridgePlan;
import io.zero88.qwe.eventbus.bridge.EventBridgePlan.AbstractEventBridgePlan;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents for {@code Websocket Client} event definition
 */
@Getter
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true, callSuper = false)
public class WebSocketClientPlan extends AbstractEventBridgePlan<WebSocketClientPlan> implements EventBridgePlan {

    /**
     * Websocket path. {@code Nullable} if using root {@code WebSocket} path
     */
    @EqualsAndHashCode.Include
    private final String path;

    private WebSocketClientPlan(String path, EventDirection inbound, EventDirection outbound) {
        super(inbound, null, outbound);
        this.path = Urls.combinePath(Strings.fallback(path, "/"));
    }

    public static WebSocketClientPlan create(@NonNull EventDirection inbound, String outboundAddress) {
        return create(null, inbound, outboundAddress);
    }

    public static WebSocketClientPlan create(String path, @NonNull EventDirection inbound, String outboundAddress) {
        return create(path, inbound, EventDirection.builder()
                                                   .address(Strings.requireNotBlank(outboundAddress))
                                                   .pattern(EventPattern.POINT_2_POINT)
                                                   .build());
    }

    public static WebSocketClientPlan create(String path, EventDirection inbound, EventDirection outbound) {
        return new WebSocketClientPlan(path, inbound, outbound).validate();
    }

    @Override
    public EventDirection processor() {
        throw new UnsupportedOperationException("WebSocket client does not support processor");
    }

    @Override
    protected WebSocketClientPlan validate() {
        Functions.getOrThrow(() -> Objects.requireNonNull(outbound()),
                             t -> new IllegalArgumentException("WebSocket client is missing inbound definition"));
        if (outbound().getPattern() != EventPattern.POINT_2_POINT) {
            throw new IllegalArgumentException("WebSocket client publisher only support " + EventPattern.POINT_2_POINT);
        }
        return this;
    }

}
