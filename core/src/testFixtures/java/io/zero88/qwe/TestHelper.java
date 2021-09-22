package io.zero88.qwe;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.LoggerFactory;

import io.github.zero88.utils.Strings;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public interface TestHelper {

    int TEST_TIMEOUT_SEC = 8;

    org.slf4j.Logger LOGGER = LoggerFactory.getLogger("LOG_TEST");

    static int getRandomPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void setup() {
        setup(Level.DEBUG);
    }

    static void setup(Level projLogLvl) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("io.zero88")).setLevel(projLogLvl);
    }

    static void testComplete(Async async) {
        testComplete(async, "", null);
    }

    static void testComplete(Async async, String msgEvent, Handler<Void> completeAction) {
        LOGGER.info("Current Test Async Count: " + async.count() + ". Countdown...");
        if (Strings.isBlank(msgEvent)) {
            LOGGER.debug(msgEvent);
        }
        if (async.count() > 0) {
            async.countDown();
        }
        if (async.count() == 0 && !async.isCompleted()) {
            async.complete();
            if (Objects.nonNull(completeAction)) {
                completeAction.handle(null);
            }
        }
    }

    static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static <T extends Throwable> void assertCause(Executable ex, Class<T> throwable,
                                                  Class<? extends Throwable> causeClass) {
        TestHelper.assertCause(ex, throwable, causeClass, null);
    }

    static <T extends Throwable> void assertCause(Executable ex, Class<T> throwable,
                                                  Class<? extends Throwable> causeClass, String throwMsg) {
        Assertions.assertNotNull(assertCause(Assertions.assertThrows(throwable, ex, throwMsg), causeClass));
    }

    static <T extends Throwable> void assertCause(Executable ex, Class<T> throwable,
                                                  Class<? extends Throwable> causeClass, String throwMsg,
                                                  String causeMsg) {
        Throwable cause = assertCause(Assertions.assertThrows(throwable, ex, throwMsg), causeClass);
        Assertions.assertNotNull(cause);
        Assertions.assertEquals(causeMsg, cause.getMessage());
    }

    static Throwable assertCause(Throwable ex, Class<? extends Throwable> causeClass) {
        ex.printStackTrace(System.out);
        Assertions.assertTrue(causeClass.isInstance(ex.getCause()));
        return ex.getCause();
    }

    static void waitTimeout(int timeout, CountDownLatch latch) {
        waitTimeout(timeout, latch, null);
    }

    static void waitTimeout(int timeout, CountDownLatch latch, Handler<Throwable> ifFailed) {
        try {
            Assertions.assertTrue(latch.await(timeout, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Optional.ofNullable(ifFailed)
                    .orElseGet(() -> t -> Assertions.fail("Interrupted when waiting timeout [" + timeout + "(s)]", t))
                    .handle(e);
        }
    }

}
