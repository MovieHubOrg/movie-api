package com.movie.api.dto.oneSignal;

import lombok.Data;

@Data
public class AdditionalData<T> {
    private T data;
}
