package io.zero88.qwe.http.server.rest.handler;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.dto.msg.ResponseData;
import io.zero88.qwe.http.server.HttpSystem.GatewaySystem;
import io.zero88.qwe.http.server.handler.RequestDispatcher;
import io.zero88.qwe.http.server.handler.ResponseDataInterceptor;
import io.zero88.qwe.http.server.handler.ResponseInterceptor;
import io.zero88.qwe.http.server.rest.api.ProxyServiceApi;
import io.zero88.qwe.micro.ServiceDiscoveryApi;

/**
 * Represents for {@code HTTP request} dispatcher in {@code Gateway} that forward request from a {@code client} to a
 * backend {@code micro-service}.
 * <p>
 * It's responsible for keeping {@code micro REST API} definition to handle an incoming request context then forwarding
 * to {@code micro service owner}. After receiving {@code micro service owner} response, it will return result to
 * client.
 *
 * @see ProxyServiceApi
 */
public interface ProxyServiceDispatcher extends RequestDispatcher<RequestData, ResponseData>, HasLogger, GatewaySystem {

    static ProxyServiceDispatcher create(ServiceDiscoveryApi dispatcher, String gatewayPath,
                                         ReqAuthDefinition authDefinition,
                                         BiFunction<String, HttpMethod, RequestFilter> filterFun) {
        return new ProxyServiceDispatcherImpl().setup(dispatcher, gatewayPath, authDefinition, filterFun);
    }

    @Override
    default Logger logger() {
        return LoggerFactory.getLogger(ProxyServiceDispatcher.class);
    }

    /**
     * Setup dispatcher
     *
     * @param dispatcher     the proxy service dispatcher
     * @param gatewayPath    the gateway path
     * @param authDefinition the auth definition
     * @param filterFun      the proxy path
     * @return a reference to this for fluent API
     */
    ProxyServiceDispatcher setup(ServiceDiscoveryApi dispatcher, String gatewayPath, ReqAuthDefinition authDefinition,
                                 BiFunction<String, HttpMethod, RequestFilter> filterFun);

    @Override
    default ResponseInterceptor<ResponseData> responseInterceptor() {
        return new ResponseDataInterceptor();
    }

}
