package io.zero88.qwe.http.server.rest.api;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.http.HttpMethod;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.micro.filter.ByPredicateFactory;
import io.zero88.qwe.micro.filter.ServiceFilterParam;
import io.zero88.qwe.utils.PriorityUtils;

public class ProxyHttpServiceApi implements ProxyServiceApi {

    private Record record;
    private HttpLocation location;

    @Override
    public String serviceType() {return HttpEndpoint.TYPE;}

    @Override
    public ProxyHttpServiceApi setup(Record record) {
        this.record = record;
        this.location = record.getLocation().mapTo(HttpLocation.class);
        return this;
    }

    @Override
    public @NotNull Collection<ProxyPath> paths() {
        return Collections.singleton(
            new ProxyPath(RouterCreator.addWildcards(location.getRoot()), HttpMethod.GET, ReqAuthDefinition.noAuth()));
    }

    @Override
    public int order() {
        return PriorityUtils.priorityOrder(location.getRoot().length());
    }

    @Override
    public String name() {
        return record.getName();
    }

    @Override
    public RequestFilter createFilter(String actualPath, HttpMethod method) {
        return new RequestFilter().put(ServiceFilterParam.TYPE, serviceType())
                                  .put(ServiceFilterParam.IDENTIFIER, location.getEndpoint())
                                  .put(ServiceFilterParam.BY, ByPredicateFactory.BY_ENDPOINT);
    }

}
