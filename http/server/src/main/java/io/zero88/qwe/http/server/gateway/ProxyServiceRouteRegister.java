package io.zero88.qwe.http.server.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.utils.Urls;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;
import io.zero88.qwe.http.server.HttpServerPluginContext;
import io.zero88.qwe.http.server.HttpSystem.GatewaySystem;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.http.server.ServerInfo;
import io.zero88.qwe.http.server.rest.api.ProxyPath;
import io.zero88.qwe.http.server.rest.api.ProxyServiceApi;
import io.zero88.qwe.http.server.rest.handler.ProxyServiceDispatcher;
import io.zero88.qwe.micro.monitor.ServiceGatewayMonitor;

import lombok.NonNull;

public interface ProxyServiceRouteRegister extends ServiceGatewayMonitor, GatewaySystem {

    default @NonNull Logger logger() {
        return LoggerFactory.getLogger(RouterCreator.class);
    }

    default void registerRouter(Record record) {
        ProxyServiceApi api = ProxyServiceApi.create(record);
        ServerInfo serverInfo = sharedData().getData(HttpServerPluginContext.SERVER_INFO_DATA_KEY);
        Router router = serverInfo.getRouter();
        for (ProxyPath pPath : api.paths()) {
            String path = Urls.combinePath(serverInfo.getServicePath(), pPath.getPath());
            if (record.getStatus() == Status.UP) {
                ProxyServiceDispatcher handler = ProxyServiceDispatcher.create(getDiscovery(),
                                                                               serverInfo.getServicePath(),
                                                                               pPath.getAuthDefinition(),
                                                                               api::createFilter);
                logger().info(decor("Enable proxy route | API[{}] | Order[{}] | Path[{}::{}]"), api.name(), api.order(),
                              pPath.getMethod(), path);
                router.route(path).order(api.order()).handler(handler).enable();
            } else {
                logger().info(decor("Disable proxy route | API[{}] | Path[{}]"), api.name(), pPath);
                router.route(path).disable();
            }
        }
    }

}
