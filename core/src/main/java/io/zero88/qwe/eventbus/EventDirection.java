package io.zero88.qwe.eventbus;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * A direction that helps to identify event address
 *
 * @see EventAction
 * @see EventPattern
 */
@Getter
@Jacksonized
@Builder(builderClassName = "Builder")
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class EventDirection {

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private final String address;

    @Default
    @ToString.Include
    private final EventPattern pattern = EventPattern.REQUEST_RESPONSE;

    @Default
    @ToString.Include
    private final boolean local = true;

}
