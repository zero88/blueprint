package io.zero88.qwe.http.server.rest.api;

import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.http.HttpMethod;
import io.vertx.servicediscovery.Record;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.micro.HasServiceType;

import lombok.NonNull;

/**
 * Proxy service API that backed by {@code Service Discovery}
 *
 * @implNote To use it, your project must depend on {@code :micro:metadata}
 */
public interface ProxyServiceApi extends HasServiceType {

    static ProxyServiceApi create(@NonNull Record record) {
        return Optional.ofNullable(ProxyServiceApiLoader.getInstance().get(record.getType()))
                       .map(api -> api.setup(record))
                       .orElseThrow(() -> new IllegalArgumentException("Unsupported service type " + record.getType()));
    }

    /**
     * Setup proxy service based on service record
     *
     * @param record service record
     * @return a reference to this for fluent API
     */
    @NotNull ProxyServiceApi setup(Record record);

    /**
     * Service path
     *
     * @return HTTP path for proxy service
     */
    @NotNull Collection<ProxyPath> paths();

    /**
     * Router order
     *
     * @return Router order
     */
    int order();

    /**
     * Service name
     *
     * @return Service Name
     */
    @NotNull String name();

    /**
     * Create a service filter to reach out service in backend
     *
     * @param actualPath the actual path
     * @param method     the actual HTTP method
     * @return the filter
     */
    @NotNull RequestFilter createFilter(String actualPath, HttpMethod method);

}
