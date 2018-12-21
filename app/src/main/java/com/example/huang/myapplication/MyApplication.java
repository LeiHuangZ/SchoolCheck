package com.example.huang.myapplication;

import android.app.Application;
import android.content.IntentFilter;
import android.util.Log;

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

        initAccessTokenWithAkSk();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mKeyPressedReceiver);
    }

    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                Log.v("Huang, MyApplication", "token =" + token);
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Log.i("Huang, MyApplication", "初始化SDK失败\n" + Log.getStackTraceString(error));
            }
        }, getApplicationContext(),  "cAG2gXd2C2Ay1lm7b7ji0q4o", "MsSs9yvmajotum7Gw1w4CnHO8Xjm431P");
    }
}
