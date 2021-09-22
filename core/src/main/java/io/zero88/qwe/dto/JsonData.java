package io.zero88.qwe.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.zero88.exceptions.HiddenException;
import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.DateTimes;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.json.JsonCodec;
import io.zero88.qwe.dto.jackson.QWEJsonCodec;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.QWEException;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import lombok.NonNull;

/**
 * Represents to Json Object data
 * <p>
 * If any data that is not {@code JsonObject}, JsonData parser will force parse data to json with {@link #SUCCESS_KEY}
 * or {@link #ERROR_KEY}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JsonData extends Serializable {

    ObjectMapper MAPPER = QWEJsonCodec.mapper();
    ObjectMapper LENIENT_MAPPER = QWEJsonCodec.lenientMapper();
    String SUCCESS_KEY = "data";
    String ERROR_KEY = "error";
    String FILTER_PROP_BY_NAME = "filter_by_name";

    static boolean isJsonObject(@NonNull Class<?> clazz) {
        return ReflectionClass.assertDataType(clazz, Map.class) ||
               ReflectionClass.assertDataType(clazz, JsonObject.class);
    }

    /**
     * Check if given class is able to added in json
     *
     * @param clazz Given class
     * @return {@code true} if able to added
     * @see JsonCodec
     */
    static boolean isAbleHandler(@NonNull Class<?> clazz) {
        return ReflectionClass.isJavaLangObject(clazz) || DateTimes.isRelatedToDateTime(clazz) ||
               ReflectionClass.assertDataType(clazz, Map.class) ||
               ReflectionClass.assertDataType(clazz, Collection.class) ||
               ReflectionClass.assertDataType(clazz, JsonObject.class) ||
               ReflectionClass.assertDataType(clazz, JsonArray.class);
    }

    static Object checkAndConvert(@NonNull Object val) {
        return isAbleHandler(val.getClass()) ? val : val.toString();
    }

    static <D> D safeGet(@NonNull JsonObject jsonObject, @NonNull String key, @NonNull Class<D> clazz) {
        return safeGet(jsonObject, key, clazz, null);
    }

    @SuppressWarnings("unchecked")
    static <D> D safeGet(@NonNull JsonObject jsonObject, @NonNull String key, @NonNull Class<D> clazz, D defValue) {
        final Object value = jsonObject.getValue(key);
        if (Objects.isNull(value)) {
            return defValue;
        }
        if (ReflectionClass.assertDataType(value.getClass(), clazz)) {
            if (value.getClass().isPrimitive() || clazz.isPrimitive()) {
                return (D) value;
            }
            return clazz.cast(value);
        }
        if (isJsonObject(clazz) && isJsonObject(value.getClass())) {
            return (D) JsonData.tryParse(clazz).toJson();
        }
        return defValue;
    }

    static <T> T convert(@NonNull JsonObject jsonObject, @NonNull Class<T> clazz) {
        return convert(jsonObject, clazz, MAPPER);
    }

    /**
     * Convert lenient with ignore unknown properties
     *
     * @param jsonObject json object
     * @param clazz      data type class
     * @param <T>        Expected Data Type
     * @return Expected instance
     * @throws IllegalArgumentException If conversion fails due to incompatible type
     */
    static <T> T convertLenient(@NonNull JsonObject jsonObject, @NonNull Class<T> clazz) {
        return convert(jsonObject, clazz, LENIENT_MAPPER);
    }

    static <T> T convert(@NonNull JsonObject jsonObject, @NonNull Class<T> clazz, @NonNull ObjectMapper mapper) {
        return mapper.convertValue(jsonObject.getMap(), clazz);
    }

    /**
     * Try parse {@code buffer} to {@code json data}
     *
     * @param buffer      Buffer data
     * @param isJson      Identify given {@code buffer} data is strictly {@code json object} or {@code json array}
     * @param useErrorKey Use whether {@link #ERROR_KEY} or {@link #SUCCESS_KEY} in case of fallback if given {@code
     *                    buffer} is not {@link JsonObject}
     * @return default {@code json data} instance
     */
    static JsonData tryParse(@NonNull Buffer buffer, boolean isJson, boolean useErrorKey) {
        return tryParse(buffer, isJson, useErrorKey ? ERROR_KEY : SUCCESS_KEY, false);
    }

    /**
     * Try parse {@code buffer} to {@code json data}
     *
     * @param buffer    Buffer data
     * @param isJson    Identify given {@code buffer} data is strictly {@code json object} or {@code json array}
     * @param backupKey Fallback key if given {@code buffer} is not {@link JsonObject}
     * @param isWrapped Whether output is needed to be wrapped or not
     * @return default {@code json data} instance
     */
    static JsonData tryParse(@NonNull Buffer buffer, boolean isJson, @NonNull String backupKey, boolean isWrapped) {
        return JsonDataImpl.tryParse(buffer, isJson, backupKey, isWrapped);
    }

    /**
     * Try parse {@code buffer} to {@code json data}
     *
     * @param buffer      Buffer data
     * @param isJson      Identify given {@code buffer} data is strictly {@code json object} or {@code json array}
     * @param useErrorKey Use whether {@link #ERROR_KEY} or {@link #SUCCESS_KEY} in case of fallback if given {@code
     *                    buffer} is not {@link JsonObject}
     * @param isWrapped   Whether output is needed to be wrapped or not
     * @return default {@code json data} instance
     */
    static JsonData tryParse(@NonNull Buffer buffer, boolean isJson, boolean useErrorKey, boolean isWrapped) {
        return tryParse(buffer, isJson, useErrorKey ? ERROR_KEY : SUCCESS_KEY, isWrapped);
    }

    /**
     * Try parse {@code buffer} to {@code json data} with {@link #SUCCESS_KEY}
     *
     * @param buffer Buffer data
     * @return default {@code json data} instance
     * @see JsonData#tryParse(Buffer, boolean, boolean, boolean)
     */
    static JsonData tryParse(@NonNull Buffer buffer) {
        return tryParse(buffer, false);
    }

    /**
     * Try parse {@code buffer} to {@code json data} with {@link #SUCCESS_KEY}
     *
     * @param buffer    Buffer data
     * @param isWrapped Whether output is needed to be wrapped or not
     * @return default {@code json data} instance
     * @see JsonData#tryParse(Buffer, boolean, boolean, boolean)
     */
    static JsonData tryParse(@NonNull Buffer buffer, boolean isWrapped) {
        return tryParse(buffer, false, false, isWrapped);
    }

    static JsonData tryParse(@NonNull Object obj) {
        return JsonDataImpl.tryParse(obj);
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz) {
        return from(object, clazz, "Invalid data format");
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, String errorMsg) {
        return from(object, clazz, MAPPER, errorMsg);
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, ObjectMapper mapper) {
        return from(object, clazz, mapper, null);
    }

    static <T extends JsonData> T from(@NonNull Object object, @NonNull Class<T> clazz, @NonNull ObjectMapper mapper,
                                       String errorMsg) {
        try {
            JsonObject entries = JsonDataSerializer.builder().mapper(mapper).build().apply(object);
            return mapper.convertValue(entries.getMap(), clazz);
        } catch (IllegalArgumentException | NullPointerException | DecodeException ex) {
            throw new QWEException(ErrorCode.INVALID_ARGUMENT, errorMsg, new HiddenException(ex));
        }
    }

    static FilterProvider ignoreFields(Set<String> ignoreFields) {
        return new SimpleFilterProvider().addFilter(FILTER_PROP_BY_NAME,
                                                    SimpleBeanPropertyFilter.serializeAllExcept(ignoreFields));
    }

    static <T extends JsonData> T merge(@NonNull JsonObject from, @NonNull JsonObject to, @NonNull Class<T> clazz) {
        return from(from.mergeIn(to, true), clazz);
    }

    default JsonObject toJson() {
        return toJson(getMapper());
    }

    default JsonObject toJson(@NonNull Set<String> ignoreFields) {
        return toJson(getMapper(), ignoreFields);
    }

    default JsonObject toJson(@NonNull ObjectMapper mapper) {
        return mapper.convertValue(this, JsonObject.class);
    }

    default JsonObject toJson(@NonNull ObjectMapper mapper, @NonNull Set<String> ignoreFields) {
        if (ignoreFields.isEmpty()) {
            return toJson(mapper);
        }
        return mapper.copy()
                     .addMixIn(JsonData.class, PropertyFilterMixIn.class)
                     .setFilterProvider(ignoreFields(ignoreFields))
                     .convertValue(this, JsonObject.class);
    }

    @JsonIgnore
    default ObjectMapper getMapper() {return MAPPER;}

    @JsonIgnore
    default ObjectMapper getPrettyMapper() {
        if (getMapper().equals(QWEJsonCodec.mapper())) {
            return QWEJsonCodec.prettyMapper();
        }
        return getMapper().copy().configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @JsonFilter(FILTER_PROP_BY_NAME)
    class PropertyFilterMixIn {}

}

