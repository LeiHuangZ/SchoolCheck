package com.example.huang.myapplication.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class SettingActivity extends AppCompatActivity {
    private static String TAG = SettingActivity.class.getSimpleName();

    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.setting_edt_ip)
    EditText mSettingEdtIp;
    @BindView(R.id.setting_edt_title)
    EditText mSettingEdtTitle;
    @BindView(R.id.setting_rcv_wifi)
    RecyclerView mSettingRcvWifi;
    @BindView(R.id.setting_srl)
    SwipeRefreshLayout mSettingSrl;
    @BindView(R.id.setting_edt_door)
    EditText mSettingEdtDoor;
    @BindView(R.id.setting_edt_client_ip)
    EditText mSettingEdtClientIp;
    private String mIp;
    private String mTitle1;
    private SpUtils mSpUtils;
    private RcAdapter mAdapter;
    private List<ScanResult> mList = new ArrayList<>();
    private WifiControl mWifiControl;
    private String mDoorName;
    private String mClientIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        initView();
        updateWifi();
        pullToRefresh();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gray));
    }

    private void pullToRefresh() {
        mSettingSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSettingSrl.setRefreshing(true);
                updateWifi();
            }
        });
    }


    private void initView() {
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });

        mSpUtils = new SpUtils(this);

        mWifiControl = WifiControl.getInstance(this);

        mIp = mSpUtils.getIP();
        mTitle1 = mSpUtils.getSchool();
        mDoorName = mSpUtils.getDoorName();
        mClientIP = mSpUtils.getClientIP();
        mSettingEdtIp.setText(mIp);
        mSettingEdtTitle.setText(mTitle1);
        mSettingEdtDoor.setText(mDoorName);
        mSettingEdtClientIp.setText(mClientIP);

        mSettingRcvWifi.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RcAdapter(this, mList);
        mSettingRcvWifi.setAdapter(mAdapter);

        //根据WiFi不同类型弹出弹窗
        mAdapter.setRcItemClickListener(new RcAdapter.RcItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                List<ScanResult> list = mAdapter.getList();
                final ScanResult result = list.get(position);
                final String ssid = result.SSID;
                AlertDialog.Builder alert = new AlertDialog.Builder(SettingActivity.this);
                alert.setTitle(ssid);
                //未知的WiFi
                if (!mWifiControl.isSaved(ssid)) {
                    alert.setMessage("输入密码");
                    final EditText etPassword = new EditText(SettingActivity.this);
                    alert.setView(etPassword);
                    alert.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String psd = etPassword.getText().toString().trim();
                            int psdLen = 8;
                            if (psd.length() < psdLen) {
                                Toast.makeText(SettingActivity.this, "密码至少为8位数", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mWifiControl.createAndConnect(ssid, psd);
                        }
                    });
                    alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    //已连接的WiFi
                } else if (mWifiControl.isConnected(ssid)) {
                    alert.setMessage(result.capabilities);
                    alert.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alert.setNegativeButton("取消保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWifiControl.disConnect();
                            mWifiControl.removeNetwork(ssid);
                        }
                    });
                    //已保存的WiFi
                } else if (mWifiControl.isSaved(ssid)) {
                    alert.setMessage(result.capabilities);
                    alert.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWifiControl.connect(ssid);
                        }
                    });
                    alert.setNegativeButton("取消保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWifiControl.removeNetwork(ssid);
                            updateWifi();
                        }
                    });
                    alert.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                }

                alert.create();
                alert.show();
            }
        });
    }

    private void updateWifi() {
        mList.clear();
        mList.addAll(mWifiControl.getScanResultList());
        mAdapter.setList(mList);
        mSettingSrl.setRefreshing(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    updateWifi();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    updateWifi();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        if ("".equals(mSettingEdtIp.getText().toString()) && "".equals(mSettingEdtTitle.getText().toString())) {
            finish();
        } else if (mTitle1.equals(mSettingEdtTitle.getText().toString()) && mIp.equals(mSettingEdtIp.getText().toString()) && mDoorName.equals(mSettingEdtDoor.getText().toString())
                && mClientIP.equals(mSettingEdtClientIp.getText().toString())) {
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("配置未保存")
                    .setCancelable(false)
                    .setMessage("您有未保存的配置信息，是否保存")
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSpUtils.saveIP(mSettingEdtIp.getText().toString());
                            mSpUtils.saveSchool(mSettingEdtTitle.getText().toString());
                            mSpUtils.saveDoorName(mSettingEdtDoor.getText().toString());
                            mSpUtils.saveClientIP(mSettingEdtClientIp.getText().toString());
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @OnClick({R.id.btn_setting_cancel, R.id.btn_setting_sure})
    public void onViewClicked(View view) {
        startActivity(new Intent(SettingActivity.this, MainActivity.class));
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.btn_setting_sure:
                mSpUtils.saveIP(mSettingEdtIp.getText().toString());
                mSpUtils.saveSchool(mSettingEdtTitle.getText().toString());
                mSpUtils.saveDoorName(mSettingEdtDoor.getText().toString());
                mSpUtils.saveClientIP(mSettingEdtClientIp.getText().toString());
                finish();
                break;
            default:
                break;
        }
    }
}
