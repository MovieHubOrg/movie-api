package com.movie.api.form.mqtt;

import lombok.Data;

@Data
public class BaseSendMsgForm<T> {
    private String cmd;
    private T data;
}
