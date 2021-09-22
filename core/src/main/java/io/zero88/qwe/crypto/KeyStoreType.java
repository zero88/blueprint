package io.zero88.qwe.crypto;

import io.vertx.core.net.PfxOptions;
import io.zero88.qwe.dto.EnumType;
import io.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class KeyStoreType extends AbstractEnumType {

    /**
     * Java KeyStore. Oracle's KeyStore format.
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/security/KeyStore.html">Javadoc KeyStore</a>
     * @see <a href="https://en.wikipedia.org/wiki/Java_KeyStore">JKS</a>
     */
    public static final KeyStoreType JKS = new KeyStoreType("JKS");
    /**
     * Java Cryptography Extension KeyStore. More secure version of JKS.
     *
     */
    public static final KeyStoreType JCEKS = new KeyStoreType("JCEKS");
    /**
     * Public-Key Cryptography Standards #12 KeyStore. RSA's KeyStore format.
     *
     * @see <a href="https://en.wikipedia.org/wiki/PKCS_12">PKCS #12</a>
     * @see PfxOptions
     */
    public static final KeyStoreType PKCS12 = new KeyStoreType("PKCS12");
    /**
     * Bouncy Castle KeyStore. Bouncy Castle's version of JKS.
     */
    public static final KeyStoreType BKS = new KeyStoreType("BKS");

    /**
     * Bouncy Castle UBER KeyStore. More secure version of BKS.
     */
    public static final KeyStoreType UBER = new KeyStoreType("UBER");

    /**
     * Bouncy Castle FIPS KeyStore (uses FIPS compliant algorithms PBDKF2, SHA-512 and AES CCM).
     */
    public static final KeyStoreType BCFKS = new KeyStoreType("BCFKS");

    private KeyStoreType(String type) {
        super(type);
    }

    @JsonCreator
    public static KeyStoreType factory(String name) {
        return EnumType.factory(name, KeyStoreType.class, true);
    }

    @JsonValue
    public String format() {return type();}

}
