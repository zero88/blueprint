package io.zero88.qwe.http.server;

import io.zero88.qwe.PluginProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class HttpServerPluginProvider implements PluginProvider<HttpServerPlugin> {

    private final HttpServerRouter httpRouter;

    @Override
    public HttpServerPlugin get() {
        return new HttpServerPlugin(httpRouter);
    }

    @Override
    public Class<HttpServerPlugin> pluginClass() {
        return HttpServerPlugin.class;
    }

}
