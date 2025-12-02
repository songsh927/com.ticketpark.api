package com.ticketpark.ticketpark.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DefaultRes<T> {
    private boolean success;
    private String msg;
    private T data;

    public DefaultRes(final boolean success, final String responseMessage) {
        this.success = success;
        this.msg = responseMessage;
        this.data = null;
    }

    public static<T> DefaultRes<T> res(final boolean success, final String responseMessage) {
        return res(success, responseMessage, null);
    }

    public static<T> DefaultRes<T> res(final boolean success, final String responseMessage, final T t) {
        return DefaultRes.<T>builder()
                .data(t)
                .success(success)
                .msg(responseMessage)
                .build();
    }
}
