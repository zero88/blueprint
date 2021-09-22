package io.zero88.qwe.http.server.handler;

import java.util.Optional;

import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.auth.UserInfo;

/**
 * The AuthN/AuthZ interceptor SPI.
 * <p>
 * To implement security feature, you need to implement your own and put your class in classpath resource {@code
 * META-INF/services/io.zero88.qwe.http.server.handler.AuthInterceptor}
 */
public interface AuthInterceptor extends RequestInterceptor<UserInfo> {

    static AuthInterceptor create(ReqAuthDefinition authDefinition) {
        return Optional.ofNullable(AuthInterceptorLoader.getInstance().get())
                       .orElseGet(AuthInterceptorDummy::new)
                       .setup(authDefinition);
    }

    AuthInterceptor setup(ReqAuthDefinition authDefinition);

}
