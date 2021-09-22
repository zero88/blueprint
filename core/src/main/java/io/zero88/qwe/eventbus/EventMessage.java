package io.zero88.qwe.eventbus;

import java.io.Serializable;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.exceptions.ErrorCode;
import io.github.zero88.repl.ReflectionClass;
import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.auth.UserInfo;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.DataTransferObject.StandardKey;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.exceptions.QWEException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents for data transfer object in event bus system.
 *
 * @apiNote If {@link EventMessage} is used in request mode, then as {@code QWE} standard message, the data is
 *     instance of {@link RequestData} with one of known key data in {@link StandardKey}
 * @see EventStatus
 * @see EventAction
 * @see ErrorMessage
 * @see RequestData
 * @since 1.0.0
 */
@ToString(doNotUseGetters = true)
@JsonInclude(Include.NON_NULL)
public final class EventMessage implements Serializable, JsonData {

    @Getter
    private final EventStatus status;
    @Getter
    private final EventAction action;
    @Getter
    private final EventAction prevAction;
    private final Buffer data;
    @JsonIgnore
    private final Class<?> dataClass;
    @Getter
    @JsonProperty
    private final ErrorMessage error;
    @Getter
    @Setter
    @Accessors(chain = true)
    private UserInfo userInfo;

    @JsonCreator
    private EventMessage(@JsonProperty(value = "status", defaultValue = "INITIAL") EventStatus status,
                         @NonNull @JsonProperty(value = "action", required = true) EventAction action,
                         @JsonProperty(value = "prevAction") EventAction prevAction,
                         @JsonProperty(value = "data") Object data,
                         @JsonProperty(value = "dataClass") Class<?> dataClass,
                         @JsonProperty(value = "error") ErrorMessage error) {
        this.status = Objects.isNull(status) ? EventStatus.INITIAL : status;
        this.action = action;
        this.prevAction = prevAction;
        this.data = Objects.isNull(data) ? null : parse(data);
        this.dataClass = Objects.nonNull(dataClass) ? dataClass : Objects.nonNull(data) ? data.getClass() : null;
        this.error = error;
    }

    private static Buffer parse(Object data) {
        if (data instanceof Buffer) {
            return (Buffer) data;
        }
        if (data instanceof JsonData) {
            return ((JsonData) data).toJson().toBuffer();
        }
        if (data instanceof JsonObject) {
            return ((JsonObject) data).toBuffer();
        }
        if (data instanceof JsonArray) {
            return ((JsonArray) data).toBuffer();
        }
        if (data instanceof ByteBuf) {
            return Buffer.buffer((ByteBuf) data);
        }
        return Json.encodeToBuffer(data);
    }

    private EventMessage(EventStatus status, EventAction action) {
        this(status, action, null, null, null, null);
    }

    private EventMessage(EventStatus status, EventAction action, @NonNull ErrorMessage error) {
        this(error, status, action, null);
    }

    private EventMessage(@NonNull ErrorMessage error, EventStatus status, EventAction action, EventAction prevAction) {
        this(status, action, prevAction, null, null, error);
    }

    private EventMessage(EventStatus status, EventAction action, Object data) {
        this(status, action, null, data, null, null);
    }

    private EventMessage(EventStatus status, EventAction action, EventAction prevAction, Object data) {
        this(status, action, prevAction, data, null, null);
    }

    public static EventMessage error(@NonNull EventAction action, @NonNull Throwable throwable) {
        return new EventMessage(EventStatus.FAILED, action, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(@NonNull EventAction action, @NonNull ErrorCode code, String message) {
        return new EventMessage(EventStatus.FAILED, action, ErrorMessage.parse(code, message));
    }

    public static EventMessage error(@NonNull EventAction action, @NonNull ErrorMessage message) {
        return new EventMessage(EventStatus.FAILED, action, message);
    }

    public static EventMessage error(@NonNull EventAction action, EventAction prevAction, @NonNull ErrorMessage error) {
        return new EventMessage(error, EventStatus.FAILED, action, prevAction);
    }

    public static EventMessage replyError(@NonNull EventAction action, @NonNull Throwable throwable) {
        return new EventMessage(ErrorMessage.parse(throwable), EventStatus.FAILED, EventAction.REPLY, action);
    }

    public static EventMessage replyError(@NonNull EventAction action, @NonNull ErrorMessage error) {
        return new EventMessage(error, EventStatus.FAILED, EventAction.REPLY, action);
    }

    public static EventMessage replySuccess(@NonNull EventAction action, Object data) {
        return success(EventAction.REPLY, action, data);
    }

    public static EventMessage initial(@NonNull EventAction action) {
        return new EventMessage(EventStatus.INITIAL, action);
    }

    public static EventMessage initial(@NonNull EventAction action, Object data) {
        return new EventMessage(EventStatus.INITIAL, action, data);
    }

    public static EventMessage success(@NonNull EventAction action) {
        return new EventMessage(EventStatus.SUCCESS, action);
    }

    public static EventMessage success(@NonNull EventAction action, Object data) {
        return new EventMessage(EventStatus.SUCCESS, action, data);
    }

    public static EventMessage success(@NonNull EventAction action, EventAction prevAction, Object data) {
        return new EventMessage(EventStatus.SUCCESS, action, prevAction, data);
    }

    public static EventMessage override(@NonNull EventMessage message, @NonNull EventAction action) {
        if (message.isError()) {
            return error(action, message.getAction(), message.getError());
        }
        return new EventMessage(message.getStatus(), action, message.getAction(), message.getData());
    }

    /**
     * Try parse given object to {@link EventMessage}
     *
     * @param object any non null object
     * @return event message instance
     * @throws QWEException if wrong format
     */
    public static EventMessage tryParse(@NonNull Object object) {
        return tryParse(object, false);
    }

    /**
     * Try parse with fallback data
     *
     * @param object  any non null object
     * @param lenient {@code true} if want to force given object to data json with {@code action} is {@link
     *                EventAction#UNKNOWN}
     * @return event message instance
     */
    public static EventMessage tryParse(@NonNull Object object, boolean lenient) {
        try {
            return JsonData.from(object, EventMessage.class, "Invalid event message format");
        } catch (QWEException e) {
            if (lenient) {
                return EventMessage.initial(EventAction.UNKNOWN, JsonData.tryParse(object));
            }
            throw e;
        }
    }

    /**
     * Get raw message data
     *
     * @return the raw message data
     */
    public @Nullable Buffer rawData() {
        return data == null ? null : this.data.copy();
    }

    /**
     * Get data in JsonObject format
     *
     * @return the message data in JsonObject
     * @apiNote If data is not json object, it will be force parsed to json object with key is {@link
     *     JsonData#SUCCESS_KEY}
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public @Nullable JsonObject getData() {
        if (Objects.isNull(data)) {
            return null;
        }
        if (ReflectionClass.assertDataType(dataClass, JsonData.class)) {
            return JsonData.from(data, (Class<? extends JsonData>) dataClass).toJson();
        }
        if (JsonData.isJsonObject(dataClass)) {
            return new JsonObject(data);
        }
        return JsonData.tryParse(data, true).toJson();
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> T parseAndGetData() {
        return (T) parseAndGetData(dataClass);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> T parseAndGetData(Class<T> dataClass) {
        return Objects.isNull(data)
               ? null
               : !ReflectionClass.assertDataType(dataClass, JsonData.class)
                 ? Json.decodeValue(data, dataClass)
                 : (T) JsonData.from(data, (Class<? extends JsonData>) dataClass);
    }

    @Override
    public JsonObject toJson(@NonNull ObjectMapper mapper) {
        final JsonObject json = JsonData.super.toJson(mapper);
        if (Objects.nonNull(data)) {
            return json.put("data", data.toJson());
        }
        return json;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == EventStatus.SUCCESS;
    }

    @JsonIgnore
    public boolean isError() {
        return this.status == EventStatus.FAILED;
    }

}
