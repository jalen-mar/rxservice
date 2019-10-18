package com.gemini.jalen.rxservice.adapter;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
            result = JSON.parseObject(tempStr, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (result == null) {
                Class<T> cls = (Class<T>) ((ParameterizedType) type).getRawType();
                Constructor<T> constructor = cls.getConstructor(new Class[]{Integer.class, String.class});
                result = constructor.newInstance(-191519, "服务器异常,程序猿正在紧急修复中!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
