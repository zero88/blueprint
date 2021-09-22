package io.zero88.qwe.exceptions;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.exceptions.ErrorCode;
import io.github.zero88.exceptions.ErrorCodeException;
import io.github.zero88.exceptions.HiddenException;
import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Strings;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Convert any {@code throwable} to friendly {@code QWEException}. The converter result will be showed directly to end
 * user, any technical information will be logged.
 *
 * @see QWEException
 */
@AllArgsConstructor
public class QWEExceptionConverter implements Function<Throwable, QWEException> {

    private static final Logger logger = LoggerFactory.getLogger(QWEExceptionConverter.class);
    private final boolean friendly;
    private final String overrideMsg;

    /**
     * Convert throwable to friendly error that human user can understand.
     * <p>
     * If {@code throwable} is instance of {@code QWEException} then keep it as it is.
     *
     * @param throwable any exception
     * @return QWE exception
     */
    public static QWEException friendlyOrKeep(Throwable throwable) {
        if (throwable instanceof QWEException) {
            return (QWEException) throwable;
        }
        return new QWEExceptionConverter(true, null).apply(throwable);
    }

    /**
     * Convert throwable to friendly error that human user can understand
     *
     * @param throwable any exception
     * @return QWE exception
     */
    public static QWEException friendly(Throwable throwable) {
        return new QWEExceptionConverter(true, null).apply(throwable);
    }

    /**
     * Convert throwable to friendly error that human user can understand
     *
     * @param throwable   any exception
     * @param overrideMsg Override message
     * @return QWE exception
     */
    public static QWEException friendly(Throwable throwable, String overrideMsg) {
        return new QWEExceptionConverter(true, overrideMsg).apply(throwable);
    }

    /**
     * Convert throwable for system process
     *
     * @param throwable any exception
     * @return QWE exception
     */
    public static QWEException from(Throwable throwable) {
        return new QWEExceptionConverter(false, null).apply(throwable);
    }

    @Override
    public QWEException apply(@NonNull Throwable throwable) {
        Throwable t = throwable;
        final Class<Object> rxCompositeEx = ReflectionClass.findClass("io.reactivex.exceptions.CompositeException");
        if (Objects.nonNull(rxCompositeEx) && ReflectionClass.assertDataType(t.getClass(), rxCompositeEx)) {
            List<Throwable> exceptions = ((io.reactivex.exceptions.CompositeException) throwable).getExceptions();
            t = exceptions.get(exceptions.size() - 1);
        }
        if (t instanceof IllegalArgumentException || t instanceof NullPointerException) {
            return new QWEException(ErrorCode.INVALID_ARGUMENT, t.getMessage(), t.getCause());
        }
        if (t instanceof ErrorCodeException) {
            final ErrorCodeException e = (ErrorCodeException) t;
            if (ErrorCode.REFLECTION_ERROR.equals(e.errorCode())) {
                return apply(t.getCause());
            }
            return overrideMsg(friendly ? convert(e, t instanceof QWEException) : e);
        }
        if (t.getCause() instanceof QWEException) {
            if (logger.isDebugEnabled()) {
                logger.debug("Wrapper Exception: ", t);
            }
            final QWEException c = (QWEException) t.getCause();
            return overrideMsg(friendly ? convert(c, false) : c);
        }
        return convert(new QWEException(ErrorCode.UNKNOWN_ERROR, overrideMsg, t), false);
    }

    private QWEException overrideMsg(ErrorCodeException t) {
        if (t instanceof QWEException && Strings.isBlank(overrideMsg)) {
            return (QWEException) t;
        }
        return new QWEException(t.errorCode(), Strings.fallback(overrideMsg, t.getMessage()), t.getCause());
    }

    private QWEException convert(ErrorCodeException throwable, boolean wrapperIsQWE) {
        final Throwable cause = throwable.getCause();
        final ErrorCode code = throwable.errorCode();
        if (Objects.isNull(cause)) {
            if (throwable instanceof QWEException) {
                return (QWEException) throwable;
            }
            return new QWEException(code, throwable.getMessage());
        }
        final String msgOrCode = originMessage(code, throwable.getMessage());
        if (cause instanceof IllegalArgumentException || cause instanceof NullPointerException) {
            return new QWEException(code,
                                    includeCauseMessage(msgOrCode, ErrorCode.INVALID_ARGUMENT, cause.getMessage()),
                                    cause.getCause());
        }
        if (cause instanceof ErrorCodeException) {
            if (!wrapperIsQWE && cause instanceof QWEException) {
                return (QWEException) cause;
            }
            return new QWEException(code, includeCauseMessage((ErrorCodeException) cause, msgOrCode), cause);
        }
        return new QWEException(code, includeCauseMessage(cause, msgOrCode), cause);
    }

    private String originMessage(ErrorCode code, String message) {
        return Strings.isBlank(message) ? code.code() : message;
    }

    private String includeCauseMessage(@NonNull Throwable cause, @NonNull String message) {
        if (Strings.isBlank(cause.getMessage())) {
            return message;
        }
        String mc = cause.getMessage().equals("null") ? cause.toString() : cause.getMessage();
        return Strings.format("{0} | Cause({1})", message, mc);
    }

    private String includeCauseMessage(@NonNull ErrorCodeException cause, @NonNull String message) {
        return cause instanceof HiddenException
               ? message
               : includeCauseMessage(message, cause.errorCode(), cause.getMessage());
    }

    private String includeCauseMessage(String message, ErrorCode causeCode, String causeMsg) {
        return ErrorCode.HIDDEN.code().equals(causeCode.code())
               ? message
               : Strings.format("{0} | Cause({1}) - Code({2})", message, Strings.fallback(causeMsg, ""),
                                causeCode.code());
    }

}
