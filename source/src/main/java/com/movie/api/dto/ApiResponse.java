package com.movie.api.dto;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public ApiResponse(HttpStatus code, String message, T result){
        this.code = code.value();
        this.message = message;
        this.data = result;
    }

    public ApiResponse(HttpStatus code, String message){
        this.code = code.value();
        this.message = message;
    }
}
