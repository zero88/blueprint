package io.zero88.qwe.micro;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Status;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.dto.msg.ResponseData;
import io.zero88.qwe.exceptions.ConflictException;
import io.zero88.qwe.exceptions.ServiceException;
import io.zero88.qwe.exceptions.ServiceNotFoundException;
import io.zero88.qwe.exceptions.ServiceUnavailable;
import io.zero88.qwe.micro.executor.ServiceExecutor;
import io.zero88.qwe.micro.executor.ServiceExecutorFactory;
import io.zero88.qwe.micro.filter.PredicateFactoryLoader;
import io.zero88.qwe.micro.filter.RecordPredicateFactory.SearchFlag;
import io.zero88.qwe.micro.filter.ServiceFilterParam;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@SuppressWarnings("rawtypes")
final class ServiceDiscoveryApiImpl implements ServiceDiscoveryApi {

    @Getter
    @Accessors(fluent = true)
    private final SharedDataLocalProxy sharedData;
    private final ServiceDiscovery sd;
    private final PredicateFactoryLoader predicateLoader;
    private final ServiceExecutorFactory executorFactory;

    ServiceDiscoveryApiImpl(SharedDataLocalProxy sharedData, ServiceDiscoveryConfig config) {
        this.sharedData = sharedData;
        this.sd = ServiceDiscovery.create(sharedData.getVertx(), config);
        this.predicateLoader = new PredicateFactoryLoader();
        this.executorFactory = new ServiceExecutorFactory();
    }

    @Override
    public ServiceDiscovery get() {
        return Objects.requireNonNull(sd, "Service Discovery is not enabled");
    }

    public Future<Record> register(@NonNull Record record) {
        return doRegister(decorator(record));
    }

    @Override
    public Future<List<Record>> register(@NonNull List<Record> records) {
        return CompositeFuture.join(records.stream().map(this::register).collect(Collectors.toList()))
                              .flatMap(ar -> Future.succeededFuture(ar.list()));
    }

    @Override
    public Future<Record> update(@NonNull Record record) {
        return this.findOne(new RequestFilter().put(ServiceFilterParam.IDENTIFIER, record.getRegistration()))
                   .flatMap(r -> update(r, record));
    }

    @Override
    public Future<List<Record>> batchUpdate(@NonNull RequestFilter filter, @NonNull JsonObject updateData) {
        final Record c = new Record(updateData);
        return this.findMany(filter)
                   .map(rr -> rr.stream().map(r -> update(r, c)).collect(Collectors.<Future>toList()))
                   .flatMap(CompositeFuture::join)
                   .flatMap(ar -> Future.succeededFuture(ar.list()));
    }

    @Override
    public Future<JsonObject> unregister(@NonNull RequestFilter filter) {
        return this.findMany(filter)
                   .map(rr -> rr.stream().map(this::unregister).collect(Collectors.<Future>toList()))
                   .flatMap(CompositeFuture::join)
                   .map(ar -> {
                       List<JsonObject> causes = ar.causes()
                                                   .stream()
                                                   .filter(Objects::nonNull)
                                                   .map(ErrorMessage::parse)
                                                   .map(ErrorMessage::toJson)
                                                   .collect(Collectors.toList());
                       return new JsonObject().put("total", ar.size())
                                              .put("removed", ar.size() - causes.size())
                                              .put("filter", filter)
                                              .put("errors", causes);
                   });
    }

    @Override
    public Future<Record> findOne(@NonNull RequestFilter filter) {
        return find(filter, SearchFlag.ONE).map(recs -> recs.stream().reduce((m1, m2) -> {
            if (!m1.equals(m2)) {
                throw new ServiceException("Service conflict", new ConflictException(
                    "More than one service by given parameters [" + filter + "]"));
            }
            return m1;
        }).orElseThrow(() -> new ServiceNotFoundException("Not found service by given parameters [" + filter + "]")));
    }

    @Override
    public Future<List<Record>> findMany(RequestFilter filter) {
        return find(Optional.ofNullable(filter).orElseGet(RequestFilter::new), SearchFlag.MANY);
    }

    @Override
    public Future<ResponseData> execute(@NonNull RequestFilter filter, RequestData requestData) {
        return findOne(filter).flatMap(r -> Optional.ofNullable(executorFactory.lookup(r.getType()))
                                                    .map(s -> execute(r, s, requestData, filter))
                                                    .orElseThrow(() -> new ServiceUnavailable(
                                                        "Unknown service executor [" + r.getType() + "]")));
    }

    private Future<Record> doRegister(@NonNull Record record) {
        return get().publish(record).onSuccess(rec -> {
            logger().info("Published Service | Registration[{}] | API[{}] | Type[{}] | Endpoint[{}]",
                          rec.getRegistration(), rec.getName(), rec.getType(), rec.getLocation().getString("endpoint"));
            if (logger().isTraceEnabled()) {
                logger().trace("Published Service [{}]", rec.toJson());
            }
        }).onFailure(t -> logger().error("Cannot publish record[{}]", record.toJson(), t));
    }

    private Future<List<Record>> find(RequestFilter filter, SearchFlag searchFlag) {
        return predicateLoader.getPredicatesFactories()
                              .stream()
                              .map(p -> p.apply(filter, searchFlag))
                              .reduce(Predicate::and)
                              .map(predicate -> get().getRecords(predicate::test, true))
                              .orElseGet(() -> get().getRecords(filter))
                              .onSuccess(recs -> logger().info("Found [{}] record(s) by filter [{}][{}]", recs.size(),
                                                               searchFlag, filter.toJson()));
    }

    private Future<Record> update(Record prev, Record current) {
        Status status = current.getStatus() == Status.UNKNOWN ? prev.getStatus() : current.getStatus();
        return get().update(new Record(prev.toJson().mergeIn(current.toJson(), true)).setStatus(status))
                    .onSuccess(r -> logger().info("Updated record[{}][{}]", prev.getType(), prev.getRegistration()));
    }

    private Future<Void> unregister(Record r) {
        return get().unpublish(r.getRegistration())
                    .onSuccess(v -> logger().info("Unregistered record[{}][{}]", r.getType(), r.getRegistration()))
                    .onFailure(t -> logger().warn("Cannot un-published record[{}]", r.toJson(), t));
    }

    private Record decorator(Record record) {
        if (!HttpEndpoint.TYPE.equals(record.getType())) {
            return record;
        }
        HttpLocation loc = new HttpLocation(record.getLocation());
        if (Objects.nonNull(loc.getHost())) {
            return record;
        }
        return record.setLocation(loc.setHost(sharedData().getData(SharedDataLocalProxy.PUBLIC_IPV4_KEY)).toJson());
    }

    private Future<ResponseData> execute(Record record, ServiceExecutor serviceExecutor, RequestData requestData,
                                         RequestFilter filter) {
        final JsonObject configuration = serviceExecutor.getConfiguration(record, sharedData());
        final ServiceReference ref = get().getReferenceWithConfiguration(record, configuration);
        return serviceExecutor.execute(sharedData(), ref, requestData, filter).eventually(ignore -> {
            ref.release();
            return Future.succeededFuture();
        });
    }

}
