package io.zero88.qwe.crypto;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Defines the requested cryptographic by {@code plugin} or {@code extension}
 *
 * @since 1.0.0
 */
public interface CryptoRequest {

    /**
     * @return a key pair alias
     */
    @Nullable
    default String requireKeyPair() {
        return null;
    }

    /**
     * @return a list of trust cert aliases. If {@code null} means request all available trust certs.
     */
    @Nullable
    default List<String> requireTrustCert() {
        return Collections.emptyList();
    }

}
