package io.zero88.qwe.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.DefaultValue;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.ParsedAsList;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;
import io.vertx.core.impl.launcher.commands.ExecUtils;
import io.vertx.core.impl.launcher.commands.Watcher;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.QWEBootConfig;
import io.zero88.qwe.QWEConfig;

@Name(value = "run", priority = 100)
@Summary("Runs a QWE Application")
public class RunCommand extends BootCommand {

    protected DeploymentOptions deploymentOptions;

    protected boolean ha;
    protected int instances;
    protected String config;
    protected boolean worker;

    protected String mainVerticle;
    protected List<String> redeploy;

    protected String vertxApplicationBackgroundId;
    protected String onRedeployCommand;
    protected Watcher watcher;
    private long redeployScanPeriod;
    private long redeployGracePeriod;
    private long redeployTerminationPeriod;

    /**
     * Enable the high availability (HA) deployment.
     *
     * @param ha high-availability flag, default is false.
     */
    @Option(longName = "ha", acceptValue = false, flag = true)
    @Description(
        "If specified the verticle will be deployed as a high availability (HA) deployment. This means it can " +
        "fail over to any other nodes in the cluster started with the same HA group.")
    public void setHighAvailability(boolean ha) {
        this.ha = ha;
    }

    /**
     * Whether the verticle is deployed as a worker verticle.
     *
     * @param worker {@code true} to deploy the verticle as worker, {@code false} otherwise
     */
    @Option(longName = "worker", acceptValue = false, flag = true)
    @Description("If specified then the verticle is a worker verticle.")
    public void setWorker(boolean worker) {
        this.worker = worker;
    }

    /**
     * Sets the number of instance of the verticle to create.
     *
     * @param instances the number of instances
     */
    @Option(longName = "instances", argName = "instances")
    @DefaultValue("1")
    @Description("Specifies how many instances of the verticle will be deployed. Defaults to 1.")
    public void setInstances(int instances) {
        this.instances = instances;
    }

    /**
     * The main verticle configuration, it can be a json file or a json string.
     *
     * @param configuration the configuration
     */
    @Option(longName = "conf", argName = "config")
    @Description(
        "Specifies configuration that should be provided to the verticle. <config> should reference either a " +
        "text file containing a valid JSON object which represents the configuration OR be a JSON string.")
    public void setConfig(String configuration) {
        if (configuration != null) {
            // For inlined configuration remove first and end single and double quotes if any
            this.config = configuration.trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
        } else {
            this.config = null;
        }
    }

    /**
     * Sets the main verticle that is deployed.
     *
     * @param verticle the verticle
     */
    @Argument(index = 0, argName = "main-verticle")
    @Description("The main verticle to deploy, it can be a fully qualified class name or a file.")
    public void setMainVerticle(String verticle) {
        this.mainVerticle = verticle;
    }

    @Option(longName = "redeploy", argName = "includes")
    @Description("Enable automatic redeployment of the application. This option takes a set on includes as parameter " +
                 "indicating which files need to be watched. Patterns are separated by a comma.")
    @ParsedAsList
    public void setRedeploy(List<String> redeploy) {
        this.redeploy = redeploy;
    }

    @Option(longName = "on-redeploy", argName = "cmd")
    @Description("Optional shell command executed when a redeployment is triggered")
    public void setOnRedeployCommand(String command) {
        this.onRedeployCommand = command;
    }

    @Option(longName = "redeploy-scan-period", argName = "period")
    @Description("When redeploy is enabled, this option configures the file system scanning period to detect file " +
                 "changes. The time is given in milliseconds. 250 ms by default.")
    @DefaultValue("250")
    public void setRedeployScanPeriod(long period) {
        this.redeployScanPeriod = period;
    }

    @Option(longName = "redeploy-grace-period", argName = "period")
    @Description(
        "When redeploy is enabled, this option configures the grace period between 2 redeployments. The time " +
        "is given in milliseconds. 1000 ms by default.")
    @DefaultValue("1000")
    public void setRedeployGracePeriod(long period) {
        this.redeployGracePeriod = period;
    }

    @Option(longName = "redeploy-termination-period", argName = "period")
    @Description("When redeploy is enabled, this option configures the time waited to be sure that the previous " +
                 "version of the application has been stopped. It is useful on Windows, where the 'terminate' command" +
                 " may take time to be " + "executed.The time is given in milliseconds. 0 ms by default.")
    @DefaultValue("0")
    public void setRedeployStopWaitingTime(long period) {
        this.redeployTerminationPeriod = period;
    }

    /**
     * Starts vert.x and deploy the verticle.
     */
    @Override
    public void run() {
        if (redeploy == null || redeploy.isEmpty()) {
            buildVertxOptionsAndDeploymentOptions();
            super.run(this::afterStoppingVertx);
            if (vertx == null) {
                // Already logged.
                ExecUtils.exitBecauseOfVertxInitializationIssue();
            }

            if (vertx instanceof VertxInternal) {
                ((VertxInternal) vertx).addCloseHook(completionHandler -> {
                    try {
                        beforeStoppingVertx(vertx);
                        completionHandler.handle(Future.succeededFuture());
                    } catch (Exception e) {
                        completionHandler.handle(Future.failedFuture(e));
                    }
                });
            }

            beforeDeployingVerticle(deploymentOptions);
            deploy();
        } else {
            // redeploy is set, start the redeployment infrastructure (watcher).
            initializeRedeployment();
        }
    }

    protected void buildVertxOptionsAndDeploymentOptions() {
        JsonObject conf = Optional.ofNullable(getJsonFromFileOrString(config, "conf")).orElseGet(JsonObject::new);
        afterConfigParsed(conf);
        boolean isQWE = QWEConfig.isInstance(conf);
        JsonObject aConf = isQWE ? conf.getJsonObject(QWEConfig.APP_CONF_KEY, new JsonObject()) : conf;
        JsonObject dConf = isQWE ? conf.getJsonObject(QWEConfig.DEPLOY_CONF_KEY, new JsonObject()) : new JsonObject();
        JsonObject bConf = isQWE ? conf.getJsonObject(QWEConfig.BOOT_CONF_KEY, new JsonObject()) : new JsonObject();
        JsonObject cliOpt = Optional.ofNullable(getJsonFromFileOrString(vertxOptions, "options"))
                                    .orElseGet(JsonObject::new);
        this.bootConf = buildVertxOptions(bConf.mergeIn(cliOpt, true));
        this.deploymentOptions = new DeploymentOptions(dConf).setConfig(aConf).setInstances(instances);
        if (executionContext.commandLine().isFlagEnabled("ha")) {
            deploymentOptions.setHa(ha);
        }
        if (executionContext.commandLine().isFlagEnabled("worker")) {
            deploymentOptions.setHa(worker);
        }
    }

    /**
     * Initializes the redeployment cycle. In "redeploy mode", the application is launched as background, and is
     * restarted after every change. A {@link Watcher} instance is responsible for monitoring files and triggering the
     * redeployment.
     */
    protected synchronized void initializeRedeployment() {
        if (watcher != null) {
            throw new IllegalStateException("Redeployment already started ? The watcher already exists");
        }
        // Compute the application id. We append "-redeploy" to ease the identification in the process list.
        vertxApplicationBackgroundId = UUID.randomUUID() + "-redeploy";
        watcher = new Watcher(getCwd(), redeploy, this::startAsBackgroundApplication,  // On deploy
                              this::stopBackgroundApplication, // On undeploy
                              onRedeployCommand, // In between command
                              redeployGracePeriod, // The redeploy grace period
                              redeployScanPeriod); // The redeploy scan period

        // Close the watcher when the JVM is terminating.
        // Notice that the vert.x finalizer is not registered when we run in redeploy mode.
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownRedeployment));
        // Start the watching process, it triggers the initial deployment.
        watcher.watch();
    }

    /**
     * Stop the redeployment if started.
     */
    protected synchronized void shutdownRedeployment() {
        if (watcher != null) {
            watcher.close();
            watcher = null;
        }
    }

    /**
     * On-Undeploy action invoked while redeploying. It just stops the application launched in background.
     *
     * @param onCompletion an optional on-completion handler. If set it must be invoked at the end of this method.
     */
    protected synchronized void stopBackgroundApplication(Handler<Void> onCompletion) {
        executionContext.execute("stop", vertxApplicationBackgroundId, "--redeploy");
        if (redeployTerminationPeriod > 0) {
            try {
                Thread.sleep(redeployTerminationPeriod);
            } catch (InterruptedException e) {
                // Ignore the exception.
                Thread.currentThread().interrupt();
            }
        }

        if (onCompletion != null) {
            onCompletion.handle(null);
        }
    }

    /**
     * On-Deploy action invoked while redeploying. It just starts the application in background, copying all input
     * parameters. In addition, the vertx application id is set.
     *
     * @param onCompletion an optional on-completion handler. If set it must be invoked at the end of this method.
     */
    protected void startAsBackgroundApplication(Handler<Void> onCompletion) {
        executionContext.execute("start", buildCommandArgs().toArray(new String[0]));
        if (onCompletion != null) {
            onCompletion.handle(null);
        }
    }

    protected List<String> buildCommandArgs() {
        // We need to copy all options and arguments.
        List<String> args = new ArrayList<>();
        // Prepend the command.
        args.add("run");
        args.add("--vertx-id=" + vertxApplicationBackgroundId);
        args.addAll(executionContext.commandLine().allArguments());
        // No need to add the main-verticle as it's part of the allArguments list.
        if (isClustered()) {
            args.add("--cluster-type=" + clusterType);
            args.add("--cluster-conf=" + clusterConfigFile);
            if (clusterHost != null) {
                args.add("--cluster-host=" + clusterHost);
            }
            if (clusterPort != 0) {
                args.add("--cluster-port=" + clusterPort);
            }
            if (clusterPublicHost != null) {
                args.add("--cluster-public-host=" + clusterPublicHost);
            }
            if (clusterPublicPort != -1) {
                args.add("--cluster-public-port=" + clusterPublicPort);
            }
            if (clusterLiteMember) {
                args.add("--cluster-lite-member");
            }
        }
        if (ha) {
            args.add("--ha");
        }
        if (haGroup != null && !haGroup.equals(QWEBootConfig.DEFAULT_HA_GROUP)) {
            args.add("--hagroup=" + haGroup);
        }
        if (quorum != -1) {
            args.add("--quorum=" + quorum);
        }
        if (classpath != null && !classpath.isEmpty()) {
            args.add("--classpath=" + String.join(File.pathSeparator, classpath));
        }
        if (vertxOptions != null) {
            // Pass the configuration in 2 steps to quote correctly the options if it's an inlined json string
            args.add("--options");
            args.add(vertxOptions);
        }
        if (config != null) {
            // Pass the configuration in 2 steps to quote correctly the configuration if it's an inlined json string
            args.add("--conf");
            args.add(config);
        }
        if (instances != 1) {
            args.add("--instances=" + instances);
        }
        if (worker) {
            args.add("--worker");
        }
        if (systemProperties != null) {
            args.addAll(systemProperties.stream().map(s -> "-D" + s).collect(Collectors.toList()));
        }
        if (keyStoreConfig != null) {
            args.add("--" + KEY_STORE_CLI_OPT);
            args.add(keyStoreConfig);
        }
        if (trustStoreConfig != null) {
            args.add("--" + TRUST_STORE_CLI_OPT);
            args.add(trustStoreConfig);
        }
        // Enable stream redirection
        args.add("--redirect-output");
        return args;
    }

    protected void deploy() {
        deploy(mainVerticle, vertx, deploymentOptions, res -> {
            if (res.failed()) {
                handleDeployFailed(res.cause());
            }
        });
    }

    private void handleDeployFailed(Throwable cause) {
        if (executionContext.main() instanceof VertxLifecycleHooks) {
            ((VertxLifecycleHooks) executionContext.main()).handleDeployFailed(vertx, mainVerticle, deploymentOptions,
                                                                               cause);
        } else {
            ExecUtils.exitBecauseOfVertxDeploymentIssue();
        }
    }

    protected void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        final Object main = executionContext.main();
        if (main instanceof VertxLifecycleHooks) {
            ((VertxLifecycleHooks) main).beforeDeployingVerticle(deploymentOptions);
        }
    }

    protected void afterConfigParsed(JsonObject config) {
        final Object main = executionContext.main();
        if (main instanceof VertxLifecycleHooks) {
            ((VertxLifecycleHooks) main).afterConfigParsed(config);
        }
    }

    protected void beforeStoppingVertx(Vertx vertx) {
        final Object main = executionContext.main();
        if (main instanceof VertxLifecycleHooks) {
            ((VertxLifecycleHooks) main).beforeStoppingVertx(vertx);
        }
    }

    protected void afterStoppingVertx() {
        final Object main = executionContext.main();
        if (main instanceof VertxLifecycleHooks) {
            ((VertxLifecycleHooks) main).afterStoppingVertx();
        }
    }

}
