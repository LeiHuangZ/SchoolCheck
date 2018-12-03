package com.example.huang.myapplication.system;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * WiFi管理类
 *
 * @author huang
 * @date 2017/10/25
 */

public class WifiControl {
    private WifiManager mWifiManager;

    /**
     * WifiControl唯一实例对象，饿汉式
     */
    private static WifiControl sWifiControl;

    /**
     * 私有构造
     */
    private WifiControl(Context context) {
        //获取WifiManager，此对象是操作WiFi的主要类
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 提供公共静态方法供外界访问
     *
     * @return 返回实例对象
     */
    public static WifiControl getInstance(Context context) {
        if (sWifiControl == null) {
            sWifiControl = new WifiControl(context);
        }
        return sWifiControl;
    }

    /**
     * 开启WiFi
     */
    public void openWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return;
        }
        mWifiManager.setWifiEnabled(true);
    }

    /**
     * 获取扫描到的WiFi列表,根据信号强度排序
     *
     * @return list 装有扫描到的WiFi的集合
     */
    List<ScanResult> getScanResultList() {
        mWifiManager.startScan();
        List<ScanResult> scanResultList = mWifiManager.getScanResults();
        List<ScanResult> scanResultList2 = new ArrayList<>();
        //根据信号排序
        Collections.sort(scanResultList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                return o2.level - o1.level;
            }
        });
        //移除名称为空的WiFi，将已连接的WiFi移至第一位
        for (ScanResult result :
                scanResultList) {
            if (mWifiManager.getConnectionInfo().getSSID().equals("\"" + result.SSID.trim() + "\"")) {
                scanResultList2.add(0, result);
            } else if (!"".equals(result.SSID.trim())) {
                scanResultList2.add(result);
            }
        }
        return scanResultList2;
    }

    private int getIndex(String ssid) {
        List<WifiConfiguration> configurationList = mWifiManager.getConfiguredNetworks();
        for (int j = 0; j < configurationList.size(); j++) {
            if (("\"" + ssid + "\"").equals(configurationList.get(j).SSID)) {
                return configurationList.get(j).networkId;
            }
        }
        return -1;
    }

    /**
     * 取消WiFi保存
     *
     * @param ssid 取消保存的WiFi的SSID
     */
    void removeNetwork(String ssid) {
        int index = getIndex(ssid);
        mWifiManager.removeNetwork(index);
    }

    /**
     * 判断传入的WiFi是否为当前连接的WiFi
     *
     * @param ssid 需要判断的WiFi的SSID
     * @return 判断结果
     */
    boolean isConnected(String ssid) {
        String regex = "\"";
        WifiInfo info = mWifiManager.getConnectionInfo();
        String ssid1 = info.getSSID();
        int linkSpeed = info.getLinkSpeed();
        return linkSpeed != 0 && (regex + ssid + regex).equals(ssid1);
    }

    /**
     * 判断传入的WiFi是否已经保存
     *
     * @param ssid 需要判断的WiFi的SSID
     * @return 判断结果
     */
    boolean isSaved(String ssid) {
        String regex = "\"";
        List<String> list = new ArrayList<>();
        List<WifiConfiguration> configurationList = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration config :
                configurationList) {
            list.add(config.SSID);
        }
        return list.contains(regex + ssid + regex);
    }

    /**
     * 连接已经配置好的WiFi
     *
     * @param ssid WiFi的ssid
     */
    void connect(String ssid) {
        int index = getIndex(ssid);
        mWifiManager.enableNetwork(index, true);
    }

    /**
     * 创建并连接一个没有配置过的WiFi
     *
     * @param ssid     WiFi的ssid
     * @param password WiFi密码
     */
    void createAndConnect(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        config.preSharedKey = "\"" + password + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;

        int network = mWifiManager.addNetwork(config);
        mWifiManager.enableNetwork(network, true);
    }
    /**
     * 断开现在连接的WiFi
     */
    void disConnect() {
        int netId = mWifiManager.getConnectionInfo().getNetworkId();
        mWifiManager.disableNetwork(netId);
    }
}
