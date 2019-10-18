package com.gemini.jalen.rxservice.adapter;

import com.gemini.jalen.rxservice.domain.Packet;

import java.lang.reflect.Type;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.Response;

final class RxJavaCallAdapter<T extends Packet> implements retrofit2.CallAdapter<T, Object> {
    private final Type responseType;
    private final boolean isBody;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;

    RxJavaCallAdapter(Type responseType, boolean isBody, boolean isFlowable,
                      boolean isSingle, boolean isMaybe) {
        this.responseType = responseType;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
    }

    @Override public Type responseType() {
        return responseType;
    }

    @Override public Object adapt(Call<T> call) {
        Observable<Response<T>> responseObservable = new ExecuteObservable<>(call);

        Observable<?> observable;
        if (isBody) {
            observable = new BodyObservable<>(responseObservable);
        } else {
            observable = responseObservable;
        }

        if (isFlowable) {
            return observable.toFlowable(BackpressureStrategy.LATEST);
        }
        if (isSingle) {
            return observable.singleOrError();
        }
        if (isMaybe) {
            return observable.singleElement();
        }
        return RxJavaPlugins.onAssembly(observable);
    }
}
