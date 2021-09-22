package io.zero88.qwe.http.server.proxy.mock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.ApplicationVerticle;
import io.zero88.qwe.ApplicationContextHolder;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.http.HttpUtils;
import io.zero88.qwe.http.server.HttpServerPluginContext;
import io.zero88.qwe.http.server.HttpServerPluginProvider;
import io.zero88.qwe.http.server.HttpServerRouter;
import io.zero88.qwe.http.server.ServerInfo;
import io.zero88.qwe.http.server.rest.api.RestApi;
import io.zero88.qwe.micro.DiscoveryContext;
import io.zero88.qwe.micro.DiscoveryPluginProvider;
import io.zero88.qwe.micro.RecordHelper;

public class MockHttpServiceServer extends ApplicationVerticle {

    @Override
    public void onStart() {
        this.addProvider(new HttpServerPluginProvider(new HttpServerRouter().registerApi(MockAPI.class)))
            .addProvider(new DiscoveryPluginProvider());
    }

    public String configFile() { return "httpService.json"; }

    @Override
    public void onInstallCompleted(ApplicationContextHolder holder) {
        final HttpServerPluginContext httpContext = holder.plugin(HttpServerPluginContext.class);
        final DiscoveryContext discoveryContext = holder.plugin(DiscoveryContext.class);
        final ServerInfo info = httpContext.getServerInfo();
        final HttpLocation location = new HttpLocation(info.toJson()).setRoot(info.getApiPath());
        discoveryContext.getDiscovery().register(RecordHelper.create("httpService", location));
    }

    @Path("/test")
    public static class MockAPI implements RestApi {

        @GET
        @Produces(HttpUtils.JSON_UTF8_CONTENT_TYPE)
        public JsonObject get() {
            return new JsonObject().put("hello", "dynamic");
        }

        @GET
        @Path("/error")
        @Produces(HttpUtils.JSON_UTF8_CONTENT_TYPE)
        public JsonObject error() {
            throw new QWEException("error");
        }

    }

}
