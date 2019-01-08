package com.example.huang.myapplication.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.utils.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.huang.myapplication.utils.Tools.notifySystemToScan;

/**
 * @author huang
 */
public class WelcomeActivity extends AppCompatActivity {

    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    /**
     * 创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
     */
    List<String> mPermissionList = new ArrayList<>();
    private final int PERMISSION_REQUEST_CODE = 1280;
    private Toast mToast;
    private boolean mIsRequesting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //6.0才用动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mIsRequesting) {
            initPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //有无权限没有通过
        boolean hasPermissionDismiss = false;
        if (PERMISSION_REQUEST_CODE == requestCode) {
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                //跳转到系统设置权限页面
                String packName = "com.example.huang.myapplication";
                Uri packageURI = Uri.parse("package:" + packName);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                startActivity(intent);
                // 提示用于手动授权
                showToast();
            }else{
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
            mIsRequesting = false;
        }
    }

    /**
     * Android6.0及以上权限判断与申请
     */
    private void initPermission(){
        //逐个判断你要的权限是否已经通过
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //添加还未授予的权限
                mPermissionList.add(permission);
            }
        }
        // 申请未申请的权限
        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            mIsRequesting = true;
        }else{
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * 提示用户手动授权
     */
    private void showToast(){
        if (mToast == null){
            mToast = Toast.makeText(this, "已禁用权限，请手动授予", Toast.LENGTH_LONG);
            mToast.show();
        } else {
            mToast.cancel();
            mToast.setText("已禁用权限，请手动授予");
            mToast.show();
        }
    }
}
