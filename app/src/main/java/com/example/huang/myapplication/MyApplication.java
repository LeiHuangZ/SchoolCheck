package com.example.huang.myapplication;

import android.Manifest;
import android.app.Application;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

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
