package com.gemini.jalen.rxservice.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {
    private AuthorizationInfoProvider provider;

    public AuthorizationInterceptor(AuthorizationInfoProvider provider) {
        this.provider = provider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        Map<String, Object> headers = provider.getHeaders();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue().toString());
        }
        return chain.proceed(builder.build());
    }
}
