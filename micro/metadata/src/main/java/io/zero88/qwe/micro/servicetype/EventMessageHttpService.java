package io.zero88.qwe.micro.servicetype;

import java.util.Optional;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceType;
import io.zero88.qwe.http.EventMethodDefinition;

import lombok.NonNull;

public interface EventMessageHttpService extends ServiceType {

    /**
     * Name of the type.
     */
    String TYPE = "eventbus-2-http";
    String SHARED_KEY_CONFIG = "sharedKey";
    String DELIVERY_OPTIONS_CONFIG = "options";

    static Record createRecord(@NonNull String name, @NonNull String address,
                               @NonNull EventMethodDefinition definition) {
        return createRecord(name, address, definition, null);
    }

    static Record createRecord(@NonNull String name, @NonNull String address, @NonNull EventMethodDefinition definition,
                               JsonObject metadata) {
        return new Record().setType(TYPE)
                           .setName(Strings.requireNotBlank(name))
                           .setMetadata(Optional.ofNullable(metadata).orElseGet(JsonObject::new))
                           .setLocation(definition.toJson().put(Record.ENDPOINT, Strings.requireNotBlank(address)));
    }

    @Override
    default String name() { return TYPE; }

}
