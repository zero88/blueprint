package io.zero88.qwe;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.zero88.qwe.PluginConfig.PluginDirConfig;
import io.zero88.qwe.crypto.CryptoContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Plugin context after deployment
 *
 * @see Plugin
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface PluginContext extends HasAppName, HasPluginName, HasSharedData, ExtensionHolder {

    /**
     * Create a pre-context of plugin before deploying it
     *
     * @param appName       an associate app name
     * @param pluginName    an associate plugin name
     * @param dataDir       a plugin data dir
     * @param sharedData    a shared data proxy in {@code Application}
     * @param extensions    the dependency extensions
     * @param cryptoContext crypto request
     * @return a plugin pre-context
     */
    static PluginContext create(String appName, String pluginName, Path dataDir, SharedDataLocalProxy sharedData,
                                Collection<? extends Extension> extensions, CryptoContext cryptoContext) {
        return new DefaultPluginContext(appName, pluginName, dataDir, sharedData, extensions, cryptoContext);
    }

    /**
     * A runtime plugin data dir
     *
     * @return an actual plugin data dir. It can be null if a {@code plugin} does not define plugin dir configuration
     * @see QWEAppConfig#dataDir()
     * @see PluginDirConfig
     */
    @Nullable Path dataDir();

    @NotNull CryptoContext cryptoContext();

    /**
     * A Plugin deployment id
     *
     * @return deployment id if plugin is already installed successfully, otherwise it is {@code null}
     */
    @Nullable String deployId();

    /**
     * Inject a plugin deployment id in post deployment
     *
     * @param deployId deployment id
     * @return a reference to this for fluent API
     */
    PluginContext deployId(String deployId);

    @Getter
    @Accessors(fluent = true)
    class DefaultPluginContext implements PluginContext {

        private final String appName;
        private final String pluginName;
        private final Path dataDir;
        private final SharedDataLocalProxy sharedData;
        private final Map<Class<? extends Extension>, Extension> extensionMap;
        private final CryptoContext cryptoContext;
        @Setter
        private String deployId;

        protected DefaultPluginContext(@NonNull PluginContext ctx) {
            this(ctx.appName(), ctx.pluginName(), ctx.dataDir(), ctx.sharedData(), ctx.extensions(),
                 ctx.cryptoContext());
            this.deployId = ctx.deployId();
        }

        DefaultPluginContext(String appName, String pluginName, Path dataDir, SharedDataLocalProxy sharedData,
                             Collection<? extends Extension> extensions, CryptoContext cryptoContext) {
            this.appName = appName;
            this.pluginName = pluginName;
            this.dataDir = dataDir;
            this.sharedData = sharedData;
            this.extensionMap = extensions.stream().collect(Collectors.toMap(Extension::getClass, Function.identity()));
            this.cryptoContext = cryptoContext;
        }

        @Override
        public final Collection<Extension> extensions() {
            return extensionMap.values();
        }

        @Override
        public final <E extends Extension> E getExtension(Class<E> extCls) {
            return (E) extensionMap.get(extCls);
        }

    }

}
