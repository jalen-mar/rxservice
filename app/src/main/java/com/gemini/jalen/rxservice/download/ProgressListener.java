package com.gemini.jalen.rxservice.download;

public interface ProgressListener {
    void update(long currentSize, long countSize, boolean finish);
    void onFailed();
    void onStart();
}
