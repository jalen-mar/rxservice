package com.gemini.jalen.rxservice.domain;

public interface Packet<T> {
    int getCode();
    String getMessage();
    T getData();
    String getHandler();
    void setHandler(String handler);
}
