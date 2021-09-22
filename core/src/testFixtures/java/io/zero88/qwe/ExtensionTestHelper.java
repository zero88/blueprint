package io.zero88.qwe;

import java.nio.file.Path;
import java.util.Optional;

import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.crypto.CryptoContext;
import io.zero88.qwe.dto.JsonData;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface ExtensionTestHelper extends AppContextTest {

    @Override
    default String appName() {
        return "ExtensionTest";
    }

    default <C extends ExtensionConfig, EE extends ExtensionEntrypoint<C>, E extends Extension<C, EE>> E initExtension(
        Vertx vertx, Class<E> cls, C extConfig) {
        return initExtension(vertx, cls, extConfig, CryptoContext.empty());
    }

    default <C extends ExtensionConfig, EE extends ExtensionEntrypoint<C>, E extends Extension<C, EE>> E initExtension(
        Vertx vertx, Class<E> cls, C extConfig, CryptoContext cryptoContext) {
        return (E) createExt(createSharedData(vertx), appName(), testDir(), cls, extConfig, cryptoContext);
    }

    static Extension createExt(SharedDataLocalProxy sharedData, String appName, Path appDir,
                               Class<? extends Extension> cls, ExtensionConfig extConfig) {
        return createExt(sharedData, appName, appDir, cls, extConfig, CryptoContext.empty());
    }

    static Extension createExt(SharedDataLocalProxy sharedData, String appName, Path appDir,
                               Class<? extends Extension> cls, ExtensionConfig extConfig, CryptoContext cryptoContext) {
        return ReflectionClass.createObject(cls)
                              .setup(sharedData, appName, appDir,
                                     Optional.ofNullable(extConfig).map(JsonData::toJson).orElseGet(JsonObject::new),
                                     cryptoContext);
    }

}
