package com.example.huang.myapplication.leave;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
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
import com.example.huang.myapplication.end.EndActivity;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class VisitorIDActivityBak extends BaseActivity {

    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.stu_card)
    TextView mStuCard;
    @BindView(R.id.next)
    Button mNext;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private SpUtils mSpUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_id);

        ButterKnife.bind(this);
        mSpUtils = new SpUtils(VisitorIDActivityBak.this);

        mStuCard.setText("请刷临时卡");
        mTitle.setText("访客临时卡刷卡");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });
        //1.初始化NFC
        initNFC();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dodgerBlue));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //2.界面可见时，判断本机的NFC是否可用
        checkNfcFunction();
        //3.为enableForegroundDispatch做准备
        preGetNFCMessage();
    }

    //4.界面获取焦点时，调用enableForegroundDispatch
    @Override
    protected void onResume() {
        super.onResume();
        // 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new SpUtils(this).clearAll();
    }

    private void checkNfcFunction() {
        //本机不支持NFC，提示并退出应用
        if (mNfcAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("NFC不支持")
                    .setIcon(R.drawable.nfc_icon)
                    .setCancelable(false)
                    .setMessage("检测到该设备不支持NFC功能，无法进行卡片读取")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        } else {//本机支持NFC
            //设备的NFC是否开启。若开启，判断NFC Beam功能是否开启
            if (!mNfcAdapter.isEnabled()) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("NFC未开启")
                        .setIcon(R.drawable.nfc_icon)
                        .setCancelable(false)
                        .setMessage("检测到该设备尚未开启NFC功能，是否开启NFC功能？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(VisitorIDActivityBak.this,"您的设备未开启NFC，将无法读取卡片信息",Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }

        }
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    public void preGetNFCMessage() {
        //将被调用的Intent，用于重复被intent触发后将要执行的跳转
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        //设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
        mFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        //设置参数TechLists
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()},
                new String[]{NfcF.class.getName()}, new String[]{NfcV.class.getName()}};
    }

    //5.当新建意图对象时，读取NFC信息
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String result;
        //解析接收到的数据，主要是函数readTag
        //扫描到标签时，系统会主动将标签信息生成一个tag对象，然后封装到一个intent中。action也是主动设定的。
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String visitorID = Tools.Bytes2HexString(tag.getId(), tag.getId().length);

            //存储学生证号
            mSpUtils.saveVisitorCard(MainActivity.count, visitorID);
            result = "临时卡：" + visitorID;

            mNext.setEnabled(true);
        } else {
            result = "读取失败，请检查卡片并重试";
        }
        mStuCard.setText(result);
        Util.initSoundPool(this);
        Util.play(1, 0);
    }

    @OnClick({R.id.next, R.id.finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.next:
                startActivity(new Intent(VisitorIDActivityBak.this, EndActivity.class).addFlags(2));
                break;
            case R.id.finish:
                mSpUtils.clearAll();
                Intent intent = new Intent(VisitorIDActivityBak.this, EndActivity.class);
                intent.addFlags(2);
                startActivity(intent);
                mStuCard.setText("");
                mNext.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNfcAdapter = null;
    }
}
