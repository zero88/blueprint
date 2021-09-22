package io.zero88.qwe.eventbus;

import io.zero88.qwe.transport.ProxyService;

public interface EventBusProxy extends ProxyService<EventBusClient> {

    EventBusClient transporter();

}
