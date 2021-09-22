package io.zero88.qwe.exceptions;

public final class CryptoException extends QWEException {

    public static final ErrorCode CODE = ErrorCode.parse("CRYPTO_ERROR");

    public CryptoException(String message, Throwable e) {
        super(CODE, message, e);
    }

    public CryptoException(String message) {
        this(message, null);
    }

    public CryptoException(Throwable e) {
        this(null, e);
    }

}
