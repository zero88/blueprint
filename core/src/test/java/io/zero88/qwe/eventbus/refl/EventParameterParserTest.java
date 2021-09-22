package io.zero88.qwe.eventbus.refl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.eventbus.EBBody;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventListenerTest;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.mock.MockEventListener;
import io.zero88.qwe.eventbus.mock.MockWithContextListener;
import io.zero88.qwe.eventbus.mock.MockWithVariousParamsListener;

@ExtendWith(VertxExtension.class)
class EventParameterParserTest {

    EventAnnotationProcessor processor;
    EventParameterParser parser;

    @BeforeEach
    void setup(Vertx vertx) {
        processor = EventAnnotationProcessor.create();
        parser = EventParameterParser.create(SharedDataLocalProxy.create(vertx, EventListenerTest.class.getName()),
                                             JsonData.MAPPER);
    }

    @Test
    void test_extract_primitive_but_send_null() {
        final EventMessage msg = EventMessage.initial(MockEventListener.PRIMITIVE_EVENT,
                                                      new JsonObject().put("id", null));
        final MethodMeta meta = processor.lookup(MockWithVariousParamsListener.class, msg.getAction());
        Assertions.assertEquals(1, meta.params().length);
        Assertions.assertEquals("Data Field [id] is primitive type but given null data",
                                Assertions.assertThrows(IllegalArgumentException.class,
                                                        () -> parser.extract(msg, meta.params())).getMessage());
    }

    @Test
    void test_extract_string() {
        final EventMessage msg = EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", "123"));
        final MethodMeta meta = processor.lookup(MockWithVariousParamsListener.class, msg.getAction());
        final Object[] inputs = parser.extract(msg, meta.params());
        Assertions.assertEquals(1, inputs.length);
        Assertions.assertEquals("123", inputs[0]);
    }

    @Test
    void test_extract_all_context() {
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("id", "123")).build();
        final EventMessage msg = EventMessage.initial(EventAction.GET_ONE, reqData);
        final MethodMeta meta = processor.lookup(MockWithContextListener.class, msg.getAction());
        final Object[] inputs = parser.extract(msg, meta.params());
        Assertions.assertEquals(5, inputs.length);
        Assertions.assertEquals(EventAction.GET_ONE, inputs[0]);
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[1].getClass(), Vertx.class));
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[2].getClass(), SharedDataLocalProxy.class));
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[3].getClass(), EventBusClient.class));
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[4].getClass(), RequestData.class));
        Assertions.assertEquals(reqData.toJson(), ((RequestData) inputs[4]).toJson());
    }

    @Test
    void test_extract_mixin_context_param_position() {
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("test", 1)).build();
        final EventMessage msg = EventMessage.initial(EventAction.PATCH, reqData);
        final MethodMeta meta = processor.lookup(MockWithContextListener.class, msg.getAction());
        final Object[] inputs = parser.extract(msg, meta.params());
        Assertions.assertEquals(4, inputs.length);
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[0].getClass(), RequestFilter.class));
        Assertions.assertEquals(new RequestFilter(reqData.filter().toJson()).toJson(),
                                ((RequestFilter) inputs[0]).toJson());
        Assertions.assertEquals(EventAction.PATCH, inputs[1]);
        Assertions.assertTrue(ReflectionClass.assertDataType(inputs[2].getClass(), EventBusClient.class));
        Assertions.assertNull(inputs[3]);
        Assertions.assertEquals("body", meta.params()[3].getParamName());
        Assertions.assertEquals(JsonObject.class, meta.params()[3].getParamClass());
    }

    @Test
    void test_register_many_actions_then_inject_correct_runtime_action() {
        final EventMessage msg1 = EventMessage.initial(EventAction.CREATE, new JsonObject().put("id", 111));
        final MethodMeta meta1 = processor.lookup(MockWithContextListener.class, msg1.getAction());
        final Object[] inputs1 = parser.extract(msg1, meta1.params());
        Assertions.assertEquals(2, inputs1.length);
        Assertions.assertEquals(EventAction.CREATE, inputs1[0]);
        Assertions.assertEquals(111, inputs1[1]);

        final EventMessage msg2 = EventMessage.initial(EventAction.UPDATE, new JsonObject().put("id", 222));
        final MethodMeta meta2 = processor.lookup(MockWithContextListener.class, msg2.getAction());
        final Object[] inputs2 = parser.extract(msg2, meta2.params());
        Assertions.assertEquals(2, inputs2.length);
        Assertions.assertEquals(EventAction.UPDATE, inputs2[0]);
        Assertions.assertEquals(222, inputs2[1]);
    }

    @Test
    void test_extract_body_and_headers() {
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put("id", 1))
                                               .headers(new JsonObject().put("hello", "world"))
                                               .build();
        final EventMessage msg = EventMessage.initial(EventAction.parse("BODY"), reqData);
        final MethodMeta meta = processor.lookup(MockWithVariousParamsListener.class, msg.getAction());
        final Object[] inputs = parser.extract(msg, meta.params());
        Assertions.assertEquals(2, inputs.length);

        final EBBody annotation = meta.params()[0].lookupAnnotation(EBBody.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals("id", annotation.value());
        Assertions.assertEquals(Integer.class, meta.params()[0].getParamClass());
        Assertions.assertEquals(1, inputs[0]);

        Assertions.assertEquals("headers", meta.params()[1].getParamName());
        Assertions.assertEquals(JsonObject.class, meta.params()[1].getParamClass());
        Assertions.assertEquals(new JsonObject().put("hello", "world"), inputs[1]);
    }

    @Test
    void test_use_EBBody_but_non_standard_message() {
        final EventMessage msg = EventMessage.initial(EventAction.parse("BODY"), new JsonObject().put("id", 2));
        final MethodMeta meta = processor.lookup(MockWithVariousParamsListener.class, msg.getAction());
        final Object[] inputs = parser.extract(msg, meta.params());
        Assertions.assertEquals(2, inputs.length);

        final EBBody annotation = meta.params()[0].lookupAnnotation(EBBody.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals(Integer.class, meta.params()[0].getParamClass());
        Assertions.assertEquals(2, inputs[0]);

        Assertions.assertEquals("headers", meta.params()[1].getParamName());
        Assertions.assertEquals(JsonObject.class, meta.params()[1].getParamClass());
        Assertions.assertNull(inputs[1]);
    }

}
