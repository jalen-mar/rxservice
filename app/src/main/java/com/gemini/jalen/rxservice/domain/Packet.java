package com.gemini.jalen.rxservice.domain;

public interface Packet<T> {
    int getCode();
    String getMessage();
    T getData();
    String getHandler();
    boolean isSuccess();
    void setHandler(String handler);
}
