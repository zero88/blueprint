package io.zero88.qwe;

public interface ExtensionEntrypoint<C extends ExtensionConfig> {

    /**
     * Get extension config
     *
     * @return extension config
     */
    C extConfig();

}
