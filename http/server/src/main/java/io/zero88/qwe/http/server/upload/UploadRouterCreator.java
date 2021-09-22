package io.zero88.qwe.http.server.upload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import io.github.zero88.utils.FileUtils;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.http.HttpUtils;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.UploadSystem;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.http.server.config.FileUploadConfig;

import lombok.NonNull;

public final class UploadRouterCreator implements RouterCreator<FileUploadConfig>, UploadSystem {

    @Override
    public Function<HttpServerConfig, FileUploadConfig> lookupConfig() {
        return HttpServerConfig::getFileUploadConfig;
    }

    @Override
    public Router subRouter(@NonNull SharedDataLocalProxy sharedData, @NonNull Path pluginDir,
                            @NonNull FileUploadConfig config) {
        final String upDir = FileUtils.createFolder(pluginDir, config.getUploadDir());
        EventBusClient.create(sharedData)
                      .register(config.getListenerAddress(), UploadListener.create(config.getListenerClass()));
        Router router = Router.router(sharedData.getVertx());
        //TODO implement it
        ReqAuthDefinition authDefinition = ReqAuthDefinition.noAuth();
        RouterCreator.addContentType(router.post(config.getPath()), HttpUtils.JSON_CONTENT_TYPES)
                     .handler(BodyHandler.create(upDir).setBodyLimit(config.getMaxBodySizeMB() * HttpServerConfig.MB))
                     .handler(UploadFileHandler.create(config.getHandlerClass())
                                               .setup(sharedData.sharedKey(), config.getListenerAddress(),
                                                      Paths.get(upDir), authDefinition));
        return router;
    }

}
