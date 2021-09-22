package io.zero88.qwe.eventbus.mock;

import java.util.Optional;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.auth.AuthN;
import io.zero88.qwe.auth.AuthZ;
import io.zero88.qwe.auth.SecurityFilter;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.eventbus.EventListenerExecutor;

import lombok.RequiredArgsConstructor;

@AuthN
@RequiredArgsConstructor
public class MockAuthListener implements EventListener {

    private final SecurityFilter securityFilter;

    public MockAuthListener() {
        this(null);
    }

    @EBContract(action = "CHECK")
    public String check() {
        return "success";
    }

    @AuthZ(role = "admin")
    @EBContract(action = "CREATE")
    public String create() {
        return "success";
    }

    @AuthZ(perm = "query")
    @EBContract(action = "QUERY")
    public String query() {
        return "success";
    }

    @Override
    public EventListenerExecutor executor(SharedDataLocalProxy sharedData) {
        return Optional.ofNullable(securityFilter)
                       .map(sf -> EventListenerExecutor.create(this, sharedData, sf))
                       .orElseGet(() -> EventListener.super.executor(sharedData));
    }

}
