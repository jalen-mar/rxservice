package com.gemini.jalen.rxservice.adapter;

import com.gemini.jalen.rxservice.domain.Packet;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

final class ExecuteObservable<T extends Packet> extends Observable<Response<T>> {
    private final Call<T> originalCall;

    ExecuteObservable(Call<T> originalCall) {
        this.originalCall = originalCall;
    }

    @Override protected void subscribeActual(Observer<? super Response<T>> observer) {
        Request request = originalCall.request();
        String handlerName = request.header("handler");
        boolean cached = Boolean.parseBoolean(request.header("cached"));
        Call<T> call = originalCall.clone();
        CallDisposable disposable = new CallDisposable(call);
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            Response<T> response = call.execute();
            if (!disposable.isDisposed()) {
                response.body().setHandler(handlerName);
                observer.onNext(response);
                if (!disposable.isDisposed()) {
                    terminated = true;
                    observer.onComplete();
                }
            }
        } catch (Throwable t) {
            if (cached) {
                if (!disposable.isDisposed()) {
                    observer.onComplete();
                }
            } else {
                Exceptions.throwIfFatal(t);
                if (terminated) {
                    RxJavaPlugins.onError(t);
                } else if (!disposable.isDisposed()) {
                    try {
                        observer.onError(t);
                    } catch (Throwable inner) {
                        Exceptions.throwIfFatal(inner);
                        RxJavaPlugins.onError(new CompositeException(t, inner));
                    }
                }
            }
        }
    }

    private static final class CallDisposable implements Disposable {
        private final Call<?> call;
        private volatile boolean disposed;

        CallDisposable(Call<?> call) {
            this.call = call;
        }

        @Override public void dispose() {
            disposed = true;
            call.cancel();
        }

        @Override public boolean isDisposed() {
            return disposed;
        }
    }
}

