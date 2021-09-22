package io.zero88.qwe.crypto;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Simple information about key entry alias and its type
 *
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class KeyAlias {

    private final String alias;
    private final KeyEntryType type;


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum KeyEntryType {
        SECRET_KEY(SecretKeyEntry.class, algo -> null),
        KEY_PAIR(PrivateKeyEntry.class, algo -> "RSA".equals(algo) ? "RSA PRIVATE KEY" : "PRIVATE KEY"),
        CERT(TrustedCertificateEntry.class, algo -> "CERTIFICATE");

        @Getter
        private final Class<? extends KeyStore.Entry> entryClass;
        private final Function<String, String> pemTypeFun;

        public String getPEMType(String algo) {
            return pemTypeFun.apply(algo);
        }
    }

}
