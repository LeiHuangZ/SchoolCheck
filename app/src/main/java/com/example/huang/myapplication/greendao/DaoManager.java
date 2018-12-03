package com.example.huang.myapplication.greendao;

import android.content.Context;


import com.zsy.words.bean.DaoMaster;
import com.zsy.words.bean.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

import java.lang.ref.WeakReference;

/**
 * 创建数据库、创建数据库表、包含增删改查的操作以及数据库的升级
 * Created by huang on 2017/10/18.
 */

public class DaoManager {
    private static final String DB_NAME = "Evidence";

    /**
     * 多线程中要共享的变量用volatile关键字修饰
     */
    private static DaoManager sManager;
    private DaoMaster sDaoMaster;
    private DaoMaster.DevOpenHelper sHelper;
    private DaoSession sDaoSession;

    /**
     * 单利模式获取操作数据库对象
     */
    public static DaoManager getInstance(Context context) {
        WeakReference<Context> weakReference = new WeakReference<>(context);
        if (sManager == null) {
            synchronized (PersonDaoManager.class) {
                if (sManager == null) {
                    sManager = new DaoManager(weakReference.get());
                }
            }
        }
        return sManager;
    }

    private DaoManager(Context context) {
        sHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
    }

    /**
     * 判断是否有存在数据库，如果没有则创建
     *
     * @return sDaoMaster
     */
    private DaoMaster getDaoMaster() {
        if (sDaoMaster == null) {
            sDaoMaster = new DaoMaster(sHelper.getWritableDatabase());
        }
        return sDaoMaster;
    }

    /**
     * 完成对数据库的添加、删除、修改、查询操作，仅仅是一个接口
     *
     * @return sDaoSession
     */
    public DaoSession getDaoSession() {
        if (sDaoSession == null) {
            if (sDaoMaster == null) {
                sDaoMaster = getDaoMaster();
            }
            sDaoSession = sDaoMaster.newSession();
        }
        return sDaoSession;
    }

    /**
     * 打开输出日志，默认为关闭
     */
    public void setDebug() {
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    /**
     * 关闭所有的操作，数据开启后，使用完毕要关闭
     */
    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }

    private void closeHelper() {
        if (sHelper != null) {
            sHelper.close();
            sHelper = null;
        }
    }

    private void closeDaoSession() {
        if (sDaoSession != null) {
            sDaoSession.clear();
            sDaoSession = null;
        }
    }
}
