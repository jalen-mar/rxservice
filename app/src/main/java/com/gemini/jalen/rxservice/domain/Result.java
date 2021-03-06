package com.gemini.jalen.rxservice.domain;

public class Result<T> implements Packet<T> {
    private T data;
    private int code;
    private boolean success;
    private String message;
    private String handler;
    private Object historicData;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        if (message == null)
            message = "请求已完成!";
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getHandler() {
        return handler;
    }

    @Override
    public void setHandler(String handler) {
        this.handler = handler;
    }

    public <X> X getHistoricData() {
        return null;
    }

    public void setHistoricData(Object historicData) {
        this.historicData = historicData;
    }
}
