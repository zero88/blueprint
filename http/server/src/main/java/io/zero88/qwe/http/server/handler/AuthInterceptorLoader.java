package io.zero88.qwe.http.server.handler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.ServiceHelper;

public final class AuthInterceptorLoader implements Supplier<AuthInterceptor> {

    private static AuthInterceptorLoader instance;

    public static AuthInterceptorLoader getInstance() {
        if (instance == null) {
            synchronized (AuthInterceptorLoader.class) {
                instance = new AuthInterceptorLoader();
            }
        }
        return instance;
    }

    private final Class<? extends AuthInterceptor> cls;

    private AuthInterceptorLoader() {
        cls = Optional.ofNullable(ServiceHelper.loadFactory(AuthInterceptor.class))
                      .map(AuthInterceptor::getClass)
                      .orElse(null);
    }

    @Override
    public AuthInterceptor get() {
        return Objects.isNull(cls) ? null : ReflectionClass.createObject(cls);
    }

}
