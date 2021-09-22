package io.zero88.qwe.eventbus.refl;

import java.lang.reflect.Method;

/**
 * Represents for EventListener method that corresponding to {@code EventAction} will be executed
 */
public interface MethodMeta {

    /**
     * @return the declaring class
     */
    String declaringClass();

    /**
     * @return the method parameters
     */
    MethodParam[] params();

    /**
     * @return a reflection method
     * @see Method
     */
    Method method();

}
