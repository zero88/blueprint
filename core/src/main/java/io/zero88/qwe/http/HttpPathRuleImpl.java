package io.zero88.qwe.http;

import java.util.Arrays;

import io.github.zero88.utils.Strings;
import io.vertx.core.http.HttpMethod;
import io.zero88.qwe.eventbus.EventAction;

public class HttpPathRuleImpl implements HttpPathRule {

    protected boolean isSingular(HttpMethod method) {
        return Arrays.asList(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH).contains(method);
    }

    protected boolean hasBody(HttpMethod method) {
        return Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.TRACE).contains(method);
    }

    public String createCapture(HttpMethod method, EventAction action, String servicePath, String capturePath) {
        boolean isSingular = isSingular(method) && action != EventAction.GET_LIST || action == EventAction.GET_ONE;
        return Strings.isBlank(servicePath) || isSingular ? capturePath : servicePath;
    }

}
