package io.zero88.qwe.http.server.config;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.repl.ReflectionClass;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.zero88.qwe.IConfig;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.WebSocketSystem;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.ws.DefaultWebSocketBridgeEventHandler;
import io.zero88.qwe.http.server.ws.WebSocketBridgeEventHandler;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class WebSocketConfig extends AbstractRouterConfig implements RouterConfig, WebSocketSystem {

    public static final String NAME = "__websocket__";
    private String bridgeHandlerClass = DefaultWebSocketBridgeEventHandler.class.getName();
    @JsonProperty(value = SockJSConfig.NAME)
    private SockJSConfig sockjsOptions = new SockJSConfig();
    @JsonProperty(value = SocketBridgeConfig.NAME)
    private SocketBridgeConfig bridgeOptions = new SocketBridgeConfig();

    public WebSocketConfig() {
        super(NAME, HttpServerConfig.class);
    }

    public @Nullable Class<? extends WebSocketBridgeEventHandler> bridgeHandlerClass() {
        return ReflectionClass.findClass(bridgeHandlerClass);
    }

    @Override
    protected @NonNull String defaultPath() {
        return "/ws";
    }

    public static class SockJSConfig extends SockJSHandlerOptions implements IConfig {

        public static final String NAME = "__sockjs__";

        @Override
        public String configKey() {return NAME;}

        @Override
        public Class<? extends IConfig> parent() {return WebSocketConfig.class;}

    }


    public static class SocketBridgeConfig extends SockJSBridgeOptions implements IConfig {

        public static final String NAME = "__bridge__";

        @Override
        public String configKey() {return NAME;}

        @Override
        public Class<? extends IConfig> parent() {return WebSocketConfig.class;}

    }

}
