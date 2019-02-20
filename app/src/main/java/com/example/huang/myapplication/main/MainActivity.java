package com.example.huang.myapplication.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.certificate.CertificateActivity;
import com.example.huang.myapplication.leave.VisitorFaceActivity;
import com.example.huang.myapplication.retrofit.RetrofitHelper;
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
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
    @BindView(R.id.visitor)
    Button mVisitor;
    @BindView(R.id.visitor_leave)
    Button mVisitorLeave;
    @BindView(R.id.student)
    Button mStudent;
    @BindView(R.id.custom_ui)
    ImageView mCustomUi;
    private SpUtils mSpUtils;

    private boolean mIsInitSuccess = false;

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

        mStudent.setEnabled(false);
        mVisitor.setEnabled(false);
        mVisitorLeave.setEnabled(false);
        mMainBtnRefresh.setEnabled(false);
        mCustomUi.setEnabled(false);


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


        /*
         * 检查鉴权状态
         */
        boolean auth = mSpUtils.getAuth();
        if (!auth) {
            return;
        }

        //判断是否设置了IP地址，没有设置，则开启设置界面
        // TODO: 2018/12/28 判断IP信息，记得开启
        String ip = mSpUtils.getIP();
        if ("".equals(ip)) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        }
        //同步通讯录
        onViewClicked(mMainBtnRefresh);
        mStudent.setEnabled(true);
        mVisitor.setEnabled(true);
        mVisitorLeave.setEnabled(true);
        mMainBtnRefresh.setEnabled(true);
        mCustomUi.setEnabled(true);
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
            this.mConnectDisconnect.setText(R.string.state_disconnect);
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
        } else if (connectState.equals("authFailed")) {
            this.connectState.setImageResource(R.drawable.disconnect);
            this.mConnectDisconnect.setText("设备未鉴权，请联系管理 " + getImei());
            this.mConnectDisconnect.setVisibility(View.VISIBLE);
            mSpUtils.putAuth(false);
        } else if (connectState.equals("authSuccess")) {
            this.connectState.setImageResource(R.drawable.connect);
            this.mConnectDisconnect.setText("已连接");
            this.mConnectDisconnect.setVisibility(View.VISIBLE);
            mSpUtils.putAuth(true);
            mExecutorService.shutdown();
            if (!mIsInitSuccess) {
                initAccessTokenWithAkSk();
            }
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
            onViewClicked(mMainBtnRefresh);
            mStudent.setEnabled(true);
            mVisitor.setEnabled(true);
            mVisitorLeave.setEnabled(true);
            mMainBtnRefresh.setEnabled(true);
            mCustomUi.setEnabled(true);
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
        /*
         * 检查鉴权状态
         */
        boolean auth = mSpUtils.getAuth();
        if (!auth) {
            initAuthTask();
            return;
        }

        if (!mIsInitSuccess) {
            initAccessTokenWithAkSk();
        }
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
                    if (mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_ID)) {

                    }
                }
                break;
            case R.id.student:
                count++;
                startActivity(new Intent(this, FaceActivity.class));
                break;
            case R.id.visitor_leave:
                count++;
                startActivity(new Intent(this, VisitorFaceActivity.class));
                break;
            case R.id.main_btn_refresh:
                queryTeachers();
                break;
            case R.id.custom_ui:
                startActivity(new Intent(MainActivity.this, CustomActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        if (mRetryDialog != null) {
            mRetryDialog.cancel();
            mRetryDialog = null;
        }
        //停止本次心跳
        SocketClientUtils.getInstance("192.168.1.154").changeFlag(false);
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mTimeoutDialog != null) {
            mExecutorService.shutdownNow();
            mTimeoutDialog.cancel();
            mTimeoutDialog = null;
        }
        super.onDestroy();

    }

    /**
     * 获取设备IMEI
     *
     * @return 设备IMEI
     */
    private String getImei() {
        //实例化TelephonyManager对象
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //获取IMEI号
        @SuppressLint({"MissingPermission", "HardwareIds"}) String imei = telephonyManager != null ? telephonyManager.getDeviceId() : null;
        //在次做个验证，也不是什么时候都能获取到的啊
        if (imei == null) {
            imei = "";
        }
        return imei;
    }

    /**
     * 获取通讯录
     */
    private void queryTeachers() {
        // 获取存储的上一次通讯录同步时间（因接口未提供唯一标识，无法进行本地数据库更新，暂时固定为"1970-01-01 00:00:00"）
        String lastSyncTime = mSpUtils.getLastSyncTime();
        // 获取配置的学校名称
        String school = mSpUtils.getSchool();
        // 更新进行时，更新按钮不可点击
        mMainBtnRefresh.setEnabled(false);
        // 请求获取通讯录
        // TODO: 2018/12/26 每页获取的数量
        RetrofitHelper.getInstance(MainActivity.this).queryTeachers(false, "", 1000, school, new ArrayList<String>(), 1, lastSyncTime);
    }

    @Override
    public void onBackPressed() {
        // TODO 2018/12/19 正式发布版本时，删除以屏蔽返回键
//        super.onBackPressed();
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

    /**
     * 用明文ak，sk初始化百度识别SDK
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                Log.v("Huang, MyApplication", "token =" + token);
                mIsInitSuccess = true;
                //时间正确，开始计时
//                initTimer();
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Log.i("Huang, MyApplication", "初始化SDK失败\n" + Log.getStackTraceString(error));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRetryDialog();
                    }
                });
                mIsInitSuccess = false;
            }
        }, getApplicationContext(), "cAG2gXd2C2Ay1lm7b7ji0q4o", "MsSs9yvmajotum7Gw1w4CnHO8Xjm431P");
    }

    /**
     * 提醒用户SDK初始化失败，检查网络和时间设置
     */
    private AlertDialog mRetryDialog;

    private void showRetryDialog() {
        if (mRetryDialog == null) {
            mRetryDialog = new AlertDialog.Builder(this)
                    .setMessage("识别SDK初始化失败，请检查网络和时间后重试")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
        mRetryDialog.show();
    }

    /**
     * 定时获取系统时间
     */
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.AbortPolicy());

    private void initTimer() {
        mExecutorService.scheduleWithFixedDelay(task, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private HandlerTimer mHandlerTimer = new HandlerTimer(this);

    private static class HandlerTimer extends Handler {
        private WeakReference<MainActivity> mReference;

        HandlerTimer(MainActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mReference.get();
            switch (msg.what) {
                case 1:
                    mainActivity.timerHandeViewMethod();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            mHandlerTimer.sendMessage(message);
        }
    };

    private long maxTime = 15811200000L;

    public void timerHandeViewMethod() {
        long currentTime = System.currentTimeMillis();
        long initTime = mSpUtils.getInitTime();
        if (initTime == 0) {
            mSpUtils.putInitTime(currentTime);
        } else {
            long l = currentTime - initTime;
            if (l > maxTime) {
                showTimeoutDialog();
            }
        }
    }

    /**
     * 提醒用户限制已到期
     */
    private AlertDialog mTimeoutDialog;

    private void showTimeoutDialog() {
        if (mTimeoutDialog == null) {
            mTimeoutDialog = new AlertDialog.Builder(this)
                    .setMessage("许可证已过期")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
        mTimeoutDialog.show();
    }

    /**
     * 如果设备未鉴权，定时获取鉴权信息
     */
    private void initAuthTask() {
        mExecutorService.scheduleWithFixedDelay(authTask, 0, 10000, TimeUnit.MILLISECONDS);
    }

    private TimerTask authTask = new TimerTask() {
        @Override
        public void run() {
            /*
             * 鉴权
             */
            boolean auth = mSpUtils.getAuth();
            if (!auth) {
                String imei = getImei();
                mSpUtils.putIMEI(imei);
                Log.v("Huang, MainActivity", "deviceID = " + imei);
                RetrofitHelper.quthDevice(imei);
            }
        }
    };
}
