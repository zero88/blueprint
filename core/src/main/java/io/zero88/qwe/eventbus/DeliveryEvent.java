package io.zero88.qwe.eventbus;

import io.zero88.qwe.dto.JsonData;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public final class DeliveryEvent implements JsonData {

    private String address;
    private EventAction action;
    private EventPattern pattern = EventPattern.REQUEST_RESPONSE;
    private boolean useRequestData = true;

}
