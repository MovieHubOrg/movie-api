package com.movie.api.form.sns;

import lombok.Data;

@Data
public class BaseSendSignalPayloadForm<T> {
    private String cmd;
    private String subCmd;
    private T data;
}
