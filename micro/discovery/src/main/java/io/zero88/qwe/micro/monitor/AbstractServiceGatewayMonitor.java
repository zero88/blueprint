package io.zero88.qwe.micro.monitor;

import io.vertx.core.eventbus.Message;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.micro.ServiceDiscoveryApi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class AbstractServiceGatewayMonitor<T> implements ServiceGatewayMonitor {

    @NonNull
    @Accessors(fluent = true)
    private final SharedDataLocalProxy sharedData;
    @NonNull
    private final ServiceDiscoveryApi discovery;

    @Override
    public final void handle(Message<Object> message) {
        try {
            process(parse(trace(message)));
        } catch (Exception e) {
            logger().warn("{} error", monitorName(), e);
        }
    }

    Message<Object> trace(Message<Object> message) {
        if (logger().isTraceEnabled()) {
            logger().trace("{}::Receive message [{}] - Headers[{}] - Body[{}]", monitorName(), message.address(),
                           message.headers(), message.body());
        }
        return message;
    }

    /**
     * Parse received message to monitor object
     *
     * @param message message
     * @return a monitor object
     */
    protected abstract T parse(Message<Object> message);

    /**
     * Process a monitor object
     *
     * @param monitorObject a monitor object
     */
    protected void process(T monitorObject) {}

}
