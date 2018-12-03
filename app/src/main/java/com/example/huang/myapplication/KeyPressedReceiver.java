package com.example.huang.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.example.huang.myapplication.system.SettingActivity;

/**
 * 按键广播接收者,双击F1键计入设置界面
 *
 * @author huang
 * @date 2017/10/30
 */

public class KeyPressedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int keyCode = intent.getIntExtra("keyCode", 0);
        if (keyCode == 0) {
            keyCode = intent.getIntExtra("keyCode", 0);
        }
        boolean keydown = intent.getBooleanExtra("keydown", false);
        if (keydown) {
            if (keyCode ==KeyEvent.KEYCODE_F1) {
                context.getApplicationContext().startActivity(new Intent(context, SettingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
}
