package io.zero88.qwe.storage.json;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import io.github.zero88.repl.Arguments;
import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Functions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.zero88.qwe.ExtensionEntrypoint;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBContext;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.file.TextFileOperator;
import io.zero88.qwe.file.TextFileOperatorImpl;
import io.zero88.qwe.file.converter.BufferConverter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JsonStorageService implements EventListener, ExtensionEntrypoint<JsonStorageConfig>, HasLogger {

    @SuppressWarnings("unchecked")
    public static <T extends JsonStorageService> T create(@NonNull Path rootDir, @NonNull JsonStorageConfig config) {
        return ReflectionClass.createObject((Class<T>) config.serviceHandlerClass(),
                                            new Arguments().put(Path.class, rootDir)
                                                           .put(JsonStorageConfig.class, config));
    }

    @Getter(value = AccessLevel.PACKAGE)
    private final Path rootDir;
    @Getter
    @Accessors(fluent = true)
    private final JsonStorageConfig extConfig;

    @EBContract(action = "CREATE_OR_UPDATE")
    public Future<JsonObject> createOrUpdate(@EBContext Vertx vertx, RequestData requestData) {
        final JsonInput ji = parse(requestData);
        final JsonPointer p = ji.pointer();
        return this.loadJson(vertx, ji)
                   .map(j -> Objects.requireNonNull(p.writeJson(j, ji.getDataToInsert(), true), "Unable to write"))
                   .flatMap(o -> writeJson(vertx, ji, o).map(ignore -> p.isRootPointer() ? o : p.queryJson(o)))
                   .map(o -> new JsonObject().put(ji.getOutputKey(), o));
    }

    @EBContract(action = "REMOVE")
    public Future<JsonObject> remove(@EBContext Vertx vertx, RequestData requestData) {
        final JsonInput ji = parse(requestData);
        if (Objects.isNull(ji.getKeyToRemove())) {
            throw new IllegalArgumentException("Key to remove is mandatory");
        }
        final JsonPointer pointer = ji.pointer();
        return this.loadJson(vertx, ji)
                   .flatMap(json -> this.remove(pointer, ji, json)
                                        .flatMap(o -> this.writeJson(vertx, ji, json).map(ignore -> o)))
                   .map(o -> new JsonObject().put(ji.getOutputKey(), o))
                   .otherwise(new JsonObject());
    }

    @EBContract(action = "QUERY")
    public Future<JsonObject> query(@EBContext Vertx vertx, RequestData requestData) {
        final JsonInput ji = parse(requestData);
        return loadJson(vertx, ji).map(json -> new JsonObject().put(ji.getOutputKey(), ji.pointer().queryJson(json)));
    }

    @EBContract(action = "HAS")
    public Future<JsonObject> has(@EBContext Vertx vertx, RequestData requestData) {
        final JsonInput ji = parse(requestData);
        return this.loadJson(vertx, ji)
                   .map(json -> new JsonObject().put(ji.getOutputKey(), Objects.nonNull(ji.pointer().queryJson(json))));
    }

    protected Future<?> remove(JsonPointer pointer, JsonInput ji, JsonObject json) {
        final String keyToRemove = ji.getKeyToRemove().toString();
        final Object o = pointer.queryJson(json);
        if (o instanceof JsonObject) {
            return Optional.ofNullable(((JsonObject) o).remove(keyToRemove))
                           .map(v -> ji.isSkipRemovedKeyInOutput() ? v : new JsonObject().put(keyToRemove, v))
                           .map(Future::succeededFuture)
                           .orElseGet(Future::succeededFuture);
        }
        if (o instanceof JsonArray) {
            return Functions.getIfThrow(() -> Integer.parseInt(keyToRemove))
                            .flatMap(idx -> Optional.ofNullable(((JsonArray) o).remove((int) idx)))
                            .map(Future::succeededFuture)
                            .orElseGet(Future::succeededFuture);
        }
        return Future.succeededFuture();
    }

    protected JsonInput parse(RequestData requestData) {
        return JsonData.from(requestData.body(), JsonInput.class);
    }

    protected Future<JsonArray> loadArray(Vertx vertx, @NonNull JsonInput ji) {
        return operator(vertx).loadArray(rootDir.resolve(ji.getFile()),
                                         Optional.ofNullable(ji.getFileOption()).orElse(extConfig.getOption()));
    }

    protected Future<JsonObject> loadJson(Vertx vertx, @NonNull JsonInput ji) {
        return operator(vertx).loadJson(rootDir.resolve(ji.getFile()),
                                        Optional.ofNullable(ji.getFileOption()).orElse(extConfig.getOption()));
    }

    protected Future<Path> writeJson(Vertx vertx, @NonNull JsonInput ji, Object data) {
        return operator(vertx).write(rootDir.resolve(ji.getFile()),
                                     Optional.ofNullable(ji.getFileOption()).orElse(extConfig.getOption()),
                                     JsonData.tryParse(data).toJson(), BufferConverter.JSON_OBJECT_CONVERTER);
    }

    private TextFileOperator operator(Vertx vertx) {
        return TextFileOperatorImpl.builder().vertx(vertx).maxSize(extConfig.getMaxSizeInMB()).build();
    }

}
