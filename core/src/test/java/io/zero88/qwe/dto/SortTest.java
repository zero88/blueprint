package io.zero88.qwe.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.dto.jpa.Sort;

public class SortTest {

    @Test
    public void test_encode_decode() {
        final Sort sort = Sort.from("abc,-def,+xyz,g1.abc,g2.def,-g2.xy");
        Assertions.assertNotNull(sort);
        final JsonObject expected = sort.toJson();
        System.out.println(expected);
        final Sort convert = JsonData.convert(expected, Sort.class);
        JsonHelper.assertJson(expected, convert.toJson());
    }

    @Test
    public void test_alternative_decode() {
        final Sort sort = Sort.from("abc,-def,+xyz,g1.abc,g2.def,-g2.xy");
        Assertions.assertNotNull(sort);
        System.out.println(sort.toJson());
        final Sort convert = JsonData.convert(new JsonObject(
            "{\"g1.abc\":\"\",\"abc\":null,\"def\":\"-\",\"g2.xy\":\"-\",\"g2.def\":\"+\",\"xyz\":\"+\"}"), Sort.class);
        JsonHelper.assertJson(sort.toJson(), convert.toJson());
    }

}
