package io.zero88.qwe;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.crypto.CryptoContext;
import io.zero88.qwe.crypto.CryptoRequest;

/**
 * @param <C> Type of Extension config
 * @param <E> Type of Extension Entrypoint
 * @see ExtensionConfig
 * @see ExtensionEntrypoint
 */
public interface Extension<C extends ExtensionConfig, E extends ExtensionEntrypoint<C>>
    extends HasConfig<C>, HasConfigKey, CryptoRequest {

    /**
     * Extension name
     *
     * @return extension name
     */
    default String extName() {
        return this.getClass().getName();
    }

    /**
     * Setup {@code Extension} when {@code Application} start
     *
     * @param sharedData    shared data proxy
     * @param appName       application name
     * @param appDir        application data dir
     * @param config        extension config
     * @param cryptoContext crypto request
     * @return a reference to this for fluent API
     */
    Extension<C, E> setup(@NotNull SharedDataLocalProxy sharedData, @NotNull String appName, @NotNull Path appDir,
                          @NotNull JsonObject config, @NotNull CryptoContext cryptoContext);

    /**
     * Stop {@code Extension} when {@code Application} stop
     */
    void stop();

    /**
     * Get extension entrypoint
     *
     * @return entrypoint
     */
    E entrypoint();

}
