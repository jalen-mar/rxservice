package com.gemini.jalen.rxservice.domain;

public interface View {
    void load();

    void loadCompleted();

    boolean isRefreshing();

    void refresh(boolean refresh);
}
