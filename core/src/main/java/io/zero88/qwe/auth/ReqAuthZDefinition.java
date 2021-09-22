package io.zero88.qwe.auth;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.utils.Strings;
import io.zero88.qwe.dto.JsonData;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for the request authorization definition that is model of {@link AuthZ}
 *
 * @since 1.0.0
 */
@Getter
@Builder
@Jacksonized
public final class ReqAuthZDefinition implements JsonData {

    @Singular
    private final @Nullable List<String> allowRoles;
    @Singular
    private final @Nullable List<String> allowPerms;
    @Singular
    private final @Nullable List<String> allowGroups;
    private final @Nullable String customAccessRule;

    @JsonIgnore
    public boolean isEmpty() {
        return (allowRoles == null || allowRoles.isEmpty()) && (allowPerms == null || allowPerms.isEmpty()) &&
               (allowGroups == null || allowGroups.isEmpty()) && Strings.isBlank(customAccessRule);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
