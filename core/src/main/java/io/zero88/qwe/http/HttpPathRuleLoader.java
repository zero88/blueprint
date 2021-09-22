package io.zero88.qwe.http;

import java.util.function.Supplier;

import io.vertx.core.ServiceHelper;

public final class HttpPathRuleLoader implements Supplier<HttpPathRule> {

    private static HttpPathRuleLoader instance;

    public static HttpPathRuleLoader getInstance() {
        if (instance == null) {
            synchronized (HttpPathRuleLoader.class) {
                instance = new HttpPathRuleLoader();
            }
        }
        return instance;
    }

    private final HttpPathRule rule;

    private HttpPathRuleLoader() {
        rule = ServiceHelper.loadFactory(HttpPathRule.class);
    }

    @Override
    public HttpPathRule get() {
        return rule;
    }

}
