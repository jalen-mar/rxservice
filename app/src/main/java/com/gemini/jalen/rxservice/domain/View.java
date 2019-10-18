package com.gemini.jalen.rxservice.domain;

public interface View {
    void load();

    void loadCompleted();

    boolean isRefresh();

    void refresh(boolean refresh);
}
