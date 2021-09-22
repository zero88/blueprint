package io.zero88.qwe.crypto;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;

/**
 * The {@code cryptography holder} for propagating from {@code application} to each {@code extension} and each {@code
 * plugin}
 * <p>
 * {@code Key-Pair option} or {@code Trust option} will be shared to set up and configure per {@code extension} or
 * {@code plugin}
 *
 * @since 1.0.0
 */
public interface CryptoHolder {

    /**
     * Get key cert option by alias
     *
     * @param alias the key cert alias
     * @return the key cert alias
     */
    @Nullable KeyCertOptions getKeyCertOptions(String alias);

    /**
     * Get trust certificate by given aliases.
     * <p>
     * The output's order will be same as the input's order regardless the cert that corresponding to the request alias
     * is whether available.
     *
     * @param aliases the trust certificate aliases
     * @return the trust certificates
     */
    @Nullable TrustOptions getTrustCertificates(List<String> aliases);

    /**
     * Get all trust certificates
     *
     * @return trust certificates
     */
    @Nullable TrustOptions getTrustCertificates();

    default @NotNull CryptoContext lookup(CryptoRequest request) {
        return new CryptoContext(getKeyCertOptions(request.requireKeyPair()),
                                 getTrustCertificates(request.requireTrustCert()));
    }

}
