package io.zero88.qwe.auth;

import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.ImplementationError;

/**
 * The security interceptor that relies on {@link AuthN} and {@link AuthZ} annotation
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface SecurityInterceptor {

    /**
     * Default {@code security interceptor} with {@link SecurityFilter} is loaded by {@code service loader} {@code
     * META-INF/services}
     *
     * @see SecurityFilterLoader
     */
    SecurityInterceptor DEFAULT = create(SecurityFilterLoader.getInstance().getSecurityFilter());

    /**
     * Create the security interceptor
     *
     * @param securityFilter the security filter
     * @return the security interceptor
     */
    static SecurityInterceptor create(SecurityFilter securityFilter) {
        return () -> securityFilter;
    }

    /**
     * The authentication/authorization annotation processor
     *
     * @return the auth annotation processor
     */
    default @NotNull AuthAnnotationProcessor authAnnotationProcessor() {
        return AuthAnnotationProcessor.DEFAULT;
    }

    /**
     * The security filter
     *
     * @return the security filter
     * @see SecurityFilter
     */
    SecurityFilter securityFilter();

    /**
     * Validate authentication/authorization based on the {@link AuthN} or {@link AuthZ} annotation of given method
     *
     * @param sharedData the shared data
     * @param message    the incoming event message
     * @param method     the method
     * @return success future if permitted, otherwise
     */
    default @NotNull Future<Void> validate(SharedDataLocalProxy sharedData, EventMessage message, Method method) {
        final ReqAuthDefinition definition = authAnnotationProcessor().lookup(method);
        if (!definition.isLoginRequired()) {
            return Future.succeededFuture();
        }
        final SecurityFilter securityFilter = securityFilter();
        if (securityFilter == null) {
            return Future.failedFuture(new ImplementationError(ErrorCode.SECURITY_ERROR, "Missing security filter"));
        }
        return securityFilter.check(sharedData, message.getUserInfo(), definition);
    }

}
