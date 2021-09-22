package io.zero88.qwe.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.zero88.exceptions.HiddenException;

public class QWEExceptionConverterTest {

    @Test
    public void test_null() {
        Assertions.assertThrows(NullPointerException.class, () -> QWEExceptionConverter.friendly(null));
    }

    @Test
    public void test_only_code() {
        QWEException t = QWEExceptionConverter.friendly(new QWEException(ErrorCode.SERVICE_ERROR));
        Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
        Assertions.assertNull(t.getMessage());
        Assertions.assertNull(t.getCause());
    }

    @Test
    public void test_only_message() {
        QWEException t = QWEExceptionConverter.friendly(new QWEException("2"));
        Assertions.assertEquals(ErrorCode.UNKNOWN_ERROR, t.errorCode());
        Assertions.assertEquals("2", t.getMessage());
        Assertions.assertNull(t.getCause());
    }

    @Test
    public void test_only_throwable_has_message() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(new QWEException(new IllegalStateException("lorem")));
            Assertions.assertEquals(ErrorCode.UNKNOWN_ERROR, t.errorCode());
            Assertions.assertEquals("UNKNOWN_ERROR | Cause(lorem)", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_only_throwable_no_message() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(new QWEException(new IllegalStateException()));
            Assertions.assertEquals(io.github.zero88.exceptions.ErrorCode.UNKNOWN_ERROR, t.errorCode());
            Assertions.assertEquals("UNKNOWN_ERROR", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_with_code_without_cause() {
        QWEException t = QWEExceptionConverter.friendly(new QWEException(ErrorCode.SERVICE_ERROR, "1"));
        Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
        Assertions.assertEquals("1", t.getMessage());
        Assertions.assertNull(t.getCause());
    }

    @Test
    public void test_with_code_with_other_cause_no_message() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(
                new QWEException(ErrorCode.SERVICE_ERROR, "1", new RuntimeException()));
            Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
            Assertions.assertEquals("1", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_with_code_with_other_cause_has_message() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(
                new QWEException(ErrorCode.DATA_NOT_FOUND, "abc", new RuntimeException("xyz")));
            Assertions.assertEquals(ErrorCode.DATA_NOT_FOUND, t.errorCode());
            Assertions.assertEquals("abc | Cause(xyz)", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_with_code_with_qwe_cause() {
        Assertions.assertThrows(QWEException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(new SecurityException("abc", new EngineException("xyz")));
            Assertions.assertEquals(ErrorCode.SECURITY_ERROR, t.errorCode());
            Assertions.assertEquals("abc | Cause(xyz) - Code(ENGINE_ERROR)", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_with_code_with_hidden_cause() {
        Assertions.assertThrows(HiddenException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(
                new ServiceException("abc", new HiddenException(ErrorCode.CONFLICT_ERROR, "xyz", null)));
            Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
            Assertions.assertEquals("abc", t.getMessage());
            HiddenException cause = (HiddenException) t.getCause();
            Assertions.assertEquals(ErrorCode.CONFLICT_ERROR, cause.errorCode());
            Assertions.assertEquals("xyz", cause.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_other_exception_no_message() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(new IllegalStateException());
            Assertions.assertEquals(ErrorCode.UNKNOWN_ERROR, t.errorCode());
            Assertions.assertEquals("UNKNOWN_ERROR", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_invalid_argument_exception_no_message() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(
                new IllegalArgumentException("xx", new IllegalStateException("abc")));
            Assertions.assertEquals(ErrorCode.INVALID_ARGUMENT, t.errorCode());
            Assertions.assertEquals("xx", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_qwe_exception_wrap_illegal_argument_exception() {
        QWEException t = QWEExceptionConverter.friendly(new ServiceException("xx", new IllegalArgumentException("a")));
        Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
        Assertions.assertEquals("xx | Cause(a) - Code(INVALID_ARGUMENT)", t.getMessage());
        Assertions.assertNull(t.getCause());
    }

    @Test
    public void test_other_exception_has_message() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            QWEException t = QWEExceptionConverter.friendly(new RuntimeException("xyz"));
            Assertions.assertEquals(ErrorCode.UNKNOWN_ERROR, t.errorCode());
            Assertions.assertEquals("UNKNOWN_ERROR | Cause(xyz)", t.getMessage());
            throw t.getCause();
        });
    }

    @Test
    public void test_other_exception_bound_qwe_exception() {
        QWEException t = QWEExceptionConverter.friendly(new RuntimeException(new ServiceException("hey")));
        Assertions.assertEquals(ErrorCode.SERVICE_ERROR, t.errorCode());
        Assertions.assertEquals("hey", t.getMessage());
        Assertions.assertNull(t.getCause());
    }

}
