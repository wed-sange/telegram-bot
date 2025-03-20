package com.sange.telegram.bot.util.http.exception;


import lombok.Getter;

public class HttpResponseException extends RuntimeException {
    @Getter
    private int code;

    public HttpResponseException(int code, String message) {
        super(message);
        this.code = code;
    }
}
