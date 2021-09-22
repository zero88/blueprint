package io.zero88.qwe.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertx.core.shareddata.Shareable;
import io.zero88.qwe.dto.EnumType;
import io.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class ClusterType extends AbstractEnumType implements Shareable {

    public static final ClusterType HAZELCAST = new ClusterType("HAZELCAST",
                                                                Arrays.asList("hazelcast.jar", "vertx-hazelcast.jar"),
                                                                Arrays.asList("com.hazelcast.core.HazelcastInstance",
                                                                              "io.vertx.spi.cluster.hazelcast" +
                                                                              ".HazelcastClusterManager"));
    public static final ClusterType ZOOKEEPER = new ClusterType("ZOOKEEPER",
                                                                Arrays.asList("zookeeper.jar", "vertx-zookeeper.jar"),
                                                                Arrays.asList(
                                                                    "org.apache.curator.framework.CuratorFramework",
                                                                    "io.vertx.spi.cluster.zookeeper" +
                                                                    ".ZookeeperClusterManager"));
    public static final ClusterType INFINISPAN = new ClusterType("INFINISPAN");
    public static final ClusterType IGNITE = new ClusterType("IGNITE");
    public static final ClusterType NONE = new ClusterType("NONE");

    private final List<String> classes;
    private final List<String> jars;

    protected ClusterType(String type) {
        this(type, null, null);
    }

    protected ClusterType(String type, List<String> jars, List<String> classes) {
        super(type);
        this.classes = Optional.ofNullable(classes).orElseGet(ArrayList::new);
        this.jars = Optional.ofNullable(jars).orElseGet(ArrayList::new);
    }

    public static ClusterType def() { return NONE; }

    @JsonCreator
    public static ClusterType factory(String name) { return EnumType.factory(name, ClusterType.class, def()); }

}
