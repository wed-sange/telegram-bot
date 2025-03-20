package com.sange.telegram.bot.util.http.exception;

import java.io.IOException;

public class HttpIOException extends RuntimeException{

    public HttpIOException(String message, IOException cause) {
        super(message, cause);
    }
}
