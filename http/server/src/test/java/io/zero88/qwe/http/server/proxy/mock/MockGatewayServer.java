package io.zero88.qwe.http.server.proxy.mock;

import io.zero88.qwe.ApplicationVerticle;
import io.zero88.qwe.http.server.HttpServerPluginProvider;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.micro.DiscoveryPluginProvider;

public class MockGatewayServer extends ApplicationVerticle {

    @Override
    public void onStart() {
        this.addProvider(new HttpServerPluginProvider(new HttpServerRouter())).addProvider(new DiscoveryPluginProvider());
    }

    public String configFile() { return "gateway.json"; }

}
