package com.movie.api.form.sns;

import lombok.Data;

@Data
public class BaseSendSignalForm<T> {
    private Integer userKind;
    private Long userId;
    private T payload;
}
