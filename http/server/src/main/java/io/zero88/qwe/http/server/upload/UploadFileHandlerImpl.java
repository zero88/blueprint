package io.zero88.qwe.http.server.upload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.FileUtils;
import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.auth.UserInfo;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.http.server.handler.AuthInterceptor;
import io.zero88.qwe.http.server.handler.RequestDataInterceptor;
import io.zero88.qwe.utils.JsonUtils.JsonCollectors;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Only override it if any performance issue
 */
public class UploadFileHandlerImpl implements UploadFileHandler {

    @Getter
    @Accessors(fluent = true)
    private AuthInterceptor authInterceptor;
    private String sharedKey;
    private String address;
    private Path uploadDir;

    public static UploadFileHandlerImpl create(String handlerClass) {
        if (Strings.isBlank(handlerClass)) {
            return new UploadFileHandlerImpl();
        }
        return Objects.requireNonNull(ReflectionClass.createObject(handlerClass));
    }

    public UploadFileHandlerImpl setup(String sharedKey, String address, Path uploadDir,
                                       ReqAuthDefinition authDefinition) {
        this.sharedKey = sharedKey;
        this.address = address;
        this.uploadDir = uploadDir;
        this.authInterceptor = AuthInterceptor.create(authDefinition);
        return this;
    }

    @Override
    public RequestDataInterceptor validator() {
        return RequestDataInterceptor.createForUpload().andThen((context, requestData) -> {
            if (context.fileUploads().isEmpty()) {
                throw new IllegalArgumentException("Missing upload file(s)");
            }
            return requestData;
        });
    }

    @Override
    public Future<EventMessage> proceed(RoutingContext context, UserInfo userInfo, RequestData reqData) {
        reqData.body().put("files", context.fileUploads().stream().collect(JsonCollectors.toArray(this::extractFile)));
        return doDispatch(EventBusClient.create(SharedDataLocalProxy.create(context.vertx(), sharedKey)), address,
                          EventPattern.REQUEST_RESPONSE,
                          EventMessage.initial(EventAction.CREATE, reqData).setUserInfo(userInfo));
    }

    private JsonObject extractFile(FileUpload fileUpload) {
        return new JsonObject().put("name", fileUpload.name())
                               .put("fileName", fileUpload.fileName())
                               .put("file", uploadDir.relativize(Paths.get(fileUpload.uploadedFileName())).toString())
                               .put("ext", FileUtils.getExtension(fileUpload.fileName()))
                               .put("charset", fileUpload.charSet())
                               .put("contentType", fileUpload.contentType())
                               .put("transferEncoding", fileUpload.contentTransferEncoding())
                               .put("size", fileUpload.size());
    }

}
