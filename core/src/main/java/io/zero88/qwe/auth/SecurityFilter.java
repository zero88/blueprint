package io.zero88.qwe.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Future;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.exceptions.SecurityException.AuthenticationException;
import io.zero88.qwe.exceptions.SecurityException.InsufficientPermissionError;

import lombok.NonNull;

/**
 * The security filter
 *
 * @since 1.0.0
 */
public interface SecurityFilter {

    /**
     * Check the principal's authentication and authorization.
     * <p>
     * The {@code Future} output can be failed with one of the reasons:
     * <ul>
     *     <li>{@link AuthenticationException} if not authenticated</li>
     *     <li>{@link InsufficientPermissionError} if not authorized</li>
     * </ul>
     *
     * @param sharedData    the shared data
     * @param userInfo      the user info
     * @param reqDefinition the auth request definition
     * @return void future if permitted, otherwise failed future
     * @see ReqAuthDefinition
     * @see UserInfo
     * @see SharedDataLocalProxy
     */
    @NotNull Future<Void> check(@NonNull SharedDataLocalProxy sharedData, @Nullable UserInfo userInfo,
                                @NonNull ReqAuthDefinition reqDefinition);

}
