package io.zero88.qwe.crypto;

import org.jetbrains.annotations.Nullable;

import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines a cryptographic context that be using in {@code plugin} or {@code extension}
 *
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public final class CryptoContext {

    @Nullable
    private final KeyCertOptions keyCertOptions;
    @Nullable
    private final TrustOptions trustOptions;

    public static CryptoContext empty() {
        return new CryptoContext(null, null);
    }

}
