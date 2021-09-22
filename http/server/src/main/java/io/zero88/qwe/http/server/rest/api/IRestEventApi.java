package io.zero88.qwe.http.server.rest.api;

import java.util.List;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.http.EventHttpApi;
import io.zero88.qwe.http.HttpUtils;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.rest.handler.RestEventApiDispatcher;

/**
 * Represents for REST event API
 *
 * @param <C> type of {@link RouterConfig}
 * @see EventHttpApi
 * @since 1.0.0
 */
public interface IRestEventApi<C extends RouterConfig> extends EventHttpApi {

    IRestEventApi<C> setup(C config, SharedDataLocalProxy sharedData);

    default List<String> contentTypes() {
        return HttpUtils.JSON_CONTENT_TYPES;
    }

    default EventPattern pattern() {
        return EventPattern.REQUEST_RESPONSE;
    }

    default <T extends RestEventApiDispatcher> Class<T> dispatcher() {
        return null;
    }

}
