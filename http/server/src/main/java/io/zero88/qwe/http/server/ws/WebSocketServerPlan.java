package io.zero88.qwe.http.server.ws;

import java.util.Objects;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.eventbus.bridge.EventBridgePlan;
import io.zero88.qwe.eventbus.bridge.EventBridgePlan.AbstractEventBridgePlan;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Defines {@code sub path} of Web Socket with server listener event and server publisher event.
 *
 * @see EventDirection
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public final class WebSocketServerPlan extends AbstractEventBridgePlan<WebSocketServerPlan> implements EventBridgePlan {

    /**
     * WebSocket path. {@code Nullable} if using root {@code WebSocket} path
     */
    @EqualsAndHashCode.Include
    private final String path;
    private final boolean onlyOutbound;

    private WebSocketServerPlan(String path, EventDirection inbound, EventDirection processor, EventDirection outbound,
                                boolean onlyOutbound) {
        super(inbound, processor, outbound);
        this.path = Urls.combinePath(Strings.fallback(path, "/"));
        this.onlyOutbound = onlyOutbound;
    }

    public static WebSocketServerPlan createInbound(EventDirection inbound, EventDirection processor) {
        return createInbound(null, inbound, processor, null);
    }

    public static WebSocketServerPlan createInbound(String path, EventDirection inbound, EventDirection processor) {
        return createInbound(path, inbound, processor, null);
    }

    public static WebSocketServerPlan createInbound(EventDirection inbound, EventDirection processor,
                                                    EventDirection outbound) {
        return createInbound(null, inbound, processor, outbound);
    }

    public static WebSocketServerPlan createInbound(String path, EventDirection inbound, EventDirection processor,
                                                    EventDirection outbound) {
        return new WebSocketServerPlan(path, inbound, processor, outbound, false).validate();
    }

    public static WebSocketServerPlan createOutbound(EventDirection outbound) {
        return createOutbound(null, outbound);
    }

    public static WebSocketServerPlan createOutbound(String path, EventDirection outbound) {
        return new WebSocketServerPlan(path, null, null, outbound, true).validate();
    }

    @Override
    protected WebSocketServerPlan validate() {
        if (isOnlyOutbound()) {
            if (Strings.isBlank(outboundAddress())) {
                throw new IllegalArgumentException("Must provide outbound address");
            }
            if (outbound().getPattern() != EventPattern.PUBLISH_SUBSCRIBE) {
                throw new IllegalArgumentException(
                    "WebSocket server outbound only support " + EventPattern.PUBLISH_SUBSCRIBE);
            }
        } else {
            if (Objects.isNull(inbound()) || Objects.isNull(processor())) {
                throw new IllegalArgumentException("Must provide both listener and processor");
            }
        }
        return this;
    }

}
