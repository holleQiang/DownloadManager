package com.zhangqiang.common.utils;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.annotation.NonNull;

import com.zhangqiang.common.dialog.loading.LoadingDialogHolderOwner;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class RXJavaUtils {

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
                                ThreadUtils.doOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lifecycle.addObserver(observer);
                                    }
                                });
                            }
                        })
                        .doOnDispose(new Action() {
                            @Override
                            public void run() throws Exception {
                                ThreadUtils.doOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lifecycle.removeObserver(observer);
                                    }
                                });
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ThreadUtils.doOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lifecycle.removeObserver(observer);
                                    }
                                });
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                ThreadUtils.doOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lifecycle.removeObserver(observer);
                                    }
                                });
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

    public static <T> ObservableTransformer<T,T> applyIOMainSchedules(){
        return new ObservableTransformer<T,T>(){

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> ObservableTransformer<T,T> withLoadingDialog(LoadingDialogHolderOwner owner){
        return new ObservableTransformer<T,T>(){

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        ThreadUtils.doOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                owner.getLoadingDialogHolder().showLoading();
                            }
                        });
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        ThreadUtils.doOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                owner.getLoadingDialogHolder().hideLoading();
                                Log.i("Test","=======hideLoading==========");
                            }
                        });
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        ThreadUtils.doOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                owner.getLoadingDialogHolder().hideLoading();
                                Log.i("Test","=======hideLoading==========");
                            }
                        });
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        ThreadUtils.doOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                owner.getLoadingDialogHolder().hideLoading();
                                Log.i("Test","=======hideLoading==========");
                            }
                        });
                    }
                });
            }
        };
    }
}
