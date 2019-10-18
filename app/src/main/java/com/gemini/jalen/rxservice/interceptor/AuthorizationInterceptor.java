package com.gemini.jalen.rxservice.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {
    private static String token;
    private String source;

    public AuthorizationInterceptor(String source) {
        this.source = source;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        builder.addHeader("source", source);
        if (token != null && token.length() != 0) {
            builder.addHeader("Authorization", token);
        }
        return chain.proceed(builder.build());
    }

    public static void setToken(String token) {
        AuthorizationInterceptor.token = token;
    }
}
