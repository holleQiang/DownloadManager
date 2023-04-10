package com.zhangqiang.myftp.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    private VB vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            vb = (VB) getVBClass().getMethod("inflate", LayoutInflater.class)
                    .invoke(this, getLayoutInflater());
            setContentView(vb.getRoot());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected abstract Class<VB> getVBClass();

    public VB getVB() {
        return vb;
    }
}
