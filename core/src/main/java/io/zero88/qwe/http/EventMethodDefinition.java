package io.zero88.qwe.http;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.exceptions.ServiceNotFoundException;
import io.zero88.qwe.utils.PriorityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * It helps define a mapping between dynamic route by {@code regex path} and {@code HttpMethod} with {@code EventAction}
 * that used by specific {@code EventBus address}
 *
 * @see EventMethodMapping
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventMethodDefinition implements JsonData {

    /**
     * The service regex path
     */
    @EqualsAndHashCode.Include
    private final String regexPath;
    private final Set<EventMethodMapping> mapping;
    /**
     * Web Router order
     */
    @JsonIgnore
    private final int order;

    private EventMethodDefinition(String regexPath, Set<EventMethodMapping> mapping) {
        if (regexPath.endsWith("/[^/]+")) {
            throw new IllegalArgumentException("Service regex path cannot end with capture parameter");
        }
        this.regexPath = regexPath;
        this.order = PriorityUtils.priorityOrder(regexPath.length());
        this.mapping = mapping;
    }

    /**
     * Create default definition with default {@link ActionMethodMapping#CRUD_MAP}.
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} in default {@code
     *                    HTTPMethod} list
     * @param paramPath   Parameter path for manipulating {@code resource}
     * @return new instance
     * @implNote {@code paramPath} will be append after {@code servicePath}. For example:
     *     <ul>
     *     <li>{@code servicePath}: {@code /client/:clientId/product}</li>
     *     <li>{@code paramPath}: {@code /:productId}</li>
     *     </ul>
     * @see #createDefault(String, String, boolean)
     */
    public static EventMethodDefinition createDefault(String servicePath, String paramPath) {
        return createDefault(servicePath, paramPath, true);
    }

    /**
     * Create default definition with default {@link ActionMethodMapping#CRUD_MAP}.
     *
     * @param servicePath    Origin service path that represents for manipulating {@code resource} in default {@code
     *                       HTTPMethod} list
     * @param paramPath      Parameter path for manipulating {@code resource}
     * @param useRequestData Whether you use {@link RequestData} in parameter in {@link EventListener} or not
     * @return new instance
     * @implNote {@code paramPath} will be append after {@code servicePath}. For example:
     *     <ul>
     *     <li>{@code servicePath}: {@code /client/:clientId/product}</li>
     *     <li>{@code paramPath}: {@code /:productId}</li>
     *     </ul>
     */
    public static EventMethodDefinition createDefault(String servicePath, String paramPath, boolean useRequestData) {
        return create(servicePath, Strings.requireNotBlank(paramPath), ActionMethodMapping.CRUD_MAP, useRequestData);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code singleton resource} with no key or {@code action job}, e.g: {@code
     * /translate}
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} or {@code action job}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, @NonNull ActionMethodMapping mapping) {
        return create(servicePath, mapping, true);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code singleton resource} with no key or {@code action job}, e.g: {@code translate}
     *
     * @param servicePath    Origin service path that represents for manipulating {@code resource} or {@code action
     *                       job}
     * @param mapping        Mapping between {@code EventAction} and {@code HTTPMethod}
     * @param useRequestData Use RequestData {@code RequestData}
     * @return new instance
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, @NonNull ActionMethodMapping mapping,
                                               boolean useRequestData) {
        return create(servicePath, null, mapping, useRequestData);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code resource} with common {@code CRUD} operations
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource}
     * @param paramPath   Parameter path for manipulating {@code resource}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance
     * @implNote {@code paramPath} will be append after {@code servicePath}. For example:
     *     <ul>
     *     <li>{@code servicePath}: {@code /client/:clientId/product}</li>
     *     <li>{@code paramPath}: {@code /:productId}</li>
     *     </ul>
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, String paramPath,
                                               @NonNull ActionMethodMapping mapping) {
        return create(servicePath, paramPath, mapping, true);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code resource} with common {@code CRUD} operations
     *
     * @param servicePath    Origin service path that represents for manipulating {@code resource}
     * @param paramPath      Parameter path for manipulating {@code resource}
     * @param mapping        Mapping between {@code EventAction} and {@code HTTPMethod}
     * @param useRequestData Use request data {@code RequestData}
     * @return new instance
     * @implNote {@code paramPath} will be append after {@code servicePath}. For example:
     *     <ul>
     *     <li>{@code servicePath}: {@code /client/:clientId/product}</li>
     *     <li>{@code paramPath}: {@code /:productId}</li>
     *     </ul>
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, String paramPath,
                                               @NonNull ActionMethodMapping mapping, boolean useRequestData) {
        final String sPath = Urls.combinePath(Strings.requireNotBlank(servicePath));
        final String pPath = Strings.isBlank(paramPath) ? paramPath : Urls.toCapture(paramPath);
        if ((Strings.isBlank(pPath) || sPath.equals(pPath)) && mapping.hasDuplicateMethod()) {
            throw new IllegalStateException("Has duplicate HTTP method for same endpoint");
        }
        final String cPath = Strings.isBlank(pPath) ? sPath : Urls.combinePath(sPath, pPath);
        final HttpPathRule rule = HttpPathRuleLoader.getInstance().get();
        return create(sPath, mapping.get()
                                    .entrySet()
                                    .stream()
                                    .map(e -> new EventMethodMapping(e.getKey(), e.getValue(),
                                                                     rule.createCapture(e.getValue(), e.getKey(), sPath,
                                                                                        cPath), useRequestData))
                                    .collect(Collectors.toSet()));
    }

    public static EventMethodDefinition create(String servicePath, Set<EventMethodMapping> mapping) {
        return new EventMethodDefinition(HttpPathRuleLoader.getInstance().get().createRegex(servicePath), mapping);
    }

    public static EventMethodDefinition from(@NonNull JsonObject json) {
        return new EventMethodDefinition(
            HttpPathRuleLoader.getInstance().get().createRegex(json.getString("regexPath")),
            json.getJsonArray("mapping", new JsonArray())
                .stream()
                .map(o -> JsonData.from(o, EventMethodMapping.class))
                .collect(Collectors.toSet()));
    }

    @JsonCreator
    public static EventMethodDefinition create(Map<String, Object> json) {
        return from(JsonObject.mapFrom(json));
    }

    public Collection<EventMethodMapping> getMapping() {
        return mapping.stream()
                      .sorted((o1, o2) -> compare(o1.method(), o2.method()))
                      .sorted((o1, o2) -> compare(Strings.fallback(o1.getCapturePath(), regexPath),
                                                  Strings.fallback(o2.getCapturePath(), regexPath)))
                      .collect(Collectors.toList());
    }

    private int compare(String n1, String n2) {
        final int c = n1.length() - n2.length();
        return c == 0 ? n1.compareTo(n2) : c;
    }

    public EventMethodMapping searchMapping(String actualPath, @NonNull HttpMethod method) {
        return mapping.stream()
                      .filter(m -> Strings.isNotBlank(m.getRegexPath()))
                      .filter(m -> m.getMethod() == method && actualPath.matches(m.getRegexPath()))
                      .findFirst()
                      .orElseThrow(() -> new ServiceNotFoundException(
                          Strings.format("Unsupported HTTP method [{0}][{1}]", method, actualPath)));
    }

    public EventAction search(String actualPath, @NonNull HttpMethod method) {
        return searchMapping(actualPath, method).getAction();
    }

    public boolean test(String actualPath, EventAction action) {
        return mapping.stream()
                      .filter(m -> Strings.isNotBlank(m.getRegexPath()))
                      .anyMatch(m -> m.getAction() == action && actualPath.matches(m.getRegexPath()));
    }

    public boolean test(String actualPath, HttpMethod method) {
        return mapping.stream()
                      .filter(m -> Strings.isNotBlank(m.getRegexPath()))
                      .anyMatch(m -> m.getMethod() == method && actualPath.matches(m.getRegexPath()));
    }

    public boolean test(String actualPath) {
        return Strings.isNotBlank(actualPath) &&
               actualPath.matches(HttpPathRuleLoader.getInstance().get().createRegexPathForSearch(regexPath));
    }

}
