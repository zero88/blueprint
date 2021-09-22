package io.zero88.qwe.file;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.zero88.qwe.JsonHelper;

class FileOptionTest {

    @Test
    void serialize() {
        final FileOption opt = FileOption.create();
        Assertions.assertTrue(opt.isAutoCreate());
        Assertions.assertTrue(opt.isOverwrite());
        JsonHelper.assertJson(FileOption.builder()
                                        .autoCreate(true)
                                        .filePerms("rw-r--r--")
                                        .folderPerms("rwxr-xr-x")
                                        .owner(System.getProperty("user.name"))
                                        .build()
                                        .toJson(), opt.toJson());
    }

    @Test
    void customize() {
        final FileOption opt = FileOption.builder().autoCreate(false).owner("pi").build();
        Assertions.assertFalse(opt.isAutoCreate());
        Assertions.assertEquals("pi", opt.getOwner());
    }

}
