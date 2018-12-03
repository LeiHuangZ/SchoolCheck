package com.example.huang.myapplication.greendao;

import android.content.Context;

import com.zsy.words.bean.DaoSession;
import com.zsy.words.bean.Person;
import com.zsy.words.bean.PersonDao;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 *  未盘物品信息实际操作类
 * @author huang
 * @date 2017/11/6
 */

public class PersonDaoManager {
    private static PersonDaoManager sManager;
    private final DaoManager mDaoManager;

    private PersonDaoManager(Context context) {
        mDaoManager = DaoManager.getInstance(context);
    }

    /**
     * 关闭数据相关连接，记得一定要调用，避免内存泄漏
     */
    public void close(){
        mDaoManager.closeConnection();
    }

    /**
     * 获取单例引用
     *
     * @param context 上下文环境，为了防止内存泄漏，使用弱引用
     * @return 返回本管理者对象
     */
    public static PersonDaoManager getInstance(Context context) {
        WeakReference<Context> weakReference = new WeakReference<>(context);
        if (sManager == null) {
            synchronized (PersonDaoManager.class) {
                if (sManager == null) {
                    sManager = new PersonDaoManager(weakReference.get());
                }
            }
        }
        return sManager;
    }

    /** 录入数据 */
    public void insertContactList(List<Person> list){
        if (list == null || list.isEmpty()){
            return;
        }
        DaoSession daoSession = mDaoManager.getDaoSession();
        PersonDao personDao = daoSession.getPersonDao();
        personDao.insertInTx(list);
    }

    /** 查询所有记录 */
    public List<Person> queryAll(){
        return mDaoManager.getDaoSession().loadAll(Person.class);
    }

    /**
     * 删除所有记录
     */
    public boolean deleteAll(){
        boolean flag = false;
        try {
            mDaoManager.getDaoSession().deleteAll(Person.class);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

}