package com.example.huang.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.SocketClientUtils;

/**
 * @author huang
 * @date 2017/11/1
 * 后台服务，用于队列上传任务
 */

public class MyService extends Service {
    private static String TAG = "MyService";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //开启心跳
        SocketClientUtils.getInstance("192.168.1.154").changeFlag(true);
        SocketClientUtils socketClientUtils = SocketClientUtils.getInstance(new SpUtils(this).getIP());
        socketClientUtils.heartBeat();
        Log.i(TAG, "onBind: ");
        return new Binder();
    }

    @Override
    public void onDestroy() {
        //停止本次心跳
        SocketClientUtils.getInstance("192.168.1.154").changeFlag(false);
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
