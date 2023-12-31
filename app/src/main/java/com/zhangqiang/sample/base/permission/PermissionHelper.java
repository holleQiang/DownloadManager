package com.zhangqiang.sample.base.permission;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.zhangqiang.options.Options;
import com.zhangqiang.options.store.shared.SharedValueStore;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;

public abstract class PermissionHelper {


    public static class PermissionResult {
        private final int requestCode;
        private final String[] permissions;
        private final int[] grantResults;

        public PermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            this.requestCode = requestCode;
            this.permissions = permissions;
            this.grantResults = grantResults;
        }

        public int getRequestCode() {
            return requestCode;
        }

        @NonNull
        public String[] getPermissions() {
            return permissions;
        }

        @NonNull
        public int[] getGrantResults() {
            return grantResults;
        }
    }


    private final HashMap<Integer, ObservableEmitter<PermissionResult>> emitterMap = new HashMap<>();

    public abstract void requestPermissions(String[] permissions, int requestCode);


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ObservableEmitter<PermissionResult> emitter = emitterMap.get(requestCode);
        if (emitter != null) {
            emitter.onNext(new PermissionResult(requestCode, permissions, grantResults));
        }
    }


    public Observable<PermissionResult> requestPermissionsObservable(Activity activity, String permission, int requestCode) {
        return Observable.create(new ObservableOnSubscribe<PermissionResult>() {
            @Override
            public void subscribe(ObservableEmitter<PermissionResult> emitter) throws Exception {

                Context context = activity.getApplicationContext();
                if (hasPermissionRequest(context, permission)
                        && ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
                        && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    //申请过，没给权限，又不应该解释，意味着被永久拒绝授权了
                    emitter.onNext(new PermissionResult(requestCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_DENIED}));
                    return;
                }else if(!hasPermissionRequest(context,permission)){
                    setPermissionRequest(context, permission);
                }else if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                    //应该向用户解释为何需要权限
                }
                emitterMap.put(requestCode, emitter);
                requestPermissions(new String[]{permission}, requestCode);
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        emitterMap.remove(requestCode);
                    }
                });
            }
        });
    }

    public static Function<PermissionResult, Boolean> applyPermissionGrant() {
        return new Function<PermissionResult, Boolean>() {
            @Override
            public Boolean apply(PermissionResult permissionResult) throws Exception {
                int[] grantResults = permissionResult.getGrantResults();
                if (grantResults.length == 1) {
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
                } else {
                    throw new RuntimeException("cann only used with one permission request");
                }
            }
        };
    }

    private static boolean hasPermissionRequest(Context context, String permission) {
        return Options.ofBoolean(permission + "_request_record", false, new SharedValueStore(context, "permission_record")).get();
    }

    private static void setPermissionRequest(Context context, String permission) {
        Options.ofBoolean(permission + "_request_record", false, new SharedValueStore(context, "permission_record")).set(true);
    }
}
