package io.zero88.qwe.launcher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.annotations.DefaultValue;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.impl.VertxBuilder;
import io.vertx.core.impl.launcher.commands.BareCommand;
import io.vertx.core.impl.launcher.commands.ExecUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.launcher.ExecutionContext;
import io.zero88.qwe.IConfig;
import io.zero88.qwe.QWEBootConfig;
import io.zero88.qwe.cluster.ClusterFactoryServiceLoader;
import io.zero88.qwe.cluster.ClusterManagerFactory;
import io.zero88.qwe.cluster.ClusterNodeListener;
import io.zero88.qwe.cluster.ClusterNodeListenerServiceLoader;
import io.zero88.qwe.cluster.ClusterType;

import lombok.Getter;

@Summary("Creates a bare QWE instance")
@Description("This command launches a vert.x instance but do not deploy any verticles. It will " +
             "receive a verticle if another node of the cluster dies.")
@Name("boot")
public class BootCommand extends BareCommand {

    public static final String KEY_STORE_CONFIG = "keyStoreConf";
    public static final String KEY_STORE_CLI_OPT = "keyStore";
    public static final String TRUST_STORE_CLI_OPT = "trustStore";
    protected QWEBootConfig bootConf;
    @Getter
    protected String clusterType;
    protected boolean clusterLiteMember;
    protected String clusterConfigFile;
    protected String keyStoreConfig;
    protected String trustStoreConfig;

    /**
     * Sets the HA group name.
     *
     * @param group the name of the group, default to {@code __QWE__}.
     */
    @Option(longName = "haGroup", argName = "haGroup")
    @Description(
        "Used in conjunction with -ha this specifies the HA group this node will join. There can be multiple " +
        "HA groups in a cluster. Nodes will only failover to other nodes in the same group. Defaults to '" +
        QWEBootConfig.DEFAULT_HA_GROUP + "'.")
    @DefaultValue(QWEBootConfig.DEFAULT_HA_GROUP)
    public void setHAGroup(String group) {
        this.haGroup = group;
    }

    /**
     * Enable the cluster mode with a specific cluster
     *
     * @param clusterType cluster type, default is false.
     */
    @Option(longName = "cluster-type", argName = "clusterType", choices = {
        "HAZELCAST", "ZOOKEEPER", "INFINISPAN", "IGNITE"
    })
    @Description("Enable cluster mode with a specific type")
    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    /**
     * Enable the cluster mode with a specific cluster
     *
     * @param clusterConfigFile cluster type, default is false.
     */
    @Option(longName = "cluster-conf", argName = "clusterConfigFile")
    @Description("Specifies the cluster configuration file corresponding to cluster type")
    public void setClusterConfigFile(String clusterConfigFile) {
        this.clusterConfigFile = clusterConfigFile;
    }

    /**
     * Enable the cluster mode with a specific cluster
     *
     * @param clusterLiteMember cluster type, default is false.
     */
    @Option(longName = "cluster-lite-member", argName = "clusterLiteMember", acceptValue = false, flag = true)
    @Description("Enable cluster lite member. Only for HAZELCAST")
    public void setClusterLiteMember(boolean clusterLiteMember) {
        this.clusterLiteMember = clusterLiteMember;
    }

    @Option(longName = "keyStore", argName = "keyStore")
    @Description("Specifies the KeyStore config. It should reference either a JSON file which represents the " +
                 "config OR be a JSON string.")
    public void setKeyStore(String keyStoreConfig) {
        this.keyStoreConfig = keyStoreConfig;
    }

    @Option(longName = "trustStore", argName = "trustStore")
    @Description("Specifies the Trust Store config. It should reference either a JSON file which represents the " +
                 "config OR be a JSON string.")
    public void setTrustStore(String trustStoreConfig) {
        this.trustStoreConfig = trustStoreConfig;
    }

    @Override
    public boolean isClustered() {
        return Objects.nonNull(clusterType);
    }

    @Override
    public void setUp(ExecutionContext ec) throws CLIException {
        super.setUp(ec);
        CommandLine commandLine = executionContext.commandLine();
        if (!isClustered() &&
            Stream.of("cluster-host", "cluster-port", "cluster-public-host", "cluster-public-port", "cluster-conf",
                      "cluster-lite-member")
                  .anyMatch(o -> commandLine.isOptionAssigned(executionContext.cli().getOption(o)))) {
            throw new CLIException("The -cluster-xxx options require -cluster to be enabled");
        }

        // If quorum and / or ha-group, ha need to have been explicitly set
        io.vertx.core.cli.Option haGroupOption = executionContext.cli().getOption("hagroup");
        io.vertx.core.cli.Option quorumOption = executionContext.cli().getOption("quorum");
        if (!getHA() && (commandLine.isOptionAssigned(haGroupOption) || commandLine.isOptionAssigned(quorumOption))) {
            throw new CLIException("The option -hagroup and -quorum requires -ha to be enabled");
        }
    }

    /**
     * Starts the vert.x instance.
     *
     * @return the created instance of vert.x
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    protected Vertx startVertx() {
        if (bootConf == null) {
            bootConf = buildVertxOptions(getJsonFromFileOrString(vertxOptions, "options"));
        }
        VertxBuilder builder = new VertxBuilder(bootConf);

        beforeStartingVertx(bootConf);
        builder.init();

        Vertx instance;
        if (isClustered()) {
            log.info("Starting clustering...");

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<AsyncResult<Vertx>> result = new AtomicReference<>();
            create(builder, ar -> {
                result.set(ar);
                latch.countDown();
            });
            try {
                if (!latch.await(2, TimeUnit.MINUTES)) {
                    log.error("Timed out in starting clustered Vert.x");
                    return null;
                }
            } catch (InterruptedException e) {
                log.error("Thread interrupted in startup");
                Thread.currentThread().interrupt();
                return null;
            }
            if (result.get().failed()) {
                log.error("Failed to form cluster", result.get().cause());
                return null;
            }
            instance = result.get().result();
        } else {
            instance = create(builder);
        }
        addShutdownHook(instance, log, finalAction);
        afterStartingVertx(instance);
        return instance;
    }

    protected QWEBootConfig buildVertxOptions(JsonObject options) {
        QWEBootConfig conf = IConfig.from(Optional.ofNullable(options).orElseGet(JsonObject::new), QWEBootConfig.class);
        conf.setEventBusOptions(getEventBusOptions(conf.getEventBusOptions().toJson()));
        //        configureFromSystemProperties(config, VERTX_OPTIONS_PROP_PREFIX);
        //        configureFromSystemProperties(config.getMetricsOptions(), METRICS_OPTIONS_PROP_PREFIX);
        conf.setClusterType(Optional.ofNullable(clusterType).orElse(conf.getClusterType().type()));
        if (!isClustered()) {
            log.error("Required cluster mode by passing \"-cluster-type\" in CLI option or defining property " +
                      "in a configuration file");
            ExecUtils.exitBecauseOfVertxInitializationIssue();
        }
        if (isClustered()) {
            if (clusterLiteMember) {
                conf.setClusterLiteMember(true);
            }
            if (Objects.nonNull(clusterConfigFile)) {
                conf.setClusterConfigFile(clusterConfigFile);
            }
            if (!Objects.equals(clusterHost, EventBusOptions.DEFAULT_CLUSTER_HOST)) {
                conf.getEventBusOptions().setHost(clusterHost);
            }
            if (clusterPort > EventBusOptions.DEFAULT_CLUSTER_PORT) {
                conf.getEventBusOptions().setPort(clusterPort);
            }
            if (!Objects.equals(clusterPublicHost, EventBusOptions.DEFAULT_CLUSTER_PUBLIC_HOST)) {
                conf.getEventBusOptions().setClusterPublicHost(clusterPublicHost);
            }
            if (clusterPublicPort > EventBusOptions.DEFAULT_CLUSTER_PUBLIC_PORT) {
                conf.getEventBusOptions().setClusterPublicPort(clusterPublicPort);
            } else {
                conf.getEventBusOptions().setClusterPublicPort(conf.getEventBusOptions().getPort());
            }

            if (getHA()) {
                conf.setHAEnabled(true);
                if (haGroup != null) {
                    conf.setHAGroup(haGroup);
                }
                if (quorum != -1) {
                    conf.setQuorumSize(quorum);
                }
            }
            if (Objects.isNull(conf.getEventBusOptions().getClusterNodeMetadata())) {
                conf.getEventBusOptions().setClusterNodeMetadata(VersionCommand.getVersion().toJson());
            }
            conf.setClusterManager(createClusterManager(conf));
        }
        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                JsonObject cliOpt = new JsonObject().put(QWEBootConfig.Fields.clusterType, clusterType)
                                                    .put(QWEBootConfig.Fields.clusterLiteMember, clusterLiteMember)
                                                    .put(QWEBootConfig.Fields.clusterConfigFile, clusterConfigFile)
                                                    .put("clusterHost", clusterHost)
                                                    .put("clusterPort", clusterPort)
                                                    .put("clusterPublicHost", clusterPublicHost)
                                                    .put("clusterPublicPort", clusterPublicPort)
                                                    .put("ha", getHA())
                                                    .put("haGroup", haGroup)
                                                    .put("quorum", quorum)
                                                    .put(KEY_STORE_CLI_OPT, keyStoreConfig)
                                                    .put(TRUST_STORE_CLI_OPT, trustStoreConfig);
                log.trace("CFG OPTION: " + options);
                log.trace("CLI OPTION: " + cliOpt);
            }
            log.debug("QWE BOOT CONFIG: " + conf.toJson());
        }
        return conf;
    }

    @Override
    protected void afterStartingVertx(Vertx instance) {
        final ClusterNodeListener listener = new ClusterNodeListenerServiceLoader().listener();
        if (listener != null && instance.isClustered()) {
            bootConf.getClusterManager().nodeListener(listener.setup(instance, bootConf));
        }
        instance.getOrCreateContext().put(KEY_STORE_CONFIG, bootConf.getKeyStoreConfig());
        super.afterStartingVertx(instance);
    }

    private ClusterManager createClusterManager(QWEBootConfig config) {
        if (config.getClusterType() == ClusterType.NONE) {
            return null;
        }
        final ClusterManagerFactory factory = new ClusterFactoryServiceLoader().lookup(config.getClusterType());
        if (factory == null) {
            return null;
        }
        return factory.validate().create(config);
    }

}
