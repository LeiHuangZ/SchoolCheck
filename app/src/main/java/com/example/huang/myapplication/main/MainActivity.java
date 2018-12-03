package com.example.huang.myapplication.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.certificate.CertificateActivity;
import com.example.huang.myapplication.leave.VisitorFaceActivity;
import com.example.huang.myapplication.student.FaceActivity;
import com.example.huang.myapplication.system.CustomActivity;
import com.example.huang.myapplication.system.SettingActivity;
import com.example.huang.myapplication.system.WifiControl;
import com.example.huang.myapplication.utils.MyTask;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.utils.SocketClientUtils;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.UiSpUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * @author huang
 */
public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.connect_state)
    public ImageView connectState;
    @BindView(R.id.school_name)
    TextView schoolName;
    @BindView(R.id.data_ip)
    TextView dataIp;
    @BindView(R.id.connect_disconnect)
    public TextView mConnectDisconnect;
    @BindView(R.id.update_count)
    TextView mUpdateCount;
    @BindView(R.id.main_btn_refresh)
    FloatingActionButton mMainBtnRefresh;
    private SpUtils mSpUtils;

    /**
     * 任务列表
     */
    public static LinkedList<MyTask> mList = new LinkedList<>();
    /**
     * 任务上传标记
     */
    public static long count;
    private MaterialDialog mInputDialog;
    private UiSpUtils mUiSpUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSpUtils = new SpUtils(this);
        mUiSpUtils = new UiSpUtils(MainActivity.this);
        //保证WiFi的开启状态
        WifiControl.getInstance(this).openWifi();

        //初始化线程池
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue));

        //EventBus注册
        EventBus.getDefault().register(this);

        //判断是否设置了IP地址，没有设置，则开启设置界面
        String ip = mSpUtils.getIP();
        if ("".equals(ip)) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return;
        }

        //同步通讯录
        MyTask myTask = new MyTask(this);
        myTask.execute(5);
        mMainBtnRefresh.setEnabled(false);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post("12345");
            }
        });
    }

    private void initView() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日", Locale.SIMPLIFIED_CHINESE);
        String data = format.format(System.currentTimeMillis());

        dataIp.setText(data);
        dataIp.append("  " + "http://www.zobao.net/");

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (mList.size() == 0) {
            PhotoUtils.deleteDirectory(MainActivity.this, "/storage/sdcard0/tempPhoto/");
            mSpUtils.clearAll();
            return;
        }
        mUpdateCount.setText("上传中：");
        mUpdateCount.append(mList.size() + "");
    }

    /**
     * 接收数据传输结果，更改UI界面
     */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onDataSynEvent(String connectState) {
        String disconnect = "1";
        String connect = "0";
        Log.i(TAG, "onDataSynEvent: " + connectState);
        if (disconnect.equals(connectState)) {
            this.connectState.setImageResource(R.drawable.disconnect);
            this.mConnectDisconnect.setVisibility(View.VISIBLE);
        } else if (connect.equals(connectState)) {
            this.connectState.setImageResource(R.drawable.connect);
            this.mConnectDisconnect.setVisibility(View.INVISIBLE);
        } else if ("contact_success".equals(connectState)) {
            Toast.makeText(this, "通讯录同步成功", Toast.LENGTH_SHORT).show();
            mMainBtnRefresh.setEnabled(true);
        } else if ("contact_fail".equals(connectState)) {
            Toast.makeText(this, "通讯录同步失败", Toast.LENGTH_SHORT).show();
            mMainBtnRefresh.setEnabled(true);
        } else if ("update".equals(connectState)) {
            mInputDialog = new MaterialDialog.Builder(MainActivity.this)
                    .title("稍候")
                    .content("正在下载更新包，请稍候....")
                    .progress(true, 100)
                    .cancelable(false)
                    .show();
        } else if ("updatefinish".equals(connectState)) {
            mInputDialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "app.apk")), "application/vnd.android.package-archive");
            startActivity(intent);
        } else if ("12345".equals(connectState)) {
            mMainBtnRefresh.setEnabled(true);
        } else {
            Log.i(TAG, "mList.size(): " + mList.size());
            if (mList.size() == 0) {
                return;
            }
            mList.remove(0);
            if (mList.size() == 0) {
                mUpdateCount.setText("");
                PhotoUtils.deleteDirectory(MainActivity.this, "/storage/sdcard0/tempPhoto/");
                mSpUtils.clearAll();
                return;
            }
            mUpdateCount.setText("上传中：");
            mUpdateCount.append(mList.size() + "");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mSpUtils.getIP().equals("")) {
            //开启心跳
            SocketClientUtils.getInstance("192.168.1.154").changeFlag(true);
            SocketClientUtils socketClientUtils = SocketClientUtils.getInstance(new SpUtils(this).getIP());
            socketClientUtils.heartBeat();

            //检测本地存储的服务器最新版本和APP版本是否相对应
            int serverVersion = mSpUtils.getVersion();
            /* 获取本地版本号并转换为int */
            PackageManager manager = getPackageManager();
            PackageInfo info;
            try {
                info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                String versionName = info.versionName;
                int versionNameToInt = versionNameToInt(versionName);
                Log.i(TAG, "sendUpdate, localVersion = " + versionNameToInt);
                Log.i(TAG, "sendUpdate, savedServerVersion = " + serverVersion);
                if (serverVersion != versionNameToInt) {
                    //获取升级
                    MyTask myTask1 = new MyTask(this);
                    myTask1.execute(3);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        initView();
        if (mList.size() == 0) {
            PhotoUtils.deleteDirectory(MainActivity.this, "/storage/sdcard0/tempPhoto/");
            mSpUtils.clearAll();
            count = 0;

        }

        //刷新显示学校信息
        schoolName.setText(mSpUtils.getSchool());


        Log.i(TAG, "onStart: ");
    }

    @OnClick({R.id.visitor, R.id.student, R.id.visitor_leave, R.id.main_btn_refresh, R.id.custom_ui})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //来访登记
            case R.id.visitor:
                count++;
                boolean isIdSelected = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_ID);
                if (isIdSelected) {
                    startActivity(new Intent(MainActivity.this, CertificateActivity.class));
                } else {
                    // TODO: 2018/1/12 判断配置的界面标记，启动相对应的界面
                    if (mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_ID)){

                    }
                }
                break;
            case R.id.student:
                count++;
                startActivity(new Intent(this, FaceActivity.class));
//                startActivity(new Intent(this, Camera2TestActivity.class));
                break;
            case R.id.visitor_leave:
                count++;
                startActivity(new Intent(this, VisitorFaceActivity.class));
                break;
            case R.id.main_btn_refresh:
                //同步通讯录
                MyTask myTask = new MyTask(this);
                myTask.execute(5);
                Log.i(TAG, "onViewClicked: ");
                mMainBtnRefresh.setEnabled(false);
//                mExecutorService.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(3000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        EventBus.getDefault().post("12345");
//                    }
//                });
                break;
            case R.id.custom_ui:
                startActivity(new Intent(MainActivity.this,CustomActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        //停止本次心跳
        SocketClientUtils.getInstance("192.168.1.154").changeFlag(false);
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
    }

    private int versionNameToInt(String versionName) {
        byte[] version = new byte[4];
        int location = 0;
        while (versionName.lastIndexOf(".") != -1) {
            String str = versionName.substring(versionName.lastIndexOf(".") + 1);
            version[location] = Byte.decode(str);
            location++;
            versionName = versionName.substring(0, versionName.lastIndexOf("."));
        }
        version[location] = Byte.decode(versionName);
        return bytesToInt(version, 0);
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }
}
