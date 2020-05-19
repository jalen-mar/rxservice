package com.gemini.jalen.rxservice;

import android.util.Log;

import com.gemini.jalen.rxservice.domain.Packet;
import com.gemini.jalen.rxservice.domain.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public abstract class Service<T extends View> implements MaybeObserver<Packet> {
    protected T view;
    private List<Disposable> list = new ArrayList<>();

    public void attachView(T view) {
        this.view = view;
    }

    public void detachView() {
        for (Disposable obj : list) {
            if (!obj.isDisposed()) {
                obj.dispose();
            }
        }
        list.clear();
        this.view = null;
    }

    protected <X extends Packet, Y extends Maybe<X>> void subscribe(Y observable) {
        subscribe(observable, false);
    }

    protected <X extends Packet, Y extends Maybe<X>> void subscribe(Y observable, boolean isSilent) {
        Maybe.just(new MaybeWrapper(observable, isSilent, view)).flatMap((Function<MaybeWrapper, Y>) wrapper -> {
            if (!wrapper.isSilent && wrapper.view != null && !wrapper.view.isRefreshing()) {
                view.load();
            }
            return (Y) wrapper.observable;
        }).compose((MaybeTransformer<Packet, Packet>) upstream ->
                upstream.subscribeOn(Schedulers.io()).map(result -> {
                    if (isSuccess(result)) {
                        Method method;
                        try {
                            method = getClass().getMethod(result.getHandler() + "OnError", result.getClass());
                            method.invoke(this, result);
                            throw new IllegalStateException();
                        } catch (Exception e) {
                            throw new ServerException(result.getCode(), result.getMessage());
                        }
                    }
                    return result;
                }).observeOn(AndroidSchedulers.mainThread())).subscribe(this);;
    }

    @Override
    public void onComplete() {
        loadCompleted();
    }

    @Override
    public void onError(Throwable t) {
        try {
            RxJavaPlugins.getErrorHandler().accept(t);
        } catch (Exception e) {
            Log.i("message-http", e.getMessage());
        }
        loadCompleted();
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (view == null) {
            d.dispose();
        } else {
            list.add(d);
        }
    }

    @Override
    public void onSuccess(Packet result) {
        Object data = result.getData();
        try {
            Method method;
            if (data == null || data instanceof StringBuffer) {
                try {
                    method = getClass().getMethod(result.getHandler(), String.class);
                } catch (Exception e) {
                    method = getClass().getMethod("showMsg", String.class);
                }
                method.invoke(this, result.getMessage());
            } else {
                Class cls = getDataClass(data);
                method = getClass().getMethod(result.getHandler(), cls);
                method.invoke(this, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadCompleted();
    }

    private Class getDataClass(Object data) {
        Class cls;
        if (data instanceof List) {
            cls = List.class;
        } else if (data instanceof Map) {
            cls = Map.class;
        } else if (data instanceof Set) {
            cls = Set.class;
        } else {
            cls = data.getClass();
        }
        return cls;
    }

    public void showMsg(String msg) {}

    protected int getCode() {
        return -1;
    }

    protected boolean isSuccess(Packet result) {
        int code = getCode();
        if (code == -1) {
            return !result.isSuccess();
        } else {
            return code != getCode();
        }
    }

    private void loadCompleted() {
        if (view != null) {
            if (view.isRefreshing()) {
                view.refresh(false);
            } else {
                view.loadCompleted();
            }
        }
    }
}

class MaybeWrapper {
    Maybe observable;
    boolean isSilent;
    View view;

    MaybeWrapper(Maybe observable, boolean isSilent, View view) {
        this.observable = observable;
        this.isSilent = isSilent;
        this.view = view;
    }
}