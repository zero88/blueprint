package io.zero88.qwe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.zero88.repl.Reflections;
import io.vertx.core.Vertx;
import io.vertx.core.net.KeyCertOptions;
import io.zero88.qwe.crypto.KeyStoreType;
import io.zero88.qwe.crypto.LazyKeyStore;
import io.zero88.qwe.crypto.LazyKeyStoreImpl;

public interface KeyStoreProvider extends LazyKeyStore {

    static KeyStoreProvider create(KeyStoreType type, String classpathFile, String password) {
        String file = Objects.requireNonNull(Reflections.contextClassLoader().getResource(classpathFile)).getFile();
        return new KeyStoreProviderImpl(type, password, file);
    }

    KeyStoreProvider putPasswordProtection(String alias, String password);

    KeyCertOptions getKeyCert(String alias);

    @Override
    KeyStoreProvider init(Vertx vertx);

    static KeyStoreProvider jwt() {
        return KeyStoreProvider.create(KeyStoreType.PKCS12, "qwe-jwt.pfx", "invoker")
                               .putPasswordProtection("ES256", "icewall")
                               .putPasswordProtection("ES384", "alacrity")
                               .putPasswordProtection("ES512", "cataclysm")
                               .putPasswordProtection("RS256", "coldsnap")
                               .putPasswordProtection("RS384", "ghostwalk")
                               .putPasswordProtection("RS512", "tornado")
                               .putPasswordProtection("tester.playio.cloud", "forgespirit");
    }

    static KeyStoreProvider tls() {
        return KeyStoreProvider.create(KeyStoreType.PKCS12, "qwe-key.pfx", "invoker")
                               .putPasswordProtection("http", "SunStrike")
                               .putPasswordProtection("eventbus", "ChaosMeteor")
                               .putPasswordProtection("tls.root", "DeafeningBlast")
                               .putPasswordProtection("sec", "EMP");
    }

    static KeyStoreProvider trust() {
        return KeyStoreProvider.create(KeyStoreType.PKCS12, "qwe-trust.pfx", "invoker");
    }

    class KeyStoreProviderImpl extends LazyKeyStoreImpl implements KeyStoreProvider {

        private final Map<String, String> aliasProtection = new HashMap<>();

        public KeyStoreProviderImpl(KeyStoreType type, String password, String path) {
            super(type, null, password, path);
        }

        public KeyStoreProvider putPasswordProtection(String alias, String password) {
            aliasProtection.put(alias, password);
            return this;
        }

        @Override
        public KeyStoreProvider init(Vertx vertx) {
            super.init(vertx);
            return this;
        }

        @Override
        public KeyCertOptions getKeyCert(String alias) {
            return getKeyCert(alias, aliasProtection.get(alias));
        }

    }

}
