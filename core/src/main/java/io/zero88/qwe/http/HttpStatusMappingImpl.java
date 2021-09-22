package io.zero88.qwe.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.github.zero88.exceptions.HiddenException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.cluster.ClusterException;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.QWEException;

/**
 * @see ErrorCode
 */
public class HttpStatusMappingImpl implements HttpStatusMapping {

    private final Map<String, HttpResponseStatus> statusError = init();
    private final Map<String, Map<HttpMethod, HttpResponseStatus>> statusMethodError = initMethod();

    protected static Map<String, Map<HttpMethod, HttpResponseStatus>> initMethod() {
        Map<String, Map<HttpMethod, HttpResponseStatus>> map = new HashMap<>();

        Map<HttpMethod, HttpResponseStatus> notFound = new HashMap<>();
        HttpMethod.values().forEach(method -> notFound.put(method, HttpResponseStatus.GONE));
        notFound.put(HttpMethod.GET, HttpResponseStatus.NOT_FOUND);
        map.put(ErrorCode.DATA_NOT_FOUND.code(), notFound);

        return Collections.unmodifiableMap(map);
    }

    protected static Map<String, HttpResponseStatus> init() {
        Map<String, HttpResponseStatus> map = new HashMap<>();
        map.put(ErrorCode.INVALID_ARGUMENT.code(), HttpResponseStatus.BAD_REQUEST);
        map.put(HttpException.HTTP_ERROR.code(), HttpResponseStatus.BAD_REQUEST);

        map.put(ErrorCode.DATA_NOT_FOUND.code(), HttpResponseStatus.NOT_FOUND);
        map.put(ErrorCode.DATA_ALREADY_EXIST.code(), HttpResponseStatus.UNPROCESSABLE_ENTITY);
        map.put(ErrorCode.DATA_BEING_USED.code(), HttpResponseStatus.UNPROCESSABLE_ENTITY);

        map.put(ErrorCode.CONFLICT_ERROR.code(), HttpResponseStatus.CONFLICT);
        map.put(ErrorCode.UNSUPPORTED.code(), HttpResponseStatus.CONFLICT);

        map.put(ErrorCode.AUTHENTICATION_ERROR.code(), HttpResponseStatus.UNAUTHORIZED);
        map.put(ErrorCode.SECURITY_ERROR.code(), HttpResponseStatus.FORBIDDEN);
        map.put(ErrorCode.INSUFFICIENT_PERMISSION_ERROR.code(), HttpResponseStatus.FORBIDDEN);

        map.put(ClusterException.CODE.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        map.put(ErrorCode.SERVICE_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        map.put(ErrorCode.SERVICE_NOT_FOUND.code(), HttpResponseStatus.SERVICE_UNAVAILABLE);
        map.put(ErrorCode.SERVICE_UNAVAILABLE.code(), HttpResponseStatus.SERVICE_UNAVAILABLE);

        map.put(ErrorCode.TIMEOUT_ERROR.code(), HttpResponseStatus.REQUEST_TIMEOUT);
        return Collections.unmodifiableMap(map);
    }

    public HttpResponseStatus success(HttpMethod method) {
        if (HttpMethod.DELETE == method) {
            return HttpResponseStatus.NO_CONTENT;
        }
        if (HttpMethod.POST == method) {
            return HttpResponseStatus.CREATED;
        }
        return HttpResponseStatus.OK;
    }

    public HttpResponseStatus error(HttpMethod method, QWEException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof HiddenException) {
            return error(method, ErrorCode.wrap(((HiddenException) cause).errorCode()));
        }
        return error(method, exception.errorCode());
    }

    public HttpResponseStatus error(HttpMethod method, io.github.zero88.exceptions.ErrorCode errorCode) {
        final Map<HttpMethod, HttpResponseStatus> methodStatus = statusMethodError.get(errorCode.code());
        return Optional.ofNullable(methodStatus)
                       .map(m -> m.get(method))
                       .orElseGet(() -> Optional.ofNullable(statusError.get(errorCode.code()))
                                                .orElse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
    }

    public ErrorCode error(HttpMethod method, int code) {
        return error(method, HttpResponseStatus.valueOf(code));
    }

    public ErrorCode error(HttpMethod method, HttpResponseStatus status) {
        return statusMethodError.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue()
                                                      .entrySet()
                                                      .stream()
                                                      .anyMatch(e -> e.getKey() == method && e.getValue() == status))
                                .map(Entry::getKey)
                                .findFirst()
                                .map(ErrorCode::parse)
                                .orElseGet(() -> statusError.entrySet()
                                                            .stream()
                                                            .filter(entry -> entry.getValue() == status)
                                                            .map(Entry::getKey)
                                                            .findFirst()
                                                            .map(ErrorCode::parse)
                                                            .orElse(ErrorCode.wrap(ErrorCode.UNKNOWN_ERROR)));
    }

}
