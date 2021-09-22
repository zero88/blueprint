package io.zero88.qwe.auth;

import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;

import lombok.NonNull;

/**
 * Represents for the authentication/authorization annotation processor
 *
 * @see AuthN
 * @see AuthZ
 * @since 1.0.0
 */
public interface AuthAnnotationProcessor {

    AuthAnnotationProcessor DEFAULT = create();

    static AuthAnnotationProcessor create() {
        return new AuthAnnotationProcessorImpl();
    }

    @NotNull ReqAuthDefinition lookup(@NonNull Method method);

}
