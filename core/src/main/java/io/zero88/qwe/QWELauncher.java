package io.zero88.qwe;

import java.util.Arrays;

import io.vertx.core.Launcher;
import io.zero88.qwe.launcher.AboutCommand;

public class QWELauncher extends Launcher {

    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    public QWELauncher() {
        unregister("bare").unregister("test");
    }

    @Override
    public void execute(String command, String... cla) {
        if (command != null && !isAskingForHelp(command) && !isAskingForVersion(command) &&
            (cla.length >= 1 && !isAskingForHelp(cla[0])) && hasBanner(command)) {
            AboutCommand.printBanner();
        }
        super.execute(command, cla);
    }

    protected boolean hasBanner(String command) {
        return !Arrays.asList("about", "version").contains(command);
    }

    public static void main(String[] args) {
        new QWELauncher().dispatch(args);
    }

}
