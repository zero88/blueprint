package io.zero88.qwe.dto.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.zero88.jpa.Sortable;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.utils.JsonUtils.JsonCollectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public final class Sort implements Sortable, JsonData {

    private final Map<String, Order> items;

    public static Sort from(String requestParam) {
        if (Strings.isBlank(requestParam)) {
            return null;
        }
        return Sort.builder()
                   .items(Stream.of(requestParam.split(","))
                                .filter(Strings::isNotBlank)
                                .map(Sort::each)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Order::property, Function.identity())))
                   .build();
    }

    @JsonCreator
    static Sort create(@NonNull Map<String, String> data) {
        return Sort.builder()
                   .items(data.entrySet()
                              .stream()
                              .map(Sort::each)
                              .filter(Objects::nonNull)
                              .collect(Collectors.toMap(Order::property, Function.identity())))
                   .build();
    }

    private static Order each(String value) {
        if (Strings.isBlank(value)) {
            return null;
        }
        final char c = value.charAt(0);
        Direction type = Direction.parse(c);
        String resource = c == Direction.ASC.getSymbol() || type.isDESC() ? value.substring(1) : value;
        return Order.by(resource, type);
    }

    private static Order each(@NonNull Entry<String, String> entry) {
        if (Strings.isBlank(entry.getKey())) {
            return null;
        }
        Direction type = Direction.parse(entry.getValue());
        if (type == null) {
            return null;
        }
        return Order.by(entry.getKey(), type);
    }

    @Override
    public @NonNull Collection<Order> orders() {
        return items.values();
    }

    @Override
    public Order get(String property) {
        return items.get(property);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public JsonObject toJson() {
        return orders().stream().collect(JsonCollectors.toJson(Order::property, Order::direction));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        public Builder item(@NonNull String property, @NonNull Direction direction) {
            return item(Order.by(property, direction));
        }

        public Builder item(@NonNull Order order) {
            items = Optional.ofNullable(items).orElseGet(HashMap::new);
            items.put(order.property(), order);
            return this;
        }

    }

}
