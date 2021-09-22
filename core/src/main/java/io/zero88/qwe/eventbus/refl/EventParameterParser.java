package io.zero88.qwe.eventbus.refl;

import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.eventbus.EBContext;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.exceptions.ImplementationError;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;

public interface EventParameterParser {

    static EventParameterParser create(SharedDataLocalProxy sharedData, ObjectMapper mapper) {
        return new EventParameterParserImpl().setup(sharedData, mapper);
    }

    /**
     * Setup event parameter parser
     *
     * @param sharedData shared data
     * @param mapper     mapper
     * @return a reference to this for fluent API
     */
    EventParameterParser setup(SharedDataLocalProxy sharedData, ObjectMapper mapper);

    /**
     * Extract event message to an array of param's value
     *
     * @param message Event message
     * @param params  Method params
     * @return An array param value
     * @throws ImplementationError      if binding unsupported {@link EBContext} data type
     * @throws IllegalArgumentException if a registered parameter is unable deserialized from runtime event message
     */
    @NonNull Object[] extract(EventMessage message, MethodParam[] params);

}
