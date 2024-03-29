package com.zhangqiang.myftp.db.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.zhangqiang.myftp.db.entity.AccountEntity;

import com.zhangqiang.myftp.db.dao.AccountEntityDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig accountEntityDaoConfig;

    private final AccountEntityDao accountEntityDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        accountEntityDaoConfig = daoConfigMap.get(AccountEntityDao.class).clone();
        accountEntityDaoConfig.initIdentityScope(type);

        accountEntityDao = new AccountEntityDao(accountEntityDaoConfig, this);

        registerDao(AccountEntity.class, accountEntityDao);
    }
    
    public void clear() {
        accountEntityDaoConfig.clearIdentityScope();
    }

    public AccountEntityDao getAccountEntityDao() {
        return accountEntityDao;
    }

}
