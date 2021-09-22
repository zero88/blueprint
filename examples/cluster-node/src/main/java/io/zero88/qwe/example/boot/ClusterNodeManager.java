package io.zero88.qwe.example.boot;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.zero88.qwe.QWEBootConfig;
import io.zero88.qwe.cluster.ClusterNodeListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ClusterNodeManager implements ClusterNodeListener {

    private Vertx vertx;
    private QWEBootConfig config;

    @Override
    public ClusterNodeManager setup(Vertx vertx, QWEBootConfig config) {
        this.vertx = vertx;
        this.config = config;
        return this;
    }

    @Override
    public void nodeAdded(String nodeID) {
        log.info("Added node: {}", nodeID);
        final ClusterManager clusterManager = config.getClusterManager();
        log.info("{}", clusterManager.getNodes());
        Promise<NodeInfo> p = vertx instanceof VertxInternal ? ((VertxInternal) vertx).promise() : Promise.promise();
        clusterManager.getNodeInfo(nodeID, p);
        p.future().onComplete(ar -> {
            if (ar.succeeded()) {
                final NodeInfo node = ar.result();
                log.info(node.host() + ":" + node.port() + "----" + node.metadata());
            } else {
                log.error("Lookup failed", ar.cause());
            }
        });
    }

    @Override
    public void nodeLeft(String nodeID) {
        log.info("Remove node: {}", nodeID);
    }

}
