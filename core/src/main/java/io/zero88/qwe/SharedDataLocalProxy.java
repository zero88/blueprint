package io.zero88.qwe;

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.zero88.qwe.eventbus.EventBusClient;

/**
 * Represents for shared local data from {@code Application} to {@code Plugin(s)}
 */
@SuppressWarnings("unchecked")
public interface SharedDataLocalProxy extends HasSharedKey {

    /**
     * Data key for EventBus delivery option
     *
     * @see EventBusClient
     */
    String EVENTBUS_DELIVERY_OPTION_KEY = "EVENTBUS_OPTION";
    String PUBLIC_IPV4_KEY = "PUBLIC_IPV4";

    Vertx getVertx();

    default <D> D getData(String dataKey) {
        return getData(dataKey, null);
    }

    default <D> D getData(String dataKey, D fallback) {
        return (D) localData().getOrDefault(dataKey, fallback);
    }

    default <D> D addData(String dataKey, D data) {
        return (D) localData().put(dataKey, data);
    }

    default <D> D removeData(String dataKey) {
        return (D) localData().remove(dataKey);
    }

    default LocalMap<Object, Object> localData() {
        return getVertx().sharedData().getLocalMap(sharedKey());
    }

    default SharedData unwrap() {
        return getVertx().sharedData();
    }

    static SharedDataLocalProxy create(Vertx vertx, String sharedKey) {
        return new SharedDataLocalProxy() {
            @Override
            public Vertx getVertx() {
                return vertx;
            }

            @Override
            public String sharedKey() {
                return sharedKey;
            }
        };
    }

}
