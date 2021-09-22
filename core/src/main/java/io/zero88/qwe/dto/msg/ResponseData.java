package io.zero88.qwe.dto.msg;

import java.util.Objects;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

//TODO refactor it
@NoArgsConstructor
public final class ResponseData extends AbstractDTO<ResponseData> {

    @Getter
    @Setter
    @Accessors(chain = true)
    @JsonIgnore
    private HttpResponseStatus status = HttpResponseStatus.OK;

    public ResponseData(JsonObject headers, JsonObject body) {
        super(headers, body);
    }

    @JsonIgnore
    public ResponseData setStatusCode(int status) {
        this.status = HttpResponseStatus.valueOf(status);
        return this;
    }

    @JsonIgnore
    public boolean isError() {
        return Objects.nonNull(this.status) && this.status.code() >= 400;
    }

}
