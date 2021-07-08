/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import java.util.Set;

public abstract class BaseCamera {

    private final Callback mCallback;
    private PictureTakenCallback pictureTakenCallback;
    private final BasePreviewView previewView;

    BaseCamera(Callback callback, BasePreviewView previewView) {
        mCallback = callback;
        this.previewView = previewView;
    }

    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    public abstract boolean start();

    public abstract void stop();

    public abstract boolean isCameraOpened();

    public abstract void setFacing(int facing);

    public abstract int getFacing();

    public abstract Set<AspectRatio> getSupportedAspectRatios();

    /**
     * @return {@code true} if the aspect ratio was changed.
     */
    public abstract boolean setAspectRatio(AspectRatio ratio);

    public abstract AspectRatio getAspectRatio();

    public abstract void setAutoFocus(boolean autoFocus);

    public abstract boolean getAutoFocus();

    public abstract void setFlash(int flash);

    public abstract int getFlash();

    public abstract void setDisplayOrientation(int displayOrientation);

    public final void takePicture(PictureTakenCallback callback) {
        if (callback != null) {
            pictureTakenCallback = callback;
            performTakePicture();
        }
    }

    protected abstract void performTakePicture();

    interface Callback {

        void onCameraOpened();

        void onCameraClosed();
    }

    protected void dispatchCameraOpened() {
        if (mCallback != null) {
            mCallback.onCameraOpened();
        }
    }

    protected void dispatchCameraClosed() {
        if (mCallback != null) {
            mCallback.onCameraClosed();
        }
    }

    protected void dispatchPictureTaken(byte[] data, int width, int height) {
        if (pictureTakenCallback != null) {
            pictureTakenCallback.onPictureTaken(data, width, height);
        }
    }


    public BasePreviewView getPreviewView() {
        return previewView;
    }
}
