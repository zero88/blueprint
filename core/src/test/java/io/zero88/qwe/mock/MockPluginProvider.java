package io.zero88.qwe.mock;

import io.zero88.qwe.PluginContext;
import io.zero88.qwe.PluginProvider;
import io.zero88.qwe.PluginVerticle;
import io.zero88.qwe.mock.MockPluginProvider.MockPlugin;

import lombok.Getter;
import lombok.NonNull;

public class MockPluginProvider implements PluginProvider<MockPlugin> {

    private final boolean error;

    public MockPluginProvider()              {this(false);}

    public MockPluginProvider(boolean error) {this.error = error;}

    @Override
    public Class<MockPlugin> pluginClass() {return MockPlugin.class;}

    @Override
    public MockPlugin get() {
        return new MockPlugin(error);
    }

    public static final class MockPlugin extends PluginVerticle<MockPluginConfig, PluginContext> {

        @Getter
        private final boolean error;

        public MockPlugin() {
            this(false);
        }

        public MockPlugin(boolean error) {
            this.error = error;
        }

        @Override
        public String pluginName() {
            return "mock";
        }

        @Override
        public @NonNull Class<MockPluginConfig> configClass() {
            return MockPluginConfig.class;
        }

        @Override
        public @NonNull String configFile() {
            return "mock-plugin.json";
        }

        @Override
        public void onStart() {
            if (error) {
                throw new RuntimeException("Error when starting Plugin[" + pluginName() + "]");
            }
        }

        @Override
        public String configKey() {
            return "mock";
        }

    }

}
