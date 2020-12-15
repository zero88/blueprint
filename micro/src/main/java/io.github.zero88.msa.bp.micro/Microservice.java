package io.github.zero88.msa.bp.micro;

import io.github.zero88.msa.bp.component.UnitVerticle;
import io.vertx.core.Future;

public final class Microservice extends UnitVerticle<MicroConfig, MicroContext> {

    Microservice() {
        super(new MicroContext());
    }

    @Override
    public void start() {
        super.start();
        logger.info("Setup micro-service...");
        getContext().setup(vertx, config, getSharedKey());
    }

    @Override
    public void stop(Future<Void> future) { getContext().unregister(future); }

    @Override
    public Class<MicroConfig> configClass() { return MicroConfig.class; }

    @Override
    public String configFile() { return "micro.json"; }

}
