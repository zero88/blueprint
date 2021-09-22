package io.zero88.qwe.exceptions;

import io.github.zero88.exceptions.ErrorCodeException;
import io.github.zero88.exceptions.RuntimeErrorCodeException;

public class QWEException extends RuntimeErrorCodeException implements ErrorCodeException {

    public QWEException(io.github.zero88.exceptions.ErrorCode errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }

    public QWEException(io.github.zero88.exceptions.ErrorCode errorCode, String message) {super(errorCode, message);}

    public QWEException(io.github.zero88.exceptions.ErrorCode errorCode, Throwable e)    {super(errorCode, e);}

    public QWEException(io.github.zero88.exceptions.ErrorCode errorCode)                 {super(errorCode);}

    public QWEException(ErrorCode errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }

    public QWEException(ErrorCode errorCode, String message) {super(errorCode, message);}

    public QWEException(ErrorCode errorCode, Throwable e)    {super(errorCode, e);}

    public QWEException(String message, Throwable e)         {super(message, e);}

    public QWEException(String message)                      {super(message);}

    public QWEException(Throwable e)                         {super(e);}

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.wrap(super.errorCode());
    }

}
