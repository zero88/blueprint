package io.zero88.qwe.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.utils.FileUtils;
import io.github.zero88.utils.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.TrustOptions;
import io.zero88.qwe.Wrapper;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.exceptions.CryptoException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;

/**
 * Represents for a wrapper of {@link KeyStore} for configuring key store option then lazy loading on demand.
 */
public interface LazyKeyStore extends Wrapper<KeyStore>, JsonData {

    @JsonCreator
    static LazyKeyStore create(@JsonProperty("type") String type, @JsonProperty("provider") String provider,
                               @JsonProperty("password") String password, @JsonProperty("path") String ksFile) {
        return new LazyKeyStoreImpl(type, provider, password, ksFile);
    }

    static KeyStore load(Vertx vertx, LazyKeyStore ks) {
        return load(ks, () -> vertx.fileSystem().readFileBlocking(ks.getPath()).getBytes());
    }

    static KeyStore load(LazyKeyStore ks, Path keyStoreFile) {
        return load(ks, () -> {
            try {
                return Files.readAllBytes(keyStoreFile);
            } catch (IOException t) {
                throw new CryptoException("Unable load key store", t);
            }
        });
    }

    static KeyStore load(LazyKeyStore ks, Supplier<byte[]> supplier) {
        try {
            String type = ks.getType().type();
            KeyStore keyStore = Strings.isBlank(ks.getProvider())
                                ? KeyStore.getInstance(type)
                                : KeyStore.getInstance(type, ks.getProvider());
            synchronized (LazyKeyStore.class) {
                InputStream is = new ByteArrayInputStream(supplier.get());
                try {
                    keyStore.load(is, ks.getPassword().toCharArray());
                } finally {
                    FileUtils.silentClose(is);
                }
            }
            return keyStore;
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException | NoSuchProviderException e) {
            throw new CryptoException("Unable load key store", e);
        }
    }

    /**
     * KeyStore type
     *
     * @return keyStore type
     * @see KeyStoreType
     * @see KeyStore#getType()
     */
    @NonNull KeyStoreType getType();

    /**
     * KeyStore provider
     *
     * @return keyStore provider
     * @see KeyStore#getProvider()
     */
    String getProvider();

    /**
     * KeyStore password
     *
     * @return key store password
     */
    String getPassword();

    /**
     * KeyStore file path
     *
     * @return key store file path
     */
    String getPath();

    /**
     * Init {@code KeyStore}
     *
     * @param vertx the vertx instance
     * @return a reference to this for fluent API
     */
    LazyKeyStore init(Vertx vertx);

    /**
     * Get all key aliases in {@code KeyStore}
     *
     * @return the key aliases, or empty collection if no alias in key store
     * @see KeyAlias
     */
    Collection<KeyAlias> aliases();

    /**
     * Get the {@code PrivateKey} associated with the given alias, using the given password to recover it. Then convert
     * to {@link KeyCertOptions} for compatible with {@code Vertx API}
     *
     * @param alias    the alias name
     * @param password the password for recovering the key
     * @return the key cert options, or null if the given alias does not exist or does not identify a key-related entry
     * @apiNote Default output type is {@link PemKeyCertOptions}
     * @see KeyStore#getKey(String, char[])
     */
    @Nullable KeyCertOptions getKeyCert(String alias, char[] password);

    /**
     * Similar to {@link #getKeyCert(String, char[])} but password in String
     */
    default @Nullable KeyCertOptions getKeyCert(String alias, String password) {
        return getKeyCert(alias, Objects.requireNonNull(password, "Password is not null").toCharArray());
    }

    /**
     * Similar to {@link #getKeyCert(String, char[])} but password in {@link Buffer}
     */
    default @Nullable KeyCertOptions getKeyCert(String alias, Buffer password) {
        return getKeyCert(alias, Objects.isNull(password) ? null : password.toString(StandardCharsets.UTF_8));
    }

    /**
     * Get the certificate associated with the given alias. Then convert to {@link TrustOptions} for compatible with
     * {@code Vertx API}
     *
     * @param alias the alias name
     * @return the trust options, or null if the given alias does not exist or does not contain a certificate.
     * @apiNote Default output type is {@link PemTrustOptions}
     * @see KeyStore#getCertificateChain(String)
     */
    @Nullable TrustOptions getCertificateChain(String alias);

    /**
     * Get the certificate associated with the given alias. Then convert to {@link TrustOptions} for compatible with
     * {@code Vertx API}
     *
     * @param alias the alias name
     * @return the trust options, or null if the given alias does not exist or does not contain a certificate.
     * @apiNote Default output type is {@link PemTrustOptions}
     * @see KeyStore#getCertificate(String)
     */
    @Nullable TrustOptions getCertificate(String alias);

    /**
     * Get one or more the certificate associated with the given alias. Then convert to {@link TrustOptions} for
     * compatible with {@code Vertx API}
     *
     * @param aliases the given aliases
     * @return the trust options, or null if the given alias does not exist or does not contain a certificate.
     * @apiNote Default output type is {@link PemTrustOptions}
     * @see KeyStore#getCertificate(String)
     */
    @Nullable TrustOptions getCertificates(@NonNull List<String> aliases);

    /**
     * Get all certificates
     *
     * @return the trust options, or null if no certificates
     * @apiNote Default output type is {@link PemTrustOptions}
     */
    @Nullable TrustOptions getCertificates();

    /**
     * Get a clone KeyStore object
     *
     * @return a cloned key store
     */
    @Override
    KeyStore unwrap();

    /**
     * Serializes all certificates in Key Store to {@code PEM} format then bundle all in {@code json} format.
     *
     * @return the json object
     * @see #toJson(Map)
     */
    @Override
    JsonObject toJson();

    /**
     * Serializes all entries in Key store to {@code PEM} format then bundle all in {@code json} format.
     *
     * @param aliasProtection map of key alias and its password
     * @return the json object
     */
    JsonObject toJson(Map<String, String> aliasProtection);

    @Override
    default JsonObject toJson(@NonNull ObjectMapper mapper) {
        return toJson();
    }

    @Override
    default JsonObject toJson(@NonNull ObjectMapper mapper, @NonNull Set<String> ignoreFields) {
        return toJson();
    }

}
