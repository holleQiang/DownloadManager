package com.zhangqiang.myftp.account;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.myftp.account.bean.AccountBean;
import com.zhangqiang.myftp.base.listeners.AbstractListeners;
import com.zhangqiang.myftp.db.DBService;
import com.zhangqiang.myftp.db.dao.AccountEntityDao;
import com.zhangqiang.myftp.db.entity.AccountEntity;

import java.util.List;

public class AccountService {

    private static volatile AccountService accountService;
    private final DBService dbService;
    private final Listeners listeners = new Listeners();

    public static synchronized void init(Context context){
        if (accountService == null) {
            accountService = new AccountService(context);
        }
    }

    public static AccountService get(){
        if (accountService == null) {
            throw new IllegalStateException("please call init method first");
        }
        return accountService;
    }

    public AccountService(Context context) {
        dbService = new DBService(context.getApplicationContext());
    }

    public AccountBean getLastLoginAccount(){
        return getAccountBeanByStatus(AccountBean.STATUS_LOGIN);
    }

    @Nullable
    private AccountBean getAccountBeanByStatus(int status) {
        List<AccountEntity> list = dbService.getDaoSession().getAccountEntityDao().queryBuilder()
                .where(AccountEntityDao.Properties.Status.eq(status))
                .list();
        if (list != null && !list.isEmpty()) {
            return toAccountBean(list.get(0));
        }
        return null;
    }

    @NonNull
    private AccountBean toAccountBean(AccountEntity accountEntity) {
        AccountBean accountBean = new AccountBean();
        accountBean.setId(accountEntity.getId());
        accountBean.setIp(accountEntity.getIp());
        accountBean.setPort(accountEntity.getPort());
        accountBean.setUserName(accountEntity.getUserName());
        accountBean.setPassword(accountEntity.getPassword());
        accountBean.setStatus(accountEntity.getStatus());
        return accountBean;
    }

    public AccountBean getPrepareLoginAccount(){
        return getAccountBeanByStatus(AccountBean.STATUS_PREPARE_LOGIN);
    }

    public void insertAccount(AccountBean accountBean){
        AccountEntity accountEntity = toAccountEntity(accountBean);
        dbService.getDaoSession().getAccountEntityDao().insert(accountEntity);
    }

    @NonNull
    private AccountEntity toAccountEntity(AccountBean accountBean) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountBean.getId());
        accountEntity.setIp(accountBean.getIp());
        accountEntity.setPort(accountBean.getPort());
        accountEntity.setUserName(accountBean.getUserName());
        accountEntity.setPassword(accountBean.getPassword());
        accountEntity.setStatus(accountBean.getStatus());
        return accountEntity;
    }

    public void updateAccount(AccountBean accountBean){
        AccountEntity accountEntity = toAccountEntity(accountBean);
        dbService.getDaoSession().getAccountEntityDao().update(accountEntity);
    }

    public void addListener(Listener listener){
        listeners.addListener(listener);
    }

    public void removeListener(Listener listener){
        listeners.removeListener(listener);
    }

    public void dispatchLogin(AccountBean accountBean){
        listeners.notifyLogin(accountBean);
    }

    public interface Listener{
        void onLogin(AccountBean accountBean);
    }

    private static class Listeners extends AbstractListeners<Listener> {
        synchronized void notifyLogin(AccountBean accountBean){
            for (int i = listenerList.size() - 1; i >= 0; i--) {
                listenerList.get(i).onLogin(accountBean);
            }
        }
    }
}
