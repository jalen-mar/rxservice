package com.gemini.jalen.rxservice.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gemini.jalen.rxservice.domain.JsonBean;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

public final class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
    public static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");

    public static FastJsonRequestBodyConverter create() {
        return new FastJsonRequestBodyConverter();
    }

    private FastJsonRequestBodyConverter() {}

    @Override
    public RequestBody convert(T value) throws IOException {
        RequestBody body;
        if (value instanceof String) {
            body = RequestBody.create(TYPE_TEXT, value.toString());
        } else {
            JSONObject json = (JSONObject) JSON.toJSON(value);
            if (value instanceof JsonBean) {
                body = RequestBody.create(TYPE_JSON, json.toJSONString());
            } else {
                FormBody.Builder builder = new FormBody.Builder();
                for (String key : json.keySet()) {
                    Object param = json.get(key);
                    if (param != null) {
                        builder.add(key, param.toString());
                    }
                }
                body = builder.build();
            }
        }
        return body;
    }
}
