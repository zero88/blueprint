package io.zero88.qwe.dto.jackson;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.spi.json.JsonCodec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SuppressWarnings("unchecked")
public final class QWEJsonCodec implements JsonCodec {

    private static final ObjectMapper MAPPER = setup(DatabindCodec.mapper());
    private static final ObjectMapper PRETTY = setup(DatabindCodec.prettyMapper());
    private static final ObjectMapper LENIENT = mapper().copy()
                                                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                                   false);

    private static ObjectMapper setup(ObjectMapper mapper) {
        return mapper.registerModule(new JavaTimeModule())
                     .registerModule(JsonModule.BASIC)
                     .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                              SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
    }

    /**
     * @return the {@link ObjectMapper} used for data binding.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * @return the {@link ObjectMapper} used for data binding configured for indenting output.
     */
    public static ObjectMapper prettyMapper() {
        return PRETTY;
    }

    public static ObjectMapper lenientMapper() {
        return LENIENT;
    }

    public static Buffer toBuffer(Object object, ObjectMapper mapper) {
        try {
            return Buffer.buffer(mapper.writeValueAsBytes(object));
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    @Override
    public <T> T fromValue(Object json, Class<T> clazz) {
        T value = QWEJsonCodec.MAPPER.convertValue(json, clazz);
        if (clazz == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public <T> T fromString(String str, Class<T> clazz) throws DecodeException {
        return fromParser(createParser(str), clazz);
    }

    @Override
    public <T> T fromBuffer(Buffer buf, Class<T> clazz) throws DecodeException {
        return fromParser(createParser(buf), clazz);
    }

    public static JsonParser createParser(Buffer buf) {
        try {
            return QWEJsonCodec.MAPPER.getFactory()
                                      .createParser((InputStream) new ByteBufInputStream(buf.getByteBuf()));
        } catch (IOException e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        }
    }

    public static JsonParser createParser(String str) {
        try {
            return QWEJsonCodec.MAPPER.getFactory().createParser(str);
        } catch (IOException e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        }
    }

    public static <T> T fromParser(JsonParser parser, Class<T> type) throws DecodeException {
        T value;
        JsonToken remaining;
        try {
            value = QWEJsonCodec.MAPPER.readValue(parser, type);
            remaining = parser.nextToken();
        } catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        } finally {
            close(parser);
        }
        if (remaining != null) {
            throw new DecodeException("Unexpected trailing token");
        }
        if (type == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public String toString(Object object, boolean pretty) throws EncodeException {
        try {
            ObjectMapper mapper = pretty ? QWEJsonCodec.PRETTY : QWEJsonCodec.MAPPER;
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    @Override
    public Buffer toBuffer(Object object, boolean pretty) throws EncodeException {
        return toBuffer(object, pretty ? QWEJsonCodec.PRETTY : QWEJsonCodec.MAPPER);
    }

    private static Object adapt(Object o) {
        try {
            if (o instanceof List) {
                List list = (List) o;
                return new JsonArray(list);
            } else if (o instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) o;
                return new JsonObject(map);
            }
            return o;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage());
        }
    }

    static void close(Closeable parser) {
        try {
            parser.close();
        } catch (IOException ignore) {
        }
    }

}
