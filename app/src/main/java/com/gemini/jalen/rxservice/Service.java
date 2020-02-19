package com.gemini.jalen.rxservice;

import android.util.Log;

import com.gemini.jalen.rxservice.domain.Result;
import com.gemini.jalen.rxservice.domain.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public abstract class Service<T extends View> implements MaybeObserver<Result> {
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

    protected <X> void subscribe(Maybe<Result<X>> observable) {
        loading();
        observable.compose((MaybeTransformer<Result, Result>) upstream ->
                upstream.subscribeOn(Schedulers.io()).map(result -> {
                    if (isSuccess(result)) {
                        Method method;
                        try {
                            method = getClass().getMethod(result.getHandler() + "OnError", Result.class);
                            method.invoke(this, result);
                        } catch (Exception e) {
                            throw new ServerException(result.getCode(), result.getMessage());
                        }
                    }
                    return result;
                }).observeOn(AndroidSchedulers.mainThread())).subscribe(this);
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
    public void onSuccess(Result result) {
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
                method = getClass().getMethod(result.getHandler(), data.getClass());
                method.invoke(this, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadCompleted();
    }

    public void showMsg(String msg) {}

    protected int getCode() {
        return -1;
    }

    protected boolean isSuccess(Result result) {
        int code = getCode();
        if (code == -1) {
            return !result.isSuccess();
        } else {
            return code != getCode();
        }
    }

    private void loadCompleted() {
        if (view != null) {
            if (view.isRefresh()) {
                view.refresh(false);
            } else {
                view.loadCompleted();
            }
        }
    }

    private void loading() {
        if (view != null) {
            if (!view.isRefresh()) {
                view.load();
            }
        }
    }
}
