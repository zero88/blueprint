package io.zero88.qwe.eventbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.zero88.qwe.dto.msg.RequestData;

/**
 * Binds the value(s) of an {@code Event Message} standard data body({@link EventMessage#getData()}) to a resource
 * method parameter
 *
 * @implNote If no using standard message structure, then this annotation will be treated as {@link EBParam}
 * @see EventMessage#getData()
 * @see RequestData#body()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface EBBody {

    /**
     * Defines the name of the message data body whose value will be used to initialize the value of the annotated
     * method argument
     */
    String value();

}
