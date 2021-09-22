package io.zero88.qwe.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides the {@code authority} attributes to determine whether permit or deny access to a particular
 * resource.
 * <p>
 * The {@code authorization} system will aggregate all declared {@code authority} attributes in single {@link AuthZ} to
 * make final decision.
 * <pre>{@code
 *  @AuthZ(role = "user")
 *  public void query() {
 *    // Mean the Principal that is granted to role "user"
 *    // is able to execute the "query" action
 *  }
 *
 *  @AuthZ(role = "super_user", perm = {"create", "update"})
 *  public void post() {
 *    // Mean the Principal that is granted to role "super_user"
 *    // with permission "create" or "update"
 *    // is able to execute the "post" action
 *  }
 * }</pre>
 * <p>
 * To obtain the {@code authorization} system approval on one of the declared {@code authority} conditions, you can
 * declare more than one {@link AuthZ}.
 * <pre>{@code
 *  @AuthZ(role = {"admin", "super_user"})
 *  @AuthZ(group = "supporter")
 *  public void answer() {
 *    // Mean the Principal that is granted to role "admin" or "super_user"
 *    // or belongs to group "supporter"
 *    // is able to execute the "answer" action
 *  }
 * }</pre>
 *
 * @see AuthZContainer
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.METHOD, ElementType.TYPE})
@Repeatable(AuthZContainer.class)
@Documented
public @interface AuthZ {

    /**
     * Declares one or more the permitted {@code roles}
     *
     * @return list of roles
     */
    String[] role() default {};

    /**
     * Declares one or more the permitted {@code permissions}
     *
     * @return list of permissions
     */
    String[] perm() default {};

    /**
     * Declares one or more the permitted {@code groups}
     *
     * @return list of groups
     */
    String[] group() default {};

    /**
     * Define custom access rule
     *
     * @return custom access rule
     */
    String access() default "";

}
