package io.zero88.qwe.crypto;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;

/**
 * Represents for provider that supply the cryptographic holder
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface CryptoHolderProvider {

    /**
     * Init a cryptographic holder
     *
     * @param args           cli arguments
     * @param keyStoreConfig key store config
     * @return the crypto holder
     * @see CryptoHolder
     */
    CryptoHolder setup(List<String> args, JsonObject keyStoreConfig);

    static CryptoHolderProvider dummy() {
        return (args, keyStoreConfig) -> new CryptoHolder() {
            @Override
            public @Nullable KeyCertOptions getKeyCertOptions(String alias) {
                return null;
            }

            @Override
            public @Nullable TrustOptions getTrustCertificates(List<String> aliases) {
                return null;
            }

            @Override
            public @Nullable TrustOptions getTrustCertificates() {
                return null;
            }
        };
    }

}
