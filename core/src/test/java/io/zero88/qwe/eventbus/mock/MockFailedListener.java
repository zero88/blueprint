package io.zero88.qwe.eventbus.mock;

import java.util.Collections;
import java.util.Map;

import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;

public class MockFailedListener implements EventListener {

    @EBContract(action = "DUP")
    public Map<String, String> dup1(RequestData data) {
        return Collections.singletonMap("key", "duplicate 1");
    }

    @EBContract(action = "DUP")
    public Map<String, String> dup2(RequestData data) {
        return Collections.singletonMap("key", "duplicate 2");
    }

    @EBContract(action = "ERR")
    public void err() {
        throw new IllegalArgumentException("Exception");
    }

}
