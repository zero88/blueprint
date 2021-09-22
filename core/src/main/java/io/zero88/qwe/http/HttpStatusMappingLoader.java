package io.zero88.qwe.http;

import java.util.function.Supplier;

import io.vertx.core.ServiceHelper;

public final class HttpStatusMappingLoader implements Supplier<HttpStatusMapping> {

    private static HttpStatusMappingLoader instance;

    public static HttpStatusMappingLoader getInstance() {
        if (instance == null) {
            synchronized (HttpStatusMappingLoader.class) {
                instance = new HttpStatusMappingLoader();
            }
        }
        return instance;
    }

    private final HttpStatusMapping mapping;

    private HttpStatusMappingLoader() {
        mapping = ServiceHelper.loadFactory(HttpStatusMapping.class);
    }

    @Override
    public HttpStatusMapping get() {
        return mapping;
    }

}
