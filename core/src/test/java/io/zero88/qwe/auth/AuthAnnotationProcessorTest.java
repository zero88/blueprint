package io.zero88.qwe.auth;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.zero88.repl.ReflectionMethod;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.auth.mock.MockAuth.MockAuth1;
import io.zero88.qwe.auth.mock.MockAuth.MockAuth2;
import io.zero88.qwe.auth.mock.MockAuth.MockAuthInClass;
import io.zero88.qwe.auth.mock.another.MockAuthAnother;
import io.zero88.qwe.auth.mock.another.MockNoAuth;
import io.zero88.qwe.dto.JsonData;

class AuthAnnotationProcessorTest {

    AuthAnnotationProcessor processor = AuthAnnotationProcessor.create();

    @Test
    void test_authz_in_method_with_single_role() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuth1.class, "m1"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(1, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), Collections.singletonList("admin"), null, null, null);
    }

    @Test
    void test_authz_in_inherit_method() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuth2.class, "m1"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(1, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), Collections.singletonList("super_user"), Collections.singletonList("m1"),
                 null, null);
    }

    @Test
    void test_authz_repeat_in_method() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuth2.class, "m3"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(2, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), null, Arrays.asList("m3", "mm"), null, null);
        doAssert(definition.getAuthz().get(1), null, null, null, "any");
    }

    @Test
    void test_authz_in_package() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuth1.class, "m2"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(1, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), null, null, Collections.singletonList("package"), null);
    }

    @Test
    void test_authz_in_class() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuthInClass.class, "noop"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(1, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), null, null, Collections.singletonList("happy"), null);
    }

    @Test
    void test_authn_only_in_class() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuthAnother.class, "access"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertFalse(definition.isAuthzRequired());
        Assertions.assertTrue(definition.getAuthz().isEmpty());
    }

    @Test
    void test_authz_in_method_under_authn_in_class() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuthAnother.class, "create"));
        Assertions.assertTrue(definition.isLoginRequired());
        Assertions.assertTrue(definition.isAuthzRequired());
        Assertions.assertEquals(1, definition.getAuthz().size());
        doAssert(definition.getAuthz().get(0), null, null, null, "any");
    }

    @Test
    void test_no_auth() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockNoAuth.class, "noop"));
        Assertions.assertFalse(definition.isLoginRequired());
        Assertions.assertFalse(definition.isAuthzRequired());
        Assertions.assertTrue(definition.getAuthz().isEmpty());
    }

    @Test
    void test_serialize_deserialize() {
        final ReqAuthDefinition definition = processor.lookup(findMethod(MockAuth2.class, "m3"));
        final JsonObject expected = new JsonObject("{\"loginRequired\":true,\"authz\":[{\"allowRoles\":[]," +
                                                   "\"allowPerms\":[\"m3\",\"mm\"],\"allowGroups\":[]," +
                                                   "\"customAccessRule\":\"\"},{\"allowRoles\":[],\"allowPerms\":[]," +
                                                   "\"allowGroups\":[],\"customAccessRule\":\"any\"}]}");
        JsonHelper.assertJson(expected, definition.toJson());
        final ReqAuthDefinition from = JsonData.from(expected, ReqAuthDefinition.class);
        Assertions.assertTrue(from.isLoginRequired());
        Assertions.assertTrue(from.isAuthzRequired());
        Assertions.assertFalse(from.getAuthz().isEmpty());
        doAssert(from.getAuthz().get(0), definition.getAuthz().get(0).getAllowRoles(),
                 definition.getAuthz().get(0).getAllowPerms(), definition.getAuthz().get(0).getAllowGroups(),
                 definition.getAuthz().get(0).getCustomAccessRule());
    }

    private Method findMethod(Class<?> cls, String methodName) {
        Method method = ReflectionMethod.find(m -> m.getName().equals(methodName), cls).findFirst().orElse(null);
        if (method == null) {
            Assertions.fail("Not found method [" + methodName + "] in [" + cls.getName() + "]");
        }
        return method;
    }

    private void doAssert(ReqAuthZDefinition definition, List<String> roles, List<String> perms, List<String> groups,
                          String custom) {
        if (Objects.nonNull(roles)) {
            Assertions.assertEquals(roles, definition.getAllowRoles());
        } else {
            Assertions.assertTrue(Objects.isNull(definition.getAllowRoles()) || definition.getAllowRoles().isEmpty());
        }
        if (Objects.nonNull(perms)) {
            Assertions.assertEquals(perms, definition.getAllowPerms());
        } else {
            Assertions.assertTrue(Objects.isNull(definition.getAllowPerms()) || definition.getAllowPerms().isEmpty());
        }
        if (Objects.nonNull(groups)) {
            Assertions.assertEquals(groups, definition.getAllowGroups());
        } else {
            Assertions.assertTrue(Objects.isNull(definition.getAllowGroups()) || definition.getAllowGroups().isEmpty());
        }
        if (Objects.nonNull(custom)) {
            Assertions.assertEquals(custom, definition.getCustomAccessRule());
        } else {
            Assertions.assertTrue(Strings.isBlank(definition.getCustomAccessRule()));
        }
    }

}
