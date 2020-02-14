package com.gemini.jalen.rxservice.interceptor;

import java.util.Map;

import io.reactivex.annotations.NonNull;

public interface AuthorizationInfoProvider {
    @NonNull
    Map<String, Object> getHeaders();
}
