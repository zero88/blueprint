package io.zero88.qwe.http.server.rest.api;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.utils.Strings;
import io.vertx.core.http.HttpMethod;
import io.vertx.servicediscovery.Record;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.micro.GatewayHeaders;
import io.zero88.qwe.micro.filter.ByPredicateFactory;
import io.zero88.qwe.micro.filter.ServiceFilterParam;
import io.zero88.qwe.micro.servicetype.EventMessageHttpService;

public class ProxyEventServiceApi implements ProxyServiceApi {

    private Record record;
    private EventMethodDefinition definition;

    @Override
    public ProxyServiceApi setup(Record record) {
        this.record = record;
        this.definition = EventMethodDefinition.from(record.getLocation());
        return this;
    }

    @Override
    public @NotNull Collection<ProxyPath> paths() {
        return definition.getMapping()
                         .stream()
                         .filter(m -> Strings.isNotBlank(m.getCapturePath()))
                         .map(m -> new ProxyPath(m.getCapturePath(), m.getMethod(), m.getAuth()))
                         .sorted(Comparator.<ProxyPath>comparingInt(p -> p.getPath().length())
                                           .thenComparing(p -> p.getMethod().name().length())
                                           .reversed())
                         .collect(Collectors.toList());
    }

    @Override
    public int order() {
        return definition.getOrder();
    }

    @Override
    public String name() {
        return record.getName();
    }

    @Override
    public RequestFilter createFilter(String actualPath, HttpMethod method) {
        return new RequestFilter().put(ServiceFilterParam.NAME, name())
                                  .put(ServiceFilterParam.TYPE, serviceType())
                                  .put(ServiceFilterParam.BY, ByPredicateFactory.BY_PATH)
                                  .put(ServiceFilterParam.IDENTIFIER, actualPath)
                                  .put(GatewayHeaders.X_FORWARDED_METHOD, method.name());
    }

    @Override
    public String serviceType() {
        return EventMessageHttpService.TYPE;
    }

}
