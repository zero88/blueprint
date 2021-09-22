package io.zero88.qwe.eventbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the value(s) of an {@code Event Message} data({@link EventMessage#getData()}) to a resource method parameter
 *
 * @apiNote Able to omit if {@code Java method} has only one parameter that can be deserialized from json
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface EBParam {

    /**
     * Defines the name of the message data whose value will be used to initialize the value of the annotated method
     * argument
     */
    String value();

}
