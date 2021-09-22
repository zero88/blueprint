package io.zero88.qwe.micro;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.micro.servicetype.EventMessageHttpService;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecordHelper {

    public static Record create(String api, HttpLocation location) {
        return create(api, location, null);
    }

    public static Record create(String api, HttpLocation location, JsonObject metadata) {
        return HttpEndpoint.createRecord(api, location.isSsl(), location.getHost(), location.getPort(),
                                         location.getRoot(), metadata);
    }

    public static Record create(String api, String address, EventMethodDefinition definition) {
        return EventMessageHttpService.createRecord(api, address, definition);
    }

}
