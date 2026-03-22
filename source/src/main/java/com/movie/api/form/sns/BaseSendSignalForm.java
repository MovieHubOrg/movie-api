package com.movie.api.form.sns;

import lombok.Data;

@Data
public class BaseSendSignalForm<T> {
    private String app;
    private Long userId;
    private T payload;
}
