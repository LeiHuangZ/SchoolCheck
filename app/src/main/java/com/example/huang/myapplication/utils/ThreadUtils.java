package com.example.huang.myapplication.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author huang
 * @date 2017/12/15  13:59
 * @Describe Thread管理类
 */
public class ThreadUtils {
    private static ThreadUtils mThreadUtils;
    private ExecutorService mExecutorService;
    private ThreadUtils(){
        //初始化线程池
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
    public static ThreadUtils getInstance(){
        if (mThreadUtils == null){
            mThreadUtils = new ThreadUtils();
        }
        return mThreadUtils;
    }

    public ExecutorService getExecutorService(){
        return mExecutorService;
    }
}
