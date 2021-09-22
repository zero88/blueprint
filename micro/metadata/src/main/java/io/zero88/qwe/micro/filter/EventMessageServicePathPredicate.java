package io.zero88.qwe.micro.filter;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.micro.GatewayHeaders;
import io.zero88.qwe.micro.servicetype.EventMessageHttpService;
import io.zero88.qwe.utils.JsonUtils;

public final class EventMessageServicePathPredicate implements ByPathPredicateFactory<EventMethodDefinition> {

    @Override
    public String serviceType() {
        return EventMessageHttpService.TYPE;
    }

    @Override
    public EventMethodDefinition parseLocation(Record record) {
        return EventMethodDefinition.from(record.getLocation());
    }

    @Override
    public boolean test(EventMethodDefinition definition, String path, JsonObject filter) {
        return JsonUtils.findString(filter, ServiceFilterParam.ACTION)
                        .map(EventAction::parse)
                        .map(action -> definition.test(path, action))
                        .orElseGet(() -> JsonUtils.findString(filter, GatewayHeaders.X_FORWARDED_METHOD)
                                                  .map(HttpMethod::valueOf)
                                                  .map(m -> definition.test(path, m))
                                                  .orElseGet(() -> definition.test(path)));
    }

}
