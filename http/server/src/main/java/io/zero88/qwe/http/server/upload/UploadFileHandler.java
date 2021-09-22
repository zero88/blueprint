package io.zero88.qwe.http.server.upload;

import java.nio.file.Path;
import java.util.Objects;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.http.server.handler.EventBusProxyDispatcher;

public interface UploadFileHandler extends EventBusProxyDispatcher {

    static UploadFileHandler create(String handlerClass) {
        if (Strings.isBlank(handlerClass) || UploadFileHandler.class.getName().equals(handlerClass)) {
            return new UploadFileHandlerImpl();
        }
        return Objects.requireNonNull(ReflectionClass.createObject(handlerClass));
    }

    UploadFileHandler setup(String sharedKey, String address, Path uploadDir, ReqAuthDefinition authDefinition);

}
