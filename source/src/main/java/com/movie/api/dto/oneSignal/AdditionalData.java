package com.movie.api.dto.oneSignal;

import lombok.Data;

@Data
public class AdditionalData<T> {
    private String title;
    private String content;
    private String cmd;
    private T data;
}
