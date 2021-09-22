package io.zero88.qwe.http;

import java.util.Objects;

import io.github.zero88.utils.Strings;
import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents for a relationship between {@code EventAction}, {@code HttpMethod} and {@code url capture path}
 *
 * @see EventAction
 * @see HttpMethod
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventMethodMapping implements JsonData {

    @Include
    @NonNull
    private final EventAction action;
    @Include
    @NonNull
    private final HttpMethod method;
    private final String capturePath;
    @Include
    @JsonIgnore
    private final String regexPath;
    /**
     * Identify using {@link RequestData} or not. Default is {@code True}
     * <p>
     * If {@code False}, only {@code path params} and {@code body} in {@code HTTP Request} will be included and omit
     * data in {@code HTTP Request query params} and {@code HTTP Request Header}
     */
    private final boolean useRequestData;
    private final ReqAuthDefinition auth;

    public EventMethodMapping(EventAction action, HttpMethod method, String capturePath) {
        this(action, method, capturePath, true, null);
    }

    public EventMethodMapping(EventAction action, HttpMethod method, String capturePath, boolean useRequestData) {
        this(action, method, capturePath, useRequestData, null);
    }

    @JsonCreator
    public EventMethodMapping(@JsonProperty("action") EventAction action, @JsonProperty("method") HttpMethod method,
                              @JsonProperty("capturePath") String capturePath,
                              @JsonProperty("useRequestData") Boolean useRequestData,
                              @JsonProperty("auth") ReqAuthDefinition auth) {
        this.action = Objects.requireNonNull(action, "Missing event action");
        this.method = Objects.requireNonNull(method, "Missing HTTP method");
        this.capturePath = Strings.requireNotBlank(capturePath, "Missing capture path");
        this.regexPath = HttpPathRuleLoader.getInstance().get().createRegex(capturePath);
        this.useRequestData = useRequestData == null || useRequestData;
        this.auth = Objects.isNull(auth) ? ReqAuthDefinition.noAuth() : auth;
    }

    @JsonProperty("method")
    public String method() {
        return getMethod().name();
    }

}
