package io.zero88.qwe.crypto;

import java.util.List;
import java.util.Optional;

import io.github.zero88.utils.ServiceHelper;
import io.vertx.core.json.JsonObject;

public final class CryptoHolderProviderLoader {

    private final CryptoHolderProvider provider;

    public CryptoHolderProviderLoader() {
        this.provider = Optional.ofNullable(ServiceHelper.loadFactory(CryptoHolderProvider.class))
                                .orElseGet(CryptoHolderProvider::dummy);
    }

    public CryptoHolder setup(List<String> args, JsonObject ksConfig) {
        return provider.setup(args, ksConfig);
    }

}
