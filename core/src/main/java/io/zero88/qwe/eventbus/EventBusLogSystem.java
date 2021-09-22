package io.zero88.qwe.eventbus;

import io.zero88.qwe.LogSystem;

import lombok.NonNull;

public interface EventBusLogSystem extends LogSystem {

    interface EventListenerLogSystem extends EventBusLogSystem {

        @Override
        default @NonNull String function() {
            return "EBListener";
        }

    }


    interface EventReplyLogSystem extends EventBusLogSystem {

        @Override
        default @NonNull String function() {
            return "EBReply";
        }

    }

}
