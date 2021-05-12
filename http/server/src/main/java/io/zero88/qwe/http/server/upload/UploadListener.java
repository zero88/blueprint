package io.zero88.qwe.http.server.upload;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.event.EBContract;
import io.zero88.qwe.event.EventAction;
import io.zero88.qwe.event.EventListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Upload listener to handle uploaded file (update database, transfer to another host)
 */
@RequiredArgsConstructor
public class UploadListener implements EventListener {

    protected final SharedDataLocalProxy proxy;
    protected final List<EventAction> actions;

    public static UploadListener create(SharedDataLocalProxy proxy, String listenerClass,
                                        @NonNull List<EventAction> actions) {
        if (Strings.isBlank(listenerClass) || UploadListener.class.getName().equals(listenerClass)) {
            return new UploadListener(proxy, actions);
        }
        Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(SharedDataLocalProxy.class, proxy);
        inputs.put(List.class, actions);
        return ReflectionClass.createObject(listenerClass, inputs);
    }

    @EBContract(action = "CREATE")
    public Future<JsonObject> create(JsonObject data) { return Future.succeededFuture(data); }

}
