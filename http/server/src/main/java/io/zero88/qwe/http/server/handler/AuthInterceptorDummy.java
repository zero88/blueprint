package io.zero88.qwe.http.server.handler;

import java.util.Optional;
import java.util.stream.Stream;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.auth.UserInfo;
import io.zero88.qwe.exceptions.SecurityException.AuthenticationException;

/**
 * The dummy AuthN/AuthZ interceptor verifies required login if any and throw exception if required authorization
 */
public final class AuthInterceptorDummy implements AuthInterceptor {

    private ReqAuthDefinition authDefinition;

    @Override
    public AuthInterceptor setup(ReqAuthDefinition authDefinition) {
        this.authDefinition = Optional.ofNullable(authDefinition).orElseGet(ReqAuthDefinition::noAuth);
        return this;
    }

    @Override
    public Future<UserInfo> filter(RoutingContext ctx) {
        if (ctx.user() == null) {
            if (authDefinition.isLoginRequired()) {
                return Future.failedFuture(new AuthenticationException("Required login or bad credential"));
            }
            return Future.succeededFuture(null);
        }
        if (authDefinition.isAuthzRequired()) {
            return Future.failedFuture(new SecurityException("Unknown authorization"));
        }
        return Future.succeededFuture(UserInfo.create(
            Stream.of("username", "access_token", "id", "identifier", "user_name", "user")
                  .map(s -> (String) ctx.user().get(s))
                  .findFirst()
                  .orElseThrow(() -> new IllegalArgumentException("Unknown user identifier")),
            new JsonObject().put("principal", ctx.user().principal()).put("attributes", ctx.user().attributes())));
    }

}
