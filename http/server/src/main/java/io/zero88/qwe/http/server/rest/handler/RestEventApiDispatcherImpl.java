package io.zero88.qwe.http.server.rest.handler;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.ReqAuthDefinition;
import io.zero88.qwe.auth.UserInfo;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.DeliveryEvent;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.http.server.handler.AuthInterceptor;
import io.zero88.qwe.http.server.handler.RequestDataInterceptor;
import io.zero88.qwe.http.server.handler.RequestInterceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class RestEventApiDispatcherImpl implements RestEventApiDispatcher {

    @Getter
    @Accessors(fluent = true)
    private AuthInterceptor authInterceptor;
    private DeliveryEvent deliveryEvent;
    private String sharedKey;

    @Override
    public RestEventApiDispatcher setup(String sharedKey, ReqAuthDefinition authDefinition,
                                        DeliveryEvent deliveryEvent) {
        this.deliveryEvent = deliveryEvent;
        this.sharedKey = sharedKey;
        this.authInterceptor = AuthInterceptor.create(authDefinition);
        return this;
    }

    @Override
    public RequestInterceptor<RequestData> validator() {
        return deliveryEvent.isUseRequestData() ? RequestDataInterceptor.create() : RequestDataInterceptor.createSlim();
    }

    @Override
    public Future<EventMessage> proceed(RoutingContext context, UserInfo userInfo, RequestData reqData) {
        EventMessage msg = deliveryEvent.isUseRequestData()
                           ? EventMessage.initial(deliveryEvent.getAction(), reqData)
                           : EventMessage.initial(deliveryEvent.getAction(), reqData.body());
        return doDispatch(EventBusClient.create(SharedDataLocalProxy.create(context.vertx(), sharedKey)),
                          deliveryEvent.getAddress(), deliveryEvent.getPattern(), msg.setUserInfo(userInfo));
    }

}
