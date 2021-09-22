package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.UploadSystem;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.upload.UploadFileHandler;
import io.zero88.qwe.http.server.upload.UploadListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
public final class FileUploadConfig extends AbstractRouterConfig implements RouterConfig, UploadSystem {

    public static final String NAME = "__upload__";

    private int maxBodySizeMB = 10;
    private String handlerClass = UploadFileHandler.class.getName();
    private String listenerAddress = UploadListener.class.getName();
    private String listenerClass = UploadListener.class.getName();
    private String uploadDir = "files";

    public FileUploadConfig() {
        super(NAME, HttpServerConfig.class);
    }

    @Override
    protected @NonNull String defaultPath() {
        return "/u";
    }

}
