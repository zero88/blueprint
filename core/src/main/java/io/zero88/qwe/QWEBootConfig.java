package io.zero88.qwe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.tracing.TracingOptions;
import io.zero88.qwe.cluster.ClusterType;
import io.zero88.qwe.utils.Networks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Boot system configuration
 *
 * @see VertxOptions
 */
@FieldNameConstants
public final class QWEBootConfig extends VertxOptions implements IConfig {

    public static final String NAME = "__system__";
    public static final int DEFAULT_EVENT_BUS_PORT = 2468;
    public static final String DEFAULT_CACHE_DIR = Paths.get(System.getProperty("java.io.tmpdir", "."), "qwe-cache")
                                                        .toString();
    public static final String DEFAULT_HA_GROUP = "__QWE__";

    @JsonIgnore
    private final VertxOptions delegate;
    @Getter
    private ClusterType clusterType = ClusterType.NONE;
    @Getter
    private boolean clusterLiteMember = false;
    @Getter
    @Accessors(fluent = true)
    private Path clusterConfigFile;

    public QWEBootConfig() { this.delegate = defVertxOpts(); }

    @JsonCreator
    public QWEBootConfig(Map<String, Object> map) {
        if (Objects.isNull(map)) {
            this.delegate = defVertxOpts();
            return;
        }
        this.clusterType = ClusterType.factory((String) map.get(Fields.clusterType));
        this.clusterLiteMember = Boolean.parseBoolean(map.getOrDefault(Fields.clusterLiteMember, "false").toString());
        this.clusterConfigFile = Optional.ofNullable(map.get(Fields.clusterConfigFile))
                                         .map(s -> Paths.get(s.toString()))
                                         .orElse(null);
        this.delegate = new VertxOptions(JsonObject.mapFrom(optimizeHA(optimizeEB(optimizeFS(map)))));
    }

    public String getClusterConfigFile() {
        return Optional.ofNullable(clusterConfigFile).map(Path::toAbsolutePath).map(Path::toString).orElse(null);
    }

    private VertxOptions defVertxOpts() {
        return new VertxOptions().setHAGroup(DEFAULT_HA_GROUP)
                                 .setEventBusOptions(defEventBusOpts())
                                 .setFileSystemOptions(defFileSysOpts());
    }

    @Override
    public String key() { return NAME; }

    @Override
    public Class<? extends IConfig> parent() { return QWEConfig.class; }

    @Override
    public JsonObject toJson() {
        return this.toJson(getMapper());
    }

    @Override
    public JsonObject toJson(ObjectMapper mapper) {
        return delegate.toJson()
                       .put(Fields.clusterType, clusterType.type())
                       .put(Fields.clusterLiteMember, clusterLiteMember)
                       .put(Fields.clusterConfigFile, clusterConfigFile);
    }

    @Override
    public int getEventLoopPoolSize() {
        return delegate.getEventLoopPoolSize();
    }

    @Override
    public QWEBootConfig setEventLoopPoolSize(int eventLoopPoolSize) {
        delegate.setEventLoopPoolSize(eventLoopPoolSize);
        return this;
    }

    @Override
    public int getWorkerPoolSize() {
        return delegate.getWorkerPoolSize();
    }

    @Override
    public QWEBootConfig setWorkerPoolSize(int workerPoolSize) {
        delegate.setWorkerPoolSize(workerPoolSize);
        return this;
    }

    @Override
    public long getBlockedThreadCheckInterval() {
        return delegate.getBlockedThreadCheckInterval();
    }

    @Override
    public QWEBootConfig setBlockedThreadCheckInterval(long blockedThreadCheckInterval) {
        delegate.setBlockedThreadCheckInterval(blockedThreadCheckInterval);
        return this;
    }

    @Override
    public long getMaxEventLoopExecuteTime() {
        return delegate.getMaxEventLoopExecuteTime();
    }

    @Override
    public QWEBootConfig setMaxEventLoopExecuteTime(long maxEventLoopExecuteTime) {
        delegate.setMaxEventLoopExecuteTime(maxEventLoopExecuteTime);
        return this;
    }

    @Override
    public long getMaxWorkerExecuteTime() {
        return delegate.getMaxWorkerExecuteTime();
    }

    @Override
    public QWEBootConfig setMaxWorkerExecuteTime(long maxWorkerExecuteTime) {
        delegate.setMaxWorkerExecuteTime(maxWorkerExecuteTime);
        return this;
    }

    @Override
    public ClusterManager getClusterManager() {
        return delegate.getClusterManager();
    }

    @Override
    public QWEBootConfig setClusterManager(ClusterManager clusterManager) {
        delegate.setClusterManager(clusterManager);
        return this;
    }

    @Override
    public int getInternalBlockingPoolSize() {
        return delegate.getInternalBlockingPoolSize();
    }

    @Override
    public QWEBootConfig setInternalBlockingPoolSize(int internalBlockingPoolSize) {
        delegate.setInternalBlockingPoolSize(internalBlockingPoolSize);
        return this;
    }

    @Override
    public boolean isHAEnabled() {
        return delegate.isHAEnabled();
    }

    @Override
    public QWEBootConfig setHAEnabled(boolean haEnabled) {
        delegate.setHAEnabled(haEnabled);
        return this;
    }

    @Override
    public int getQuorumSize() {
        return delegate.getQuorumSize();
    }

    @Override
    public QWEBootConfig setQuorumSize(int quorumSize) {
        delegate.setQuorumSize(quorumSize);
        return this;
    }

    @Override
    public String getHAGroup() {
        return delegate.getHAGroup();
    }

    @Override
    public QWEBootConfig setHAGroup(String haGroup) {
        delegate.setHAGroup(haGroup);
        return this;
    }

    @Override
    public MetricsOptions getMetricsOptions() {
        return delegate.getMetricsOptions();
    }

    @Override
    public FileSystemOptions getFileSystemOptions() {
        return delegate.getFileSystemOptions();
    }

    @Override
    public QWEBootConfig setMetricsOptions(MetricsOptions metrics) {
        delegate.setMetricsOptions(metrics);
        return this;
    }

    @Override
    public QWEBootConfig setFileSystemOptions(FileSystemOptions fileSystemOptions) {
        delegate.setFileSystemOptions(fileSystemOptions);
        return this;
    }

    @Override
    public long getWarningExceptionTime() {
        return delegate.getWarningExceptionTime();
    }

    @Override
    public QWEBootConfig setWarningExceptionTime(long warningExceptionTime) {
        delegate.setWarningExceptionTime(warningExceptionTime);
        return this;
    }

    @Override
    public EventBusOptions getEventBusOptions() {
        return delegate.getEventBusOptions();
    }

    @Override
    public QWEBootConfig setEventBusOptions(EventBusOptions options) {
        delegate.setEventBusOptions(options);
        return this;
    }

    @Override
    public AddressResolverOptions getAddressResolverOptions() {
        return delegate.getAddressResolverOptions();
    }

    @Override
    public QWEBootConfig setAddressResolverOptions(AddressResolverOptions addressResolverOptions) {
        delegate.setAddressResolverOptions(addressResolverOptions);
        return this;
    }

    @Override
    public boolean getPreferNativeTransport() {
        return delegate.getPreferNativeTransport();
    }

    @Override
    public QWEBootConfig setPreferNativeTransport(boolean preferNativeTransport) {
        delegate.setPreferNativeTransport(preferNativeTransport);
        return this;
    }

    @Override
    public TimeUnit getMaxEventLoopExecuteTimeUnit() {
        return delegate.getMaxEventLoopExecuteTimeUnit();
    }

    @Override
    public QWEBootConfig setMaxEventLoopExecuteTimeUnit(TimeUnit maxEventLoopExecuteTimeUnit) {
        delegate.setMaxEventLoopExecuteTimeUnit(maxEventLoopExecuteTimeUnit);
        return this;
    }

    @Override
    public TimeUnit getMaxWorkerExecuteTimeUnit() {
        return delegate.getMaxWorkerExecuteTimeUnit();
    }

    @Override
    public QWEBootConfig setMaxWorkerExecuteTimeUnit(TimeUnit maxWorkerExecuteTimeUnit) {
        delegate.setMaxWorkerExecuteTimeUnit(maxWorkerExecuteTimeUnit);
        return this;
    }

    @Override
    public TimeUnit getWarningExceptionTimeUnit() {
        return delegate.getWarningExceptionTimeUnit();
    }

    @Override
    public QWEBootConfig setWarningExceptionTimeUnit(TimeUnit warningExceptionTimeUnit) {
        delegate.setWarningExceptionTimeUnit(warningExceptionTimeUnit);
        return this;
    }

    @Override
    public TimeUnit getBlockedThreadCheckIntervalUnit() {
        return delegate.getBlockedThreadCheckIntervalUnit();
    }

    @Override
    public QWEBootConfig setBlockedThreadCheckIntervalUnit(TimeUnit blockedThreadCheckIntervalUnit) {
        delegate.setBlockedThreadCheckIntervalUnit(blockedThreadCheckIntervalUnit);
        return this;
    }

    @Override
    public TracingOptions getTracingOptions() {
        return delegate.getTracingOptions();
    }

    @Override
    public QWEBootConfig setTracingOptions(TracingOptions tracingOptions) {
        delegate.setTracingOptions(tracingOptions);
        return this;
    }

    private Map<String, Object> optimizeEB(Map<String, Object> map) {
        final Map<String, Object> opts = getOpts(map, "eventBusOptions");
        opts.compute("port", (s, o) -> o instanceof Number
                                       ? Networks.validPort(((Number) o).intValue(), DEFAULT_EVENT_BUS_PORT)
                                       : DEFAULT_EVENT_BUS_PORT);
        opts.compute("clusterPublicPort", (s, prop) -> prop instanceof Number
                                                    ? Networks.validPort(((Number) prop).intValue(),
                                                                         (Integer) opts.get("clusterPublicPort"))
                                                    : opts.get("port"));
        return map;
    }

    private Map<String, Object> optimizeFS(Map<String, Object> map) {
        getOpts(map, "fileSystemOptions").compute("fileCacheDir",
                                                  (k, p) -> overrideProp(FileSystemOptions.DEFAULT_FILE_CACHING_DIR, p,
                                                                         DEFAULT_CACHE_DIR));
        return map;
    }

    private Map<String, Object> optimizeHA(Map<String, Object> map) {
        map.compute("haGroup", (k, p) -> overrideProp(VertxOptions.DEFAULT_HA_GROUP, p, DEFAULT_HA_GROUP));
        return map;
    }

    private Object overrideProp(Object compare, Object prop, Object fallback) {
        return compare.equals(prop) || prop == null ? fallback : prop;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOpts(Map<String, Object> map, String optKey) {
        return (Map<String, Object>) map.compute(optKey, (s, o) -> {
            if (o instanceof JsonObject) {
                return ((JsonObject) o).getMap();
            }
            if (o instanceof Map) {
                return o;
            }
            return new HashMap<>();
        });
    }

    private EventBusOptions defEventBusOpts() {
        return new EventBusOptions().setPort(DEFAULT_EVENT_BUS_PORT).setClusterPublicPort(DEFAULT_EVENT_BUS_PORT);
    }

    private FileSystemOptions defFileSysOpts() {
        return new FileSystemOptions().setFileCacheDir(DEFAULT_CACHE_DIR);
    }

}
