package com.zhangqiang.myftp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zhangqiang.myftp.account.AccountService;
import com.zhangqiang.myftp.account.bean.AccountBean;
import com.zhangqiang.myftp.base.BaseActivity;
import com.zhangqiang.myftp.databinding.ActivityMainBinding;
import com.zhangqiang.myftp.login.LoginActivity;
import com.zhangqiang.myftp.session.SessionService;

public class MainActivity extends BaseActivity<ActivityMainBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountBean lastLoginAccount = AccountService.get().getLastLoginAccount();
        if (lastLoginAccount == null) {
            getVB().btLogin.setVisibility(View.VISIBLE);
            getVB().btLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(v.getContext(), LoginActivity.class));
                }
            });
        } else {
            getVB().btLogin.setVisibility(View.GONE);
            SessionService.get().createSessionIfNeed(lastLoginAccount, new SessionService.SessionCreateListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onConnectFail() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onLoginFail() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        AccountService.get().addListener(accountListener);
    }

    @Override
    protected Class<ActivityMainBinding> getVBClass() {
        return ActivityMainBinding.class;
    }

    final AccountService.Listener accountListener = new AccountService.Listener() {
        @Override
        public void onLogin(AccountBean accountBean) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getVB().btLogin.setVisibility(View.GONE);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountService.get().removeListener(accountListener);
    }
}