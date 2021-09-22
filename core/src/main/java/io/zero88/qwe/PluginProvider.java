package io.zero88.qwe;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * A plugin provider
 *
 * @param <T> Plugin type
 */
public interface PluginProvider<T extends Plugin> extends Supplier<T> {

    @NotNull Class<T> pluginClass();

    /**
     * Provide a set of dependency extensions that used by plugin
     *
     * @return set of extensions classes
     */
    default Collection<Class<? extends Extension>> extensions() {
        return Collections.emptySet();
    }

}
