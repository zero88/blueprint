package io.zero88.qwe.http.server.config;

import io.zero88.qwe.http.server.HttpServerConfig;
import io.zero88.qwe.http.server.HttpSystem.DownloadSystem;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.download.DownloadFileHandler;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class FileDownloadConfig extends AbstractRouterConfig implements RouterConfig, DownloadSystem {

    public static final String NAME = "__download__";

    private String handlerClass = DownloadFileHandler.class.getName();
    private String downloadDir = "files";

    public FileDownloadConfig() {
        super(NAME, HttpServerConfig.class);
    }

    @Override
    protected @NonNull String defaultPath() {
        return "/f";
    }

}
