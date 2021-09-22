package io.zero88.qwe.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.zero88.qwe.dto.JsonData;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for a request authentication/authorization definition that is model of {@link AuthN}, {@link AuthZ} and
 * {@link AuthZContainer}
 *
 * @see ReqAuthZDefinition
 * @since 1.0.0
 */
@Jacksonized
@Builder(access = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReqAuthDefinition implements JsonData {

    private final boolean loginRequired;
    private final List<ReqAuthZDefinition> authz;

    public static ReqAuthDefinition requireLogin() {
        return new ReqAuthDefinition(true, new ArrayList<>());
    }

    public static ReqAuthDefinition noAuth() {
        return new ReqAuthDefinition(false, new ArrayList<>());
    }

    public static ReqAuthDefinition authz(@NonNull ReqAuthZDefinition definition) {
        return authz(Collections.singletonList(definition));
    }

    public static ReqAuthDefinition authz(@NonNull List<ReqAuthZDefinition> definitions) {
        return new ReqAuthDefinition(true, definitions.stream()
                                                      .filter(Objects::nonNull)
                                                      .filter(ReqAuthZDefinition::isNotEmpty)
                                                      .collect(Collectors.toList()));
    }

    /**
     * A flag defines should check whether the principal is authorized or not
     *
     * @return the authorized flag
     */
    @JsonIgnore
    public boolean isAuthzRequired() {
        return authz != null && !authz.isEmpty();
    }

    /**
     * A flag defines should check whether the principal is authenticated or not
     *
     * @return the authenticated flag
     */
    public boolean isLoginRequired() {
        return loginRequired;
    }

    /**
     * Get a request authorization definitions
     *
     * @return the authorization definitions. It might be {@code null} if {@link #isLoginRequired()} or {@link
     *     #isAuthzRequired()} is {@code False}
     */
    public List<ReqAuthZDefinition> getAuthz() {
        return authz;
    }

}
