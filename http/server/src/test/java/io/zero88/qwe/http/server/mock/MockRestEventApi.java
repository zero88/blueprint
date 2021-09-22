package io.zero88.qwe.http.server.mock;

import java.util.Collections;
import java.util.Set;

import io.zero88.qwe.http.EventMethodDefinition;
import io.zero88.qwe.http.server.rest.api.RestEventApi;

public class MockRestEventApi implements RestEventApi {

    @Override
    public String address() {
        return "http.server.test";
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault("/test/events", "/:event_id"));
    }

}
