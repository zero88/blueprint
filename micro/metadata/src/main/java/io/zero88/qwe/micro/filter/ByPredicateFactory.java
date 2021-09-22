package io.zero88.qwe.micro.filter;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.zero88.qwe.micro.filter.FilterAttributeFinder.FilterStringFinder;
import io.zero88.qwe.utils.JsonUtils;

import lombok.NonNull;

/**
 * Represents for predicate that is built from {@code identifier} and {@code indicator} in the runtime filter
 * <p>
 * For example:
 * <pre>{@code
 * /find?identifier=sample.name&by=name
 * /find?identifier=sample&by=group
 * /find?identifier=uuid&by=registration
 * /find?identifier="/test/123"&by=path
 * }</pre>
 *
 * @see RecordPredicateFactory
 * @see ServiceFilterParam#BY
 * @see ServiceFilterParam#IDENTIFIER
 */
public interface ByPredicateFactory extends RecordPredicateFactory, FilterStringFinder {

    String BY_NAME = ServiceFilterParam.NAME;
    String BY_REGISTRATION = "registration";
    String BY_GROUP = "group";
    String BY_PATH = "path";
    String BY_ENDPOINT = "endpoint";

    @Override
    default @NonNull Predicate<Record> apply(@NonNull JsonObject filter, @NonNull SearchFlag searchFlag) {
        final String identifier = findAttribute(filter);
        if (Strings.isBlank(identifier)) {
            if (searchFlag.isOne()) {
                throw new IllegalArgumentException("Missing record identifier[" + by() + "]");
            }
            return r -> true;
        }
        if (!by().equalsIgnoreCase(findIndicator(filter))) {
            return r -> true;
        }
        return apply(identifier, searchFlag, filter);
    }

    @Override
    default @NotNull String attribute() {
        return ServiceFilterParam.IDENTIFIER;
    }

    /**
     * By which indicator
     *
     * @return the indicator name
     */
    String by();

    /**
     * Build record predicate
     *
     * @param identifier given identifier
     * @param searchFlag search flag
     * @param filter     origin filter
     * @return a record predicate
     */
    Predicate<Record> apply(String identifier, SearchFlag searchFlag, JsonObject filter);

    default String findIndicator(JsonObject filter) {
        return JsonUtils.findString(filter, ServiceFilterParam.BY).orElse(BY_REGISTRATION);
    }

}
