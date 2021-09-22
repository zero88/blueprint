package io.zero88.qwe.auth;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.utils.Strings;

import lombok.NonNull;

public class AuthAnnotationProcessorImpl implements AuthAnnotationProcessor {

    @Override
    public @NotNull ReqAuthDefinition lookup(@NonNull Method method) {
        final Class<?> cls = method.getDeclaringClass();
        List<ReqAuthZDefinition> list = Optional.ofNullable(getAuthZ(method))
                                                .orElseGet(() -> Optional.ofNullable(getAuthZ(cls))
                                                                         .orElseGet(() -> getAuthZ(cls.getPackage())));
        if (list != null) {
            return ReqAuthDefinition.authz(list);
        }
        if (hasAuthN(method) || hasAuthN(cls) || hasAuthN(cls.getPackage())) {
            return ReqAuthDefinition.requireLogin();
        }
        return ReqAuthDefinition.noAuth();
    }

    protected boolean hasAuthN(AnnotatedElement element) {
        return element.getAnnotation(AuthN.class) != null;
    }

    protected List<ReqAuthZDefinition> getAuthZ(AnnotatedElement element) {
        AuthZ[] authZs = element.getAnnotationsByType(AuthZ.class);
        if (authZs == null || authZs.length == 0) {
            return null;
        }
        return Arrays.stream(authZs)
                     .map(authZ -> ReqAuthZDefinition.builder()
                                                     .allowRoles(collect(authZ.role()))
                                                     .allowPerms(collect(authZ.perm()))
                                                     .allowGroups(collect(authZ.group()))
                                                     .customAccessRule(authZ.access())
                                                     .build())
                     .collect(Collectors.toList());
    }

    private List<String> collect(String[] items) {
        return Arrays.stream(items).filter(Strings::isNotBlank).collect(Collectors.toList());
    }

}
