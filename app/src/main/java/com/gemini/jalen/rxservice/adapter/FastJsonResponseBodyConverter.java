package com.gemini.jalen.rxservice.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gemini.jalen.rxservice.ServerException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private Type type;
    public static FastJsonResponseBodyConverter create(Type type) {
        return new FastJsonResponseBodyConverter(type);
    }

    private FastJsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) {
        T result = null;
        try {
            BufferedSource bufferedSource = Okio.buffer(value.source());
            String tempStr = bufferedSource.readUtf8();
            bufferedSource.close();
            result = parseObject(tempStr, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null) {
            throw new ServerException(405, "服务器异常,程序猿正在紧急修复中!");
        }
        return result;
    }

    //以防脑残的后台接口数据
    private <T> T parseObject(String value, Type type) {
        JSONObject json = JSON.parseObject(value);
        String targetData = json.getString("data");
        if (targetData == null || targetData.trim().length() == 0) {
            json.remove("data");
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            if (arguments[0] instanceof ParameterizedType) {
                ParameterizedType argument = ((ParameterizedType) arguments[0]);
                if (List.class.isAssignableFrom((Class<?>) argument.getRawType())) {
                    json.put("data", new ArrayList());
                } else {
                    json.put("data", new HashMap());
                }
            } else {
                if (List.class.isAssignableFrom((Class<?>) arguments[0])) {
                    json.put("data", new ArrayList());
                } else {
                    json.put("data", new HashMap());
                }
            }

        }
        return json.toJavaObject(type);
    }
}
