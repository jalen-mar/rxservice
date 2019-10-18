package com.gemini.jalen.rxservice;

public class ServerException extends RuntimeException {
    private int code;

    public ServerException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }
}
