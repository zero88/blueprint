package io.zero88.qwe.auth;

import io.github.zero88.utils.ServiceHelper;

import lombok.Getter;

public final class SecurityFilterLoader {

    static SecurityFilterLoader instance;

    @Getter
    private final SecurityFilter securityFilter;

    private SecurityFilterLoader() {
        this.securityFilter = ServiceHelper.loadFactory(SecurityFilter.class);
    }

    public static SecurityFilterLoader getInstance() {
        if (instance == null) {
            synchronized (SecurityFilterLoader.class) {
                instance = new SecurityFilterLoader();
            }
        }
        return instance;
    }

}
