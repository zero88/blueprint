package io.zero88.qwe.eventbus.refl;

import java.lang.annotation.Annotation;
import java.util.Map;

import io.zero88.qwe.eventbus.EBContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class MethodParam {

    private final String paramName;
    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final Class<?> paramClass;
    /**
     * Check whether method param is EBContext or not
     *
     * @see EBContext
     */
    private final boolean isContext;

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T lookupAnnotation(Class<T> annotationClass) {
        return (T) annotations.get(annotationClass);
    }

}
