package com.example.huang.myapplication;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.example.huang.myapplication.main.MainActivity;

import java.lang.ref.WeakReference;

/**
 *  MainActivity的Handler
 * @author huang
 * @date 2017/11/1
 */

public class MyHandler extends Handler {
    private WeakReference<MainActivity> mWeakReference;
    public MyHandler(MainActivity activity) {
        mWeakReference = new WeakReference<MainActivity>(activity);
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        MainActivity mainActivity = mWeakReference.get();
        if (msg.what == 0) {
            mainActivity.connectState.setImageResource(R.drawable.connect);
            mainActivity.mConnectDisconnect.setVisibility(View.INVISIBLE);
        } else {
            mainActivity.connectState.setImageResource(R.drawable.disconnect);
            mainActivity.mConnectDisconnect.setVisibility(View.VISIBLE);
        }
    }
}
