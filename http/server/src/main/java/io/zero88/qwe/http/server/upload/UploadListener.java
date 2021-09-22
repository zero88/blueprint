package io.zero88.qwe.http.server.upload;

import java.util.Objects;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.eventbus.EBContext;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;

import lombok.RequiredArgsConstructor;

/**
 * Upload listener to handle uploaded file (update database, transfer to another host)
 */
@RequiredArgsConstructor
public class UploadListener implements EventListener {

    public static UploadListener create(String listenerClass) {
        if (Strings.isBlank(listenerClass) || UploadListener.class.getName().equals(listenerClass)) {
            return new UploadListener();
        }
        return Objects.requireNonNull(ReflectionClass.createObject(listenerClass));
    }

    @EBContract(action = "CREATE")
    public Future<JsonObject> create(@EBContext SharedDataLocalProxy sharedData, JsonObject data) {
        logger().info(decor("Upload metadata::{}"), data);
        return Future.succeededFuture(data);
    }

}
