package io.zero88.qwe.rpc;

import java.util.Optional;
import java.util.function.Function;

import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Status;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.dto.ErrorMessageConverter;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.exceptions.ErrorCode;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.exceptions.ServiceNotFoundException;
import io.zero88.qwe.micro.filter.ServiceFilterParam;
import io.zero88.qwe.micro.filter.ServiceScope;
import io.zero88.qwe.micro.transfomer.RecordOutput;
import io.zero88.qwe.micro.transfomer.RecordTransformer.ViewType;

import lombok.NonNull;

/**
 * Helps invoking event service by delegating to {@code service locator} finds service by name then executes with given
 * {@code event action} and {@code request data}
 *
 * @see <a href="https://en.wikipedia.org/wiki/Remote_procedure_call">Remote procedure call</a>
 * @since 1.0.0
 */
public interface GatewayServiceInvoker extends RemoteServiceInvoker {

    /**
     * Gateway index address
     *
     * @return gateway index address
     * @since 1.0.0
     */
    @NonNull String gatewayAddress();

    /**
     * Defines service scope to discover
     *
     * @return service scope. Default: {@link ServiceScope#ALL}
     * @since 1.0.0
     */
    @NonNull
    default ServiceScope scope() {
        return ServiceScope.ALL;
    }

    /**
     * Destination name
     *
     * @return destination name
     * @since 1.0.0
     */
    @NonNull String destination();

    /**
     * Destination service pattern
     *
     * @return service pattern. Default: {@link EventPattern#REQUEST_RESPONSE}
     * @since 1.0.0
     */
    @NonNull
    default EventPattern pattern() {
        return EventPattern.REQUEST_RESPONSE;
    }

    /**
     * Defines whether throw exception if response is error
     *
     * @return {@code true} if want to throw exception
     * @apiNote Default is {@code false}
     */
    default boolean throwIfResponseError() {
        return false;
    }

    /**
     * Do search {@code destination service}.
     *
     * @param action event action to execute
     * @return destination address
     * @since 1.0.0
     */
    default Future<String> search(@NonNull EventAction action) {
        final RequestData searchReq = RequestData.builder()
                                                 .body(new JsonObject().put(ServiceFilterParam.IDENTIFIER,
                                                                            destination()))
                                                 .filter(new JsonObject().put(ServiceFilterParam.BY, "NAME")
                                                                         .put(ServiceFilterParam.STATUS, Status.UP)
                                                                         .put(ServiceFilterParam.SCOPE, scope())
                                                                         .put(ServiceFilterParam.VIEW,
                                                                              ViewType.TECHNICAL)
                                                                         .put(ServiceFilterParam.ACTION,
                                                                              action.action()))
                                                 .build();
        final Future<EventMessage> invoker = invoke(gatewayAddress(), EventAction.GET_ONE, searchReq);
        return invoker.flatMap(out -> out.isError()
                                      ? Future.failedFuture(notFound().apply(out.getError()))
                                      : Future.succeededFuture(
                                          Optional.ofNullable(out.getData()).orElse(new JsonObject())))
                      .map(json -> json.getString(RecordOutput.Fields.endpoint))
                      .flatMap(addr -> Strings.isBlank(addr)
                                       ? Future.failedFuture(new ServiceNotFoundException(
                          "Not found destination address of service name '" + destination() + "'"))
                                       : Future.succeededFuture(addr));
    }

    /**
     * Do invoke remote service
     *
     * @param action given event action
     * @param data   given request body data
     * @return single result from remote service
     * @apiNote result can be single error if remote service not found or not compatible with given event action
     * @apiNote It is equivalent to call {@link #execute(EventAction, RequestData)} with given {@code data} will be
     *     {@code body} of {@code request data}
     * @see EventAction
     * @see RequestData
     * @since 1.0.0
     */
    default Future<JsonObject> execute(@NonNull EventAction action, JsonObject data) {
        return execute(action, RequestData.builder().body(data).build());
    }

    /**
     * Do invoke remote service
     *
     * @param action  given event action
     * @param reqData given request data
     * @return json result can be single error if remote service not found or not compatible with given event action
     * @apiNote Search destination address via {@link #search(EventAction)} then {@link #execute(String,
     *     EventAction, RequestData)}
     * @see EventAction
     * @see RequestData
     * @since 1.0.0
     */
    default Future<JsonObject> execute(@NonNull EventAction action, @NonNull RequestData reqData) {
        return search(action).flatMap(address -> execute(address, action, reqData));
    }

    /**
     * Do invoke remote service.
     *
     * @param address the address
     * @param action  the action
     * @param reqData the req data
     * @return json result
     * @since 1.0.0
     */
    default Future<JsonObject> execute(@NonNull String address, @NonNull EventAction action,
                                       @NonNull RequestData reqData) {
        return this.invoke(address, action, reqData).map(out -> {
            if (out.isError() && throwIfResponseError()) {
                throw new QWEException(out.getError().getCode(), out.getError().getMessage());
            }
            return Optional.ofNullable(out.getError())
                           .map(ErrorMessage::toJson)
                           .orElseGet(() -> Optional.ofNullable(out.getData()).orElseGet(JsonObject::new));
        });
    }

    /**
     * Not found converter function.
     *
     * @return converter function
     * @since 1.0.0
     */
    default Function<ErrorMessage, QWEException> notFound() {
        return msg -> ErrorMessageConverter.override(msg, ErrorCode.SERVICE_NOT_FOUND,
                                                     RemoteServiceInvoker.notFoundMessage(serviceLabel()));
    }

}
