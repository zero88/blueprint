package io.zero88.qwe.micro.monitor;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.micro.ServiceDiscoveryApi;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ServiceGatewayUsageMonitor extends AbstractServiceGatewayMonitor<UsageInfo> {

    protected ServiceGatewayUsageMonitor(@NonNull SharedDataLocalProxy sharedData, @NonNull ServiceDiscoveryApi api) {
        super(sharedData, api);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ServiceGatewayUsageMonitor> T create(SharedDataLocalProxy sharedData,
                                                                  ServiceDiscoveryApi api, String className) {
        return (T) ServiceGatewayMonitor.create(sharedData, api, className, ServiceGatewayUsageMonitor.class);
    }

    @Override
    public String monitorName() {
        return "service-usage-gateway";
    }

    @Override
    protected UsageInfo parse(Message<Object> message) {
        return UsageInfo.parse((JsonObject) trace(message).body());
    }

}
