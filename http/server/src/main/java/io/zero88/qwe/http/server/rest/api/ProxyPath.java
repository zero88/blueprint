package io.zero88.qwe.http.server.rest.api;

import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.auth.ReqAuthDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ProxyPath {

    @EqualsAndHashCode.Include
    private final String path;
    @EqualsAndHashCode.Include
    private final HttpMethod method;
    private final ReqAuthDefinition authDefinition;

}
