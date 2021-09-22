package io.zero88.qwe.eventbus;

import io.github.zero88.exceptions.ReflectionException;
import io.github.zero88.repl.ReflectionMethod;
import io.github.zero88.utils.Functions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.UserInfo;
import io.zero88.qwe.eventbus.output.AnyToFuture;
import io.zero88.qwe.eventbus.refl.MethodMeta;
import io.zero88.qwe.exceptions.ImplementationError;
import io.zero88.qwe.exceptions.ServiceNotFoundException;
import io.zero88.qwe.exceptions.ServiceUnavailable;
import io.zero88.qwe.exceptions.UnsupportedException;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@SuppressWarnings({"rawtypes", "unchecked"})
public class EventListenerExecutorImpl implements EventListenerExecutor {

    @Getter
    private final EventListener listener;
    @Getter
    private final SharedDataLocalProxy sharedData;

    EventListenerExecutorImpl(EventListener listener, SharedDataLocalProxy sharedData) {
        this.listener = listener;
        this.sharedData = sharedData;
    }

    @Override
    public Future<EventMessage> execute(Message message) {
        final EventMessage msg = messageConverter().from(message);
        if (msg.getUserInfo() == null) {
            msg.setUserInfo(UserInfo.parse(sharedData.getVertx().getOrCreateContext().getLocal(UserInfo.USER_KEY)));
        }
        final String addr = message.address();
        debug("Received message", msg.getAction(), addr);
        return sharedData.getVertx().executeBlocking(promise -> execute(msg, addr).onComplete(promise));
    }

    private Future<EventMessage> execute(EventMessage msg, String address) {
        final EventAction action = msg.getAction();
        debug("Execute", action, address, "...");
        Future<EventMessage> future;
        try {
            final MethodMeta methodMeta = annotationProcessor().lookup(listener.getClass(), action);
            future = this.securityInterceptor()
                         .validate(sharedData, msg, methodMeta.method())
                         .flatMap(i -> executeMethod(methodMeta, paramParser().extract(msg, methodMeta.params())))
                         .map(outputSerializer())
                         .onSuccess(data -> debug("Succeed when handling", action, address))
                         .map(data -> EventMessage.replySuccess(action, data));
        } catch (ImplementationError e) {
            future = Future.failedFuture(new ServiceUnavailable(e));
        } catch (UnsupportedException e) {
            future = Future.failedFuture(new ServiceNotFoundException(e));
        } catch (IllegalArgumentException e) {
            future = Future.failedFuture(e);
        }
        return future.onFailure(t -> debugError(action, address, t)).otherwise(t -> EventMessage.replyError(action, t));
    }

    private Future<Object> executeMethod(MethodMeta methodMeta, Object[] inputs) {
        try {
            final Object response = ReflectionMethod.execute(listener, methodMeta.method(), inputs);
            return outputToFuture().stream()
                                   .filter(h -> Functions.getOrDefault(false, () -> h.verify(methodMeta)))
                                   .findFirst()
                                   .orElseGet(AnyToFuture::new)
                                   .transform(methodMeta, response);
        } catch (ReflectionException e) {
            return Future.failedFuture(e.getCause());
        }
    }

    private void debug(String lifecycleMsg, EventAction action, String address) {
        debug(lifecycleMsg, action, address, "");
    }

    private void debug(String lifecycleMsg, EventAction action, String address, String suffix) {
        debug(lifecycleMsg, action, address, suffix, null);
    }

    private void debugError(EventAction action, String address, Throwable t) {
        debug("Error when handling", action, address, "", t);
    }

    private void debug(String lifecycleMsg, EventAction action, String address, String suffix, Throwable t) {
        listener.logger().debug(listener.decor("{} [{}][{}]{}"), lifecycleMsg, address, action, suffix, t);
    }

}
