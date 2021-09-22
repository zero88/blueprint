package io.zero88.qwe.micro.monitor;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.micro.ServiceDiscoveryApi;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ServiceGatewayAnnounceMonitor extends AbstractServiceGatewayMonitor<Record> {

    protected ServiceGatewayAnnounceMonitor(@NonNull SharedDataLocalProxy proxy, @NonNull ServiceDiscoveryApi api) {
        super(proxy, api);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ServiceGatewayAnnounceMonitor> T create(SharedDataLocalProxy proxy,
                                                                     ServiceDiscoveryApi wrapper, String className) {
        return (T) ServiceGatewayMonitor.create(proxy, wrapper, className, ServiceGatewayAnnounceMonitor.class);
    }

    @Override
    protected Record parse(Message<Object> message) {
        return new Record((JsonObject) trace(message).body());
    }

    @Override
    public String monitorName() {
        return "service-announcement-gateway";
    }

}
