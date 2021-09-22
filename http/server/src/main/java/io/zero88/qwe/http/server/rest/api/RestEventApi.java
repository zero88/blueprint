package io.zero88.qwe.http.server.rest.api;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.http.server.config.ApiConfig;

/**
 * Represents for the resource API that is connected via {@code HTTP} protocol but its handler relies on {@code
 * EventBus}
 */
public interface RestEventApi extends IRestEventApi<ApiConfig> {

    @Override
    default RestEventApi setup(ApiConfig config, SharedDataLocalProxy sharedData) {
        return this;
    }

}
