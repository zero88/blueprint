package io.zero88.qwe.http;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.zero88.qwe.eventbus.EventAction;
import io.vertx.core.http.HttpMethod;

import lombok.NonNull;

public interface ActionMethodMapping extends Supplier<Map<EventAction, HttpMethod>> {

    /**
     * Default mapping for common {@code CREATE | READ | UPDATE | DELETE} operations
     *
     * @see #defaultCRUDMap()
     */
    ActionMethodMapping CRUD_MAP = ActionMethodMapping.create(defaultCRUDMap());

    /**
     * Default mapping for {@code READ | UPDATE | DELETE} operations
     *
     * @see #defaultRUDMap()
     */
    ActionMethodMapping RUD_MAP = ActionMethodMapping.create(defaultRUDMap());

    /**
     * Default mapping for {@code CREATE_OR_UPDATE | READ | DELETE} operations
     *
     * @see #defaultCRDMap()
     */
    ActionMethodMapping CRD_MAP = ActionMethodMapping.create(defaultCRDMap());

    /**
     * Default mapping for common {@code CREATE | UPDATE | DELETE} operations
     *
     * @see #defaultDMLMap()
     */
    ActionMethodMapping DML_MAP = ActionMethodMapping.create(defaultDMLMap());

    /**
     * Default mapping for common {@code BATCH_CREATE | BATCH_UPDATE | BATCH_DELETE} operations
     *
     * @see #defaultBatchDMLMap()
     */
    ActionMethodMapping BATCH_DML_MAP = ActionMethodMapping.create(defaultBatchDMLMap());

    /**
     * Default mapping for reading {@code GET | GET_LIST} operations
     *
     * @see #defaultDQLMap()
     */
    ActionMethodMapping DQL_MAP = ActionMethodMapping.create(defaultDQLMap());

    static ActionMethodMapping create(@NonNull Map<EventAction, HttpMethod> map) {
        return () -> Collections.unmodifiableMap(map);
    }

    static ActionMethodMapping byCRUD(@NonNull Collection<EventAction> available) {
        return by(CRUD_MAP, available);
    }

    static ActionMethodMapping by(@NonNull ActionMethodMapping base, @NonNull Collection<EventAction> available) {
        Map<EventAction, HttpMethod> baseMap = base.get();
        return ActionMethodMapping.create(
            available.stream().filter(baseMap::containsKey).collect(Collectors.toMap(e -> e, baseMap::get)));
    }

    static Map<EventAction, HttpMethod> defaultCRUDMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultCRDMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE_OR_UPDATE, HttpMethod.PUT);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultRUDMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultDMLMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultDQLMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultBatchDMLMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.BATCH_CREATE, HttpMethod.POST);
        map.put(EventAction.BATCH_UPDATE, HttpMethod.PUT);
        map.put(EventAction.BATCH_PATCH, HttpMethod.PATCH);
        map.put(EventAction.BATCH_DELETE, HttpMethod.DELETE);
        return map;
    }

    default boolean hasDuplicateMethod() {
        return get().size() != new HashSet<>(get().values()).size();
    }

}
