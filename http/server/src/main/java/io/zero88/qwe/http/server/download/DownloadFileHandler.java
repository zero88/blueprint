package io.zero88.qwe.http.server.download;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.repl.Arguments;
import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.exceptions.DataNotFoundException;
import io.zero88.qwe.http.server.HttpSystem.DownloadSystem;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Download file handler helps extract {@code URL path} to {@code file id} then do some stuffs (e.g: find record in
 * Database) to lookup actual file.
 */
@Getter
@RequiredArgsConstructor
public abstract class DownloadFileHandler implements Handler<RoutingContext>, DownloadSystem {

    private final Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);
    private final String downloadPath;
    private final Path downloadDir;

    public static DownloadFileHandler create(String handlerClass, String downloadPath, @NonNull Path downloadDir) {
        if (Strings.isBlank(handlerClass) || DownloadFileHandler.class.getName().equals(handlerClass)) {
            return new DownloadFileHandler(downloadPath, downloadDir) {
                protected Path getFilePath(String fileId) {
                    return downloadDir.resolve(fileId);
                }
            };
        }
        return ReflectionClass.createObject(handlerClass, new Arguments().put(String.class, downloadPath)
                                                                         .put(Path.class, downloadDir));
    }

    @Override
    public void handle(RoutingContext context) {
        final String fileId = context.request().path().replaceFirst(downloadPath, "");
        final Path filePath = getFilePath(fileId);
        if (filePath.toFile().exists()) {
            context.response()
                   .setChunked(true)
                   .setStatusCode(HttpResponseStatus.OK.code())
                   .sendFile(filePath.toString())
                   .onFailure(t -> logger.warn(decor("Something wrong when sending file"), t));
        } else {
            throw new DataNotFoundException(decor("Not found file: " + fileId + " in system"));
        }
    }

    protected abstract Path getFilePath(String fileId);

}
