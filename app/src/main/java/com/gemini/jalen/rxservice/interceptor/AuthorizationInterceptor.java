package com.gemini.jalen.rxservice.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {
    private static Map<String, Object> header;

    public AuthorizationInterceptor(Map<String, Object> header) {
        this.header = header;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        for (Map.Entry<String, Object> entry : header.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue().toString());
        }
        return chain.proceed(builder.build());
    }

    public static void setHeader(String key, Object value) {
        header.put(key, value);
    }
}
