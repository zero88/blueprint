package io.zero88.qwe.http.server.rest.api;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.ServiceHelper;

public final class ProxyServiceApiLoader {

    private static ProxyServiceApiLoader instance;

    public static ProxyServiceApiLoader getInstance() {
        if (instance == null) {
            synchronized (ProxyServiceApiLoader.class) {
                instance = new ProxyServiceApiLoader();
            }
        }
        return instance;
    }

    private final Map<String, Class<? extends ProxyServiceApi>> cls;

    private ProxyServiceApiLoader() {
        cls = ServiceHelper.loadFactories(ProxyServiceApi.class)
                           .stream()
                           .collect(Collectors.toMap(ProxyServiceApi::serviceType, ProxyServiceApi::getClass));
    }

    public ProxyServiceApi get(String serviceType) {
        return Optional.ofNullable(cls.get(serviceType)).map(ReflectionClass::createObject).orElse(null);
    }

}
