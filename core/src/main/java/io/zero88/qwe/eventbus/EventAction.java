package io.zero88.qwe.eventbus;

import java.io.Serializable;
import java.util.Objects;

import io.zero88.qwe.dto.EnumType;
import io.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * Defines {@code action} for {@code Eventbus}
 */
@Getter
public final class EventAction extends AbstractEnumType implements EnumType, Serializable {

    private EventAction(String type) {
        super(type);
    }

    public static final EventAction INIT = new EventAction("INIT");
    public static final EventAction CREATE = new EventAction("CREATE");
    public static final EventAction UPDATE = new EventAction("UPDATE");
    public static final EventAction PATCH = new EventAction("PATCH");
    public static final EventAction HALT = new EventAction("HALT");
    public static final EventAction REMOVE = new EventAction("REMOVE");
    public static final EventAction GET_ONE = new EventAction("GET_ONE");
    public static final EventAction GET_LIST = new EventAction("GET_LIST");
    public static final EventAction CREATE_OR_UPDATE = new EventAction("CREATE_OR_UPDATE");
    public static final EventAction ACK = new EventAction("ACK");
    public static final EventAction REPLY = new EventAction("REPLY");
    public static final EventAction RETURN = new EventAction("RETURN");
    public static final EventAction MIGRATE = new EventAction("MIGRATE");
    public static final EventAction UNKNOWN = new EventAction("UNKNOWN");
    public static final EventAction SEND = new EventAction("SEND");
    public static final EventAction PUBLISH = new EventAction("PUBLISH");
    public static final EventAction MONITOR = new EventAction("MONITOR");
    public static final EventAction DISCOVER = new EventAction("DISCOVER");
    public static final EventAction NOTIFY = new EventAction("NOTIFY");
    public static final EventAction NOTIFY_ERROR = new EventAction("NOTIFY_ERROR");
    public static final EventAction SYNC = new EventAction("SYNC");
    public static final EventAction BATCH_CREATE = new EventAction("BATCH_CREATE");
    public static final EventAction BATCH_UPDATE = new EventAction("BATCH_UPDATE");
    public static final EventAction BATCH_PATCH = new EventAction("BATCH_PATCH");
    public static final EventAction BATCH_DELETE = new EventAction("BATCH_DELETE");

    public static EventAction parse(String action) {
        return EnumType.factory(action, EventAction.class, true, UNKNOWN);
    }

    @JsonValue
    public String action() {
        return this.type();
    }

    public int hashCode() {
        return this.action().hashCode();
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof String) {
            return Objects.equals(this.action(), o);
        }
        if (!(o instanceof EventAction)) {
            return false;
        }
        return Objects.equals(this.action(), ((EventAction) o).action());
    }

}
