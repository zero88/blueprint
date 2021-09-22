package io.zero88.qwe.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.zero88.exceptions.HiddenException;
import io.github.zero88.utils.Strings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.cluster.ClusterException;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.ServiceException;

public class HttpStatusMappingTest {

    private static HttpStatusMapping httpStatusMapping;

    @BeforeAll
    static void load() {
        httpStatusMapping = HttpStatusMappingLoader.getInstance().get();
    }

    @Test
    public void test_success_ok() {
        Assertions.assertEquals(HttpResponseStatus.OK, httpStatusMapping.success(HttpMethod.GET));
        Assertions.assertEquals(HttpResponseStatus.OK, httpStatusMapping.success(HttpMethod.PUT));
        Assertions.assertEquals(HttpResponseStatus.OK, httpStatusMapping.success(HttpMethod.PATCH));
        Assertions.assertEquals(HttpResponseStatus.OK, httpStatusMapping.success(HttpMethod.HEAD));
        Assertions.assertEquals(HttpResponseStatus.OK, httpStatusMapping.success(HttpMethod.OPTIONS));
    }

    @Test
    public void test_success_post() {
        Assertions.assertEquals(HttpResponseStatus.CREATED, httpStatusMapping.success(HttpMethod.POST));
    }

    @Test
    public void test_success_delete() {
        Assertions.assertEquals(HttpResponseStatus.NO_CONTENT, httpStatusMapping.success(HttpMethod.DELETE));
    }

    @Test
    public void test_error_bad_request() {
        Assertions.assertEquals(HttpResponseStatus.BAD_REQUEST,
                                httpStatusMapping.error(HttpMethod.DELETE, HttpException.HTTP_ERROR));
        Assertions.assertEquals(HttpResponseStatus.BAD_REQUEST,
                                httpStatusMapping.error(HttpMethod.POST, ErrorCode.INVALID_ARGUMENT));
    }

    @Test
    public void test_error_gone() {
        HttpMethod.values()
                  .parallelStream()
                  .filter(method -> HttpMethod.GET != method)
                  .forEach(method -> Assertions.assertEquals(HttpResponseStatus.GONE,
                                                             httpStatusMapping.error(method, ErrorCode.DATA_NOT_FOUND),
                                                             "HTTP Method: " + method));
    }

    @Test
    public void test_error_not_found() {
        Assertions.assertEquals(HttpResponseStatus.NOT_FOUND,
                                httpStatusMapping.error(HttpMethod.GET, ErrorCode.DATA_NOT_FOUND));
    }

    @Test
    public void test_error_internal_server_error() {
        Assertions.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                httpStatusMapping.error(HttpMethod.DELETE, ErrorCode.UNKNOWN_ERROR));
        Assertions.assertEquals(HttpResponseStatus.SERVICE_UNAVAILABLE,
                                httpStatusMapping.error(HttpMethod.GET, ErrorCode.SERVICE_NOT_FOUND));
    }

    @Test
    public void test_error_service_unavailable() {
        Map<ErrorCode, List<HttpMethod>> test = new HashMap<>();
        test.put(ErrorCode.SERVICE_ERROR, HttpMethod.values());
        test.put(ClusterException.CODE, HttpMethod.values());
        test.entrySet()
            .stream()
            .parallel()
            .forEach(e -> e.getValue()
                           .parallelStream()
                           .forEach(method -> Assertions.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                                      httpStatusMapping.error(method, e.getKey()),
                                                                      Strings.format("Method: {0} | Code: {1}", method,
                                                                                     e.getKey()))));
    }

    @Test
    public void test_error_by_exception_without_hidden() {
        Assertions.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                httpStatusMapping.error(HttpMethod.GET, new ServiceException("hey")));
    }

    @Test
    public void test_error_by_exception_with_hidden() {
        ServiceException t = new ServiceException("Hey", new HiddenException(ClusterException.CODE, "xx", null));
        Assertions.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, httpStatusMapping.error(HttpMethod.GET, t));
    }

}
