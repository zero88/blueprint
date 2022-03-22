package io.zero88.qwe.sql.workflow.step;

import io.vertx.core.Future;
import io.zero88.jooqx.JsonRecord;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Represents a {@code DQL} step
 *
 * @param <T> Type of {@code JsonRecord}
 * @since 1.0.0
 */
public interface DQLStep<T extends JsonRecord> extends SQLStep {

    /**
     * Do {@code SQL Query} based on given {@code request data} and {@code validator}.
     *
     * @param reqData   the req data
     * @param validator the validator
     * @return result in Single
     * @see RequestData
     * @see OperationValidator
     * @since 1.0.0
     */
    Future<T> query(@NonNull RequestData reqData, @NonNull OperationValidator validator);

}