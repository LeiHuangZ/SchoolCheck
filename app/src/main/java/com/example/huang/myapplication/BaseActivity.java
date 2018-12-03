package com.example.huang.myapplication;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import com.example.huang.myapplication.utils.SpUtils;

/**
 * 基础类，在Activity不可见时，存储Activity标签
 *
 * @author huang
 * @date 2017/11/1
 */

public abstract class BaseActivity extends AppCompatActivity {
    private KeyPressedReceiver mKeyPressedReceiver;

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mKeyPressedReceiver);
        new SpUtils(this).saveTag(this.getClass().getSimpleName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKeyPressedReceiver = new KeyPressedReceiver();
        IntentFilter intentFilter = new IntentFilter("android.rfid.FUN_KEY");
        registerReceiver(mKeyPressedReceiver, intentFilter);
    }
}
