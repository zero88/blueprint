package io.zero88.qwe.eventbus;

import java.io.Serializable;

/**
 * {@code EventBus} pattern mode.
 */
public enum EventPattern implements Serializable {

    PUBLISH_SUBSCRIBE, POINT_2_POINT, REQUEST_RESPONSE
}
