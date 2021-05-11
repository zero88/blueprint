package io.zero88.qwe.storage.json.service;

import java.io.FileInputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.github.zero88.utils.FileUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.zero88.qwe.ComponentTestHelper;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.event.EventAction;
import io.zero88.qwe.event.EventBusClient;
import io.zero88.qwe.event.EventMessage;
import io.zero88.qwe.file.FileOption;
import io.zero88.qwe.file.TextFileOperatorImpl;
import io.zero88.qwe.storage.json.JsonStorageProvider;
import io.zero88.qwe.storage.json.StorageConfig;
import io.zero88.qwe.utils.Configs;

@ExtendWith(VertxExtension.class)
class JsonStorageServiceTest {

    StorageConfig config;
    EventBusClient client;

    @BeforeEach
    void before(Vertx vertx, VertxTestContext context, @TempDir Path tmp) throws InterruptedException {
        config = StorageConfig.builder().build().makeFullPath(tmp);
        ComponentTestHelper.deploy(vertx, context, config.toJson(), new JsonStorageProvider(), tmp);
        client = EventBusClient.create(SharedDataLocalProxy.create(vertx, JsonStorageServiceTest.class.getName()));
    }

    @Test
    void test_create_new(VertxTestContext context) {
        final JsonObject data = new JsonObject().put("ab", 12);
        final JsonInput ji = JsonInput.builder()
                                      .file("test")
                                      .dataToInsert(data)
                                      .fileOption(FileOption.builder().strict(false).build())
                                      .build();
        final EventMessage msg = EventMessage.initial(EventAction.CREATE_OR_UPDATE,
                                                      RequestData.builder().body(ji.toJson()).build());
        final String file = config.fullPath().resolve(ji.getFile()).toString();
        FileUtils.createFolder(config.fullPath().toAbsolutePath(), null);
        client.request(config.getServiceAddress(), msg)
              .onFailure(context::failNow)
              .onSuccess(result -> context.verify(() -> {
                  System.out.println(result.toJson());
                  final JsonObject json = Configs.readAsJson(new FileInputStream(file));
                  Assertions.assertTrue(result.isSuccess());
                  Assertions.assertNotNull(result.getData());
                  Assertions.assertEquals(data, result.getData().getJsonObject(ji.getOutputKey()));
                  Assertions.assertEquals(data, json);
                  context.completeNow();
              }));
    }

    @Test
    void test_add_new_item(Vertx vertx, VertxTestContext context) {
        final JsonObject toInsert = new JsonObject().put("k", 3);
        final JsonInput ji = JsonInput.builder().file("test").pointer("/hh").dataToInsert(toInsert).build();
        final JsonObject data = new JsonObject().put("cd", new JsonObject().put("1", "x").put("3", "y"));
        final Path file = config.fullPath().resolve(ji.getFile());
        final EventMessage msg = EventMessage.initial(EventAction.CREATE_OR_UPDATE,
                                                      RequestData.builder().body(ji.toJson()).build());
        TextFileOperatorImpl.builder()
                            .vertx(vertx)
                            .build()
                            .write(file, FileOption.builder().build(), data.toBuffer())
                            .flatMap(g -> client.request(config.getServiceAddress(), msg))
                            .onFailure(context::failNow)
                            .onSuccess(result -> context.verify(() -> {
                                final JsonObject json = Configs.readAsJson(new FileInputStream(file.toString()));
                                System.out.println(result.toJson());
                                System.out.println(json);
                                Assertions.assertTrue(result.isSuccess());
                                Assertions.assertNotNull(result.getData());
                                Assertions.assertEquals(toInsert, result.getData().getJsonObject(ji.getOutputKey()));
                                Assertions.assertEquals(data.mergeIn(new JsonObject().put("hh", toInsert)), json);
                                context.completeNow();
                            }));
    }

    @Test
    void test_update_existed_item(Vertx vertx, VertxTestContext context) {
        final String toInsert = "z";
        final JsonInput ji = JsonInput.builder().file("test").pointer("/cd/1").dataToInsert(toInsert).build();
        final JsonObject data = new JsonObject().put("cd", new JsonObject().put("1", "x").put("3", "y"));
        final Path file = config.fullPath().resolve(ji.getFile());
        final EventMessage msg = EventMessage.initial(EventAction.CREATE_OR_UPDATE,
                                                      RequestData.builder().body(ji.toJson()).build());
        TextFileOperatorImpl.builder()
                            .vertx(vertx)
                            .build()
                            .write(file, FileOption.builder().build(), data.toBuffer())
                            .flatMap(g -> client.request(config.getServiceAddress(), msg))
                            .onComplete(context.succeeding(result -> context.verify(() -> {
                                final JsonObject json = Configs.readAsJson(new FileInputStream(file.toString()));
                                System.out.println(json);
                                System.out.println(result.toJson());
                                Assertions.assertTrue(result.isSuccess());
                                Assertions.assertNotNull(result.getData());
                                Assertions.assertEquals(toInsert, result.getData().getString(ji.getOutputKey()));
                                Assertions.assertEquals(new JsonObject("{\"cd\":{\"1\":\"z\",\"3\":\"y\"}}"), json);
                                context.completeNow();
                            })));
    }

    @Test
    void test_query(Vertx vertx, VertxTestContext context) {
        final JsonInput ji = JsonInput.builder().file("test").pointer("/cd/3").build();
        final JsonObject data = new JsonObject().put("cd", new JsonObject().put("1", "x").put("3", "y"));
        final Path file = config.fullPath().resolve(ji.getFile());
        final EventMessage msg = EventMessage.initial(EventAction.parse("QUERY"),
                                                      RequestData.builder().body(ji.toJson()).build());
        TextFileOperatorImpl.builder()
                            .vertx(vertx)
                            .build()
                            .write(file, FileOption.builder().build(), data.toBuffer())
                            .flatMap(g -> client.request(config.getServiceAddress(), msg))
                            .onComplete(context.succeeding(result -> context.verify(() -> {
                                System.out.println(result.toJson());
                                Assertions.assertTrue(result.isSuccess());
                                Assertions.assertNotNull(result.getData());
                                Assertions.assertEquals("y", result.getData().getString("data"));
                                context.completeNow();
                            })));
    }

    @Test
    void test_remove_json(Vertx vertx, VertxTestContext context) {
        final JsonObject data = new JsonObject().put("cd", new JsonObject().put("1", "x").put("3", "y"));
        final JsonInput ji = JsonInput.builder().file("test").pointer("/cd").keyToRemove("3").build();
        final Path file = config.fullPath().resolve(ji.getFile());
        final EventMessage msg = EventMessage.initial(EventAction.REMOVE,
                                                      RequestData.builder().body(ji.toJson()).build());
        TextFileOperatorImpl.builder()
                            .vertx(vertx)
                            .build()
                            .write(file, FileOption.builder().build(), data.toBuffer())
                            .flatMap(g -> client.request(config.getServiceAddress(), msg))
                            .onComplete(context.succeeding(result -> context.verify(() -> {
                                final JsonObject json = Configs.readAsJson(new FileInputStream(file.toString()));
                                System.out.println(json);
                                System.out.println(result.toJson());
                                Assertions.assertTrue(result.isSuccess());
                                Assertions.assertNotNull(result.getData());
                                Assertions.assertEquals(new JsonObject().put("cd", new JsonObject().put("1", "x")),
                                                        json);
                                Assertions.assertEquals(new JsonObject().put("3", "y"),
                                                        result.getData().getJsonObject("data"));
                                context.completeNow();
                            })));
    }

}
