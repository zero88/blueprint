package io.zero88.qwe.crypto;

import java.security.KeyStore;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.KeyStoreProvider;
import io.zero88.qwe.crypto.KeyAlias.KeyEntryType;

@ExtendWith(VertxExtension.class)
class LazyKeyStoreTest {

    KeyStoreProvider jwtKeyStore;
    KeyStoreProvider tlsKeyStore;
    KeyStoreProvider trustStore;

    @BeforeEach
    void setup() {
        jwtKeyStore = KeyStoreProvider.jwt();
        tlsKeyStore = KeyStoreProvider.tls();
        trustStore = KeyStoreProvider.trust();
    }

    @Test
    void test_load_ES256(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authOptions = createJWTOpt(vertx, "ES256").setJWTOptions(
            new JWTOptions().setAlgorithm("ES256").setIssuer("xyz").setExpiresInMinutes(3));
        JWTAuth jwt = JWTAuth.create(vertx, authOptions);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_ES384(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authConfig = createJWTOpt(vertx, "ES384").setJWTOptions(new JWTOptions().setAlgorithm("ES384"));
        JWTAuth jwt = JWTAuth.create(vertx, authConfig);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_ES512(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authConfig = createJWTOpt(vertx, "ES512").setJWTOptions(new JWTOptions().setAlgorithm("ES512"));
        JWTAuth jwt = JWTAuth.create(vertx, authConfig);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_RS256(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authConfig = createJWTOpt(vertx, "RS256").setJWTOptions(
            new JWTOptions().setAlgorithm("RS256").setIssuer("xyz").setExpiresInMinutes(3));
        JWTAuth jwt = JWTAuth.create(vertx, authConfig);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_RS512(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authConfig = createJWTOpt(vertx, "RS512").setJWTOptions(
            new JWTOptions().setAlgorithm("RS512").setIssuer("xyz").setExpiresInMinutes(3));
        JWTAuth jwt = JWTAuth.create(vertx, authConfig);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_simple(Vertx vertx, VertxTestContext context) {
        JWTAuthOptions authConfig = new JWTAuthOptions().addPubSecKey(
            new PubSecKeyOptions().setAlgorithm("HS256").setBuffer("keyboard cat"));
        JWTAuth jwt = JWTAuth.create(vertx, authConfig);
        Assertions.assertNotNull(jwt.generateToken(new JsonObject().put("sub", "zero88")));
        context.completeNow();
    }

    @Test
    void test_load_tls_by_alias(Vertx vertx, VertxTestContext context) throws Exception {
        KeyCertOptions options = tlsKeyStore.init(vertx).getKeyCert("http");
        Assertions.assertNotNull(options);
        KeyStore keyStore = ((PemKeyCertOptions) options).loadKeyStore(vertx);
        Assertions.assertEquals(1, keyStore.size());
        context.completeNow();
    }

    @Test
    void test_load_secret_key_should_failed(Vertx vertx, VertxTestContext context) {
        IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class,
                                                             () -> tlsKeyStore.init(vertx).getKeyCert("sec"));
        Assertions.assertEquals("The alias [sec] is not key-pair format", t.getMessage());
        context.completeNow();
    }

    @Test
    void test_load_truststore_by_alias(Vertx vertx, VertxTestContext context) throws Exception {
        TrustOptions options = trustStore.init(vertx).getCertificate("emqx");
        Assertions.assertNotNull(options);
        TrustManagerFactory factory = options.getTrustManagerFactory(vertx);
        Assertions.assertNotNull(factory);
        Assertions.assertEquals(1, factory.getTrustManagers().length);
        Assertions.assertEquals(1, ((X509TrustManager) factory.getTrustManagers()[0]).getAcceptedIssuers().length);
        context.completeNow();
    }

    @Test
    void test_load_all_truststore(Vertx vertx, VertxTestContext context) throws Exception {
        TrustOptions options = trustStore.init(vertx).getCertificates();
        Assertions.assertNotNull(options);
        TrustManagerFactory factory = options.getTrustManagerFactory(vertx);
        Assertions.assertNotNull(factory);
        Assertions.assertEquals(1, factory.getTrustManagers().length);
        Assertions.assertEquals(4, ((X509TrustManager) factory.getTrustManagers()[0]).getAcceptedIssuers().length);
        context.completeNow();
    }

    @Test
    void test_clone(Vertx vertx, VertxTestContext context) throws Exception {
        final KeyStore ks1 = trustStore.init(vertx).unwrap();
        Assertions.assertNotNull(ks1);
        final KeyStore ks2 = trustStore.init(vertx).unwrap();
        Assertions.assertNotNull(ks2);
        Assertions.assertSame(ks1, ks2);
        Assertions.assertEquals(4, ks1.size());
        context.completeNow();
    }

    @Test
    void test_get_entries(Vertx vertx, VertxTestContext context) {
        final Collection<KeyAlias> entries = tlsKeyStore.init(vertx).aliases();
        Assertions.assertNotNull(entries);
        Assertions.assertEquals(4, entries.size());
        Assertions.assertEquals(1, entries.stream().filter(ke -> ke.getType() == KeyEntryType.SECRET_KEY).count());
        Assertions.assertEquals(3, entries.stream().filter(ke -> ke.getType() == KeyEntryType.KEY_PAIR).count());
        context.completeNow();
    }

    @Test
    void test_serialize_json(Vertx vertx, VertxTestContext context) {
        JsonObject json = trustStore.init(vertx).toJson();
        Assertions.assertNotNull(json);
        Assertions.assertFalse(json.isEmpty());
        Assertions.assertEquals(4, json.size());
        System.out.println(json);
        context.completeNow();
    }

    @Test
    void test_serialize_json_with_key(Vertx vertx, VertxTestContext context) {
        JsonObject cert = tlsKeyStore.init(vertx).toJson();
        Assertions.assertNotNull(cert);
        Assertions.assertEquals(3, cert.size());
        Assertions.assertEquals(0, cert.stream()
                                       .filter(e -> ((JsonObject) e.getValue()).containsKey("private_key"))
                                       .count());
        Map<String, String> protection = new HashMap<>();
        protection.put("eventbus", "ChaosMeteor");
        protection.put("sec", "EMP");
        JsonObject containKey = tlsKeyStore.init(vertx).toJson(protection);
        Assertions.assertNotNull(containKey);
        Assertions.assertEquals(4, containKey.size());
        JsonObject eventbus = containKey.getJsonObject("eventbus");
        Assertions.assertNotNull(eventbus);
        Assertions.assertTrue(eventbus.containsKey("private_key"));
        Assertions.assertTrue(eventbus.containsKey("cert"));
        JsonObject sec = containKey.getJsonObject("sec");
        Assertions.assertNotNull(sec);
        Assertions.assertFalse(sec.containsKey("cert"));
        Assertions.assertTrue(sec.containsKey("private_key"));
        Assertions.assertFalse(sec.getString("private_key").contains("-----BEGIN"));
        JsonObject http = containKey.getJsonObject("http");
        Assertions.assertNotNull(http);
        Assertions.assertTrue(http.containsKey("cert"));
        Assertions.assertFalse(http.containsKey("private_key"));
        System.out.println(containKey);
        context.completeNow();
    }

    JWTAuthOptions createJWTOpt(Vertx vertx, String algorithm) {
        PemKeyCertOptions keyCert = (PemKeyCertOptions) jwtKeyStore.init(vertx).getKeyCert(algorithm);
        Assertions.assertNotNull(keyCert);
        PubSecKeyOptions cert = new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(keyCert.getCertValue());
        PubSecKeyOptions pKey = new PubSecKeyOptions().setAlgorithm(algorithm).setBuffer(keyCert.getKeyValue());
        return new JWTAuthOptions().addPubSecKey(cert).addPubSecKey(pKey);
    }

}
