package io.zero88.qwe.http.server.download;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import io.github.zero88.utils.FileUtils;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.DownloadSystem;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.http.server.config.FileDownloadConfig;

import lombok.NonNull;

public class DownloadRouterCreator implements RouterCreator<FileDownloadConfig>, DownloadSystem {

    @Override
    public Function<HttpServerConfig, FileDownloadConfig> lookupConfig() {
        return HttpServerConfig::getFileDownloadConfig;
    }

    @Override
    public Router subRouter(@NonNull SharedDataLocalProxy sharedData, @NonNull Path pluginDir,
                            @NonNull FileDownloadConfig config) {
        final String downloadDir = FileUtils.createFolder(pluginDir, config.getDownloadDir());
        logger().info(decor("Setup download dir[{}]"), downloadDir);
        final Router router = Router.router(sharedData.getVertx());
        router.get(RouterCreator.addWildcards("/"))
              .handler(StaticHandler.create()
                                    .setEnableRangeSupport(true)
                                    .setSendVaryHeader(true)
                                    .setFilesReadOnly(false)
                                    .setAllowRootFileSystemAccess(true)
                                    .setIncludeHidden(false)
                                    .setWebRoot(downloadDir))
              .handler(DownloadFileHandler.create(config.getHandlerClass(), config.getPath(), Paths.get(downloadDir)));
        return router;
    }

}
