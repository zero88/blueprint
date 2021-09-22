package io.zero88.qwe.eventbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method responds to one or many particular event action.
 *
 * @see EBContext
 * @see EBParam
 * @see EBBody
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EBContract {

    /**
     * Associates the name of an {@code Event Action} with an annotation
     *
     * @return the possible event actions that a method can process
     * @see EventAction
     */
    String[] action();

}
