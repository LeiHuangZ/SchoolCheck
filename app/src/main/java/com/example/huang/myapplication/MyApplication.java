package com.example.huang.myapplication;

import android.app.Application;
import android.content.IntentFilter;

/**
 *
 * @author huang
 * @date 2017/11/1
 */

public class MyApplication extends Application {
    private KeyPressedReceiver mKeyPressedReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mKeyPressedReceiver = new KeyPressedReceiver();
        IntentFilter intentFilter = new IntentFilter("android.rfid.FUN_KEY");
        registerReceiver(mKeyPressedReceiver, intentFilter);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mKeyPressedReceiver);
    }
}
