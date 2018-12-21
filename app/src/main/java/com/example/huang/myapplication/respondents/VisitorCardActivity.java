package com.example.huang.myapplication.respondents;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.certificate.identification.handheld.instance.Util;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.Tools;
import com.zistone.card.MyCardManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class VisitorCardActivity extends BaseActivity {

    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.visitor_card)
    TextView mVisitorCard;
    @BindView(R.id.next)
    Button mNext;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private SpUtils mSpUtils;

    private boolean isReading = false;
    private MyCardManager myCardManager;
    private String visitorNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_card);

        myCardManager = new MyCardManager(this);

        ButterKnife.bind(this);
        mSpUtils = new SpUtils(VisitorCardActivity.this);

        mTitle.setText("发放临时卡");
        mVisitorCard.setText("请读取临时卡");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dodgerBlue));
    }

    private class ThreadRun implements Runnable{
        @Override
        public void run() {
            isReading = true;
            while(isReading){
                if(myCardManager != null){
                    visitorNum = myCardManager.readCardBID();
                    if(visitorNum == null){
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
//                        isReading = false;
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 0:
                    //存储访客临时卡号
                    mSpUtils.saveVisitorCard(MainActivity.count, visitorNum);
                    String result = "临时卡号：" + visitorNum;
                    mNext.setEnabled(true);
                    mVisitorCard.setText(result);
                    Util.initSoundPool(VisitorCardActivity.this);
                    Util.play(1, 0);
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(myCardManager != null)
            myCardManager.openCard(0);
        new Thread(new ThreadRun()).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isReading = false;
        if(myCardManager != null)
            myCardManager.close();
    }

    //4.界面获取焦点时，调用enableForegroundDispatch
    @Override
    protected void onResume() {
        super.onResume();
        // 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new SpUtils(this).clearAll();
    }

    @OnClick({R.id.next, R.id.finish, R.id.visitor_card_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.next:
                isReading = false;
                if (myCardManager != null) {
                    myCardManager.close();
                    myCardManager = null;
                }
                startActivity(new Intent(VisitorCardActivity.this, VisitorFaceTakeActivity.class).addFlags(0));
                break;
            case R.id.finish:
                isReading = false;
                if (myCardManager != null) {
                    myCardManager.close();
                    myCardManager = null;
                }
                mSpUtils.saveVisitorCard(MainActivity.count, "");
                Intent intent = new Intent(VisitorCardActivity.this, VisitorFaceTakeActivity.class);
                startActivity(intent);
                mVisitorCard.setText("");
                mNext.setEnabled(false);
                break;
            case R.id.visitor_card_cancel:
                isReading = false;
                if (myCardManager != null) {
                    myCardManager.close();
                    myCardManager = null;
                }
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isReading = false;
        if(myCardManager != null)
            myCardManager.close();
    }
}
