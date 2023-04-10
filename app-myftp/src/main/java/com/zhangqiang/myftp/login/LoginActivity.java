package com.zhangqiang.myftp.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zhangqiang.myftp.R;
import com.zhangqiang.myftp.account.AccountService;
import com.zhangqiang.myftp.account.bean.AccountBean;
import com.zhangqiang.myftp.base.BaseActivity;
import com.zhangqiang.myftp.databinding.ActivityLoginBinding;
import com.zhangqiang.myftp.session.SessionService;

import java.util.UUID;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountBean prepareLoginAccount = AccountService.get().getPrepareLoginAccount();
        if (prepareLoginAccount != null) {
            getVB().etIpInput.setText(prepareLoginAccount.getIp());
            getVB().etPortInput.setText(String.valueOf(prepareLoginAccount.getPort()));
            getVB().etUserNameInput.setText(prepareLoginAccount.getUserName());
            getVB().etPasswordInput.setText(prepareLoginAccount.getPassword());
        }

        getVB().btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = getVB().etIpInput.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(LoginActivity.this, "请输入ip", Toast.LENGTH_SHORT).show();
                    return;
                }
                String port = getVB().etPortInput.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(LoginActivity.this, "请输入端口", Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = getVB().etUserNameInput.getText().toString();
                String password = getVB().etPasswordInput.getText().toString();

                AccountBean accountBean;
                if (prepareLoginAccount != null) {
                    accountBean = prepareLoginAccount;
                }else {
                    accountBean = new AccountBean();
                    accountBean.setId(UUID.randomUUID().toString());
                    accountBean.setStatus(AccountBean.STATUS_PREPARE_LOGIN);
                }
                accountBean.setIp(ip);
                accountBean.setPort(Integer.parseInt(port));
                accountBean.setUserName(username);
                accountBean.setPassword(password);
                if (prepareLoginAccount != null) {
                    AccountService.get().updateAccount(accountBean);
                }else{
                    AccountService.get().insertAccount(accountBean);
                }
                SessionService.get().createSession(accountBean, new SessionService.SessionCreateListener() {
                    @Override
                    public void onSuccess() {
                        accountBean.setStatus(AccountBean.STATUS_LOGIN);
                        AccountService.get().updateAccount(accountBean);
                        AccountService.get().dispatchLogin(accountBean);
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        LoginActivity.this.finish();
                    }

                    @Override
                    public void onConnectFail() {
                        Toast.makeText(LoginActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoginFail() {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected Class<ActivityLoginBinding> getVBClass() {
        return ActivityLoginBinding.class;
    }
}
