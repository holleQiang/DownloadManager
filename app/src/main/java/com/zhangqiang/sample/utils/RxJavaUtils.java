package com.zhangqiang.sample.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

public class RxJavaUtils {

    public static <T> ObservableTransformer<T, T> bindLifecycle(final LifecycleOwner lifecycleOwner) {

        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {

                final Lifecycle lifecycle = lifecycleOwner.getLifecycle();
                final BehaviorSubject<Boolean> abortSubject = BehaviorSubject.createDefault(lifecycle.getCurrentState() == Lifecycle.State.DESTROYED);
                final FullLifecycleObserver observer = new FullLifecycleObserver() {
                    @Override
                    public void onCreate(@NonNull LifecycleOwner owner) {
                        super.onCreate(owner);
                        abortSubject.onNext(false);
                    }

                    @Override
                    public void onDestroy(@NonNull LifecycleOwner owner) {
                        super.onDestroy(owner);
                        abortSubject.onNext(true);
                    }
                };
                return upstream
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                lifecycle.addObserver(observer);
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                lifecycle.removeObserver(observer);
                            }
                        })
                        .takeUntil(abortSubject.filter(new Predicate<Boolean>() {
                            @Override
                            public boolean test(Boolean aBoolean) throws Exception {
                                return aBoolean;
                            }
                        }));
            }
        };
    }

    private static class FullLifecycleObserver implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onCreate(@NonNull LifecycleOwner owner) {
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onStart(@NonNull LifecycleOwner owner) {

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onResume(@NonNull LifecycleOwner owner) {

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause(@NonNull LifecycleOwner owner) {

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop(@NonNull LifecycleOwner owner) {

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(@NonNull LifecycleOwner owner) {
        }
    }
}
