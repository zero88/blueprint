package io.zero88.qwe.http.server.upload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.zero88.utils.FileUtils;
import io.github.zero88.utils.HttpScheme;
import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.event.EventAction;
import io.zero88.qwe.event.EventBusClient;
import io.zero88.qwe.event.EventMessage;
import io.zero88.qwe.event.EventModel;
import io.zero88.qwe.http.HttpUtils.HttpHeaderUtils;
import io.zero88.qwe.http.server.rest.handler.RestEventRequestDispatcher;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Only override it if any performance issue
 */
@Getter
@RequiredArgsConstructor
public class UploadFileHandler implements RestEventRequestDispatcher {

    @NonNull
    @Accessors(fluent = true)
    private final EventBusClient eventbus;
    private final EventModel eventModel;
    private final Path uploadDir;
    private final String publicUrl;

    public static UploadFileHandler create(String handlerClass, @NonNull EventBusClient controller,
                                           @NonNull EventModel eventModel, @NonNull Path uploadDir, String publicUrl) {
        if (Strings.isBlank(handlerClass) || UploadFileHandler.class.getName().equals(handlerClass)) {
            return new UploadFileHandler(controller, eventModel, uploadDir, publicUrl);
        }
        Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(EventBusClient.class, controller);
        inputs.put(EventModel.class, eventModel);
        inputs.put(Path.class, uploadDir);
        inputs.put(String.class, publicUrl);
        return ReflectionClass.createObject(handlerClass, inputs);
    }

    @Override
    public void handle(RoutingContext context) {
        if (context.fileUploads().isEmpty()) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            return;
        }
        String link = Strings.isBlank(publicUrl) ? Urls.buildURL(HttpScheme.parse(context.request().scheme()),
                                                                 context.request().host(), -1) : publicUrl;
        JsonObject data = new JsonObject();
        context.fileUploads().forEach(fileUpload -> data.put(fileUpload.name(), extractFileInfo(link, fileUpload)));
        data.put("attributes", HttpHeaderUtils.serializeHeaders(context.request().formAttributes()));
        EventMessage message = EventMessage.initial(
            eventModel.getEvents().stream().findFirst().orElse(EventAction.CREATE), data);
        dispatch(context, "UPLOAD", eventModel.getAddress(), eventModel.getPattern(), message);
    }

    private JsonObject extractFileInfo(String link, FileUpload fileUpload) {
        return new JsonObject().put("fileName", fileUpload.fileName())
                               .put("file", uploadDir.relativize(Paths.get(fileUpload.uploadedFileName())).toString())
                               .put("ext", FileUtils.getExtension(fileUpload.fileName()))
                               .put("charset", fileUpload.charSet())
                               .put("contentType", fileUpload.contentType())
                               .put("size", fileUpload.size())
                               .put("serverUrl", link);
    }

}
