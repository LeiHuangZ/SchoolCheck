package com.example.huang.myapplication.certificate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.certificate.identification.handheld.instance.IDReadActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class CertificateActivity extends BaseActivity {

    @BindView(R.id.title)
    DrawableTextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);
        ButterKnife.bind(this);

        title.setText("证件扫描");
        title.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                finish();
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));
    }

    @OnClick({R.id.btn_IDCard, R.id.btn_driving_licence, R.id.btn_passport, R.id.btn_residence, R.id.btn_carLicense, R.id.btn_other})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //开启身份证扫描界面
            case R.id.btn_IDCard:
                startActivity(new Intent(CertificateActivity.this, IDReadActivity.class));
                break;
            //开启驾照扫描界面
            case R.id.btn_driving_licence:
                Intent intent = new Intent(CertificateActivity.this, LicenseActivity.class);
                //标签，0 -->驾照
                intent.addFlags(0);
                startActivity(intent);
                break;
            //开启护照扫描界面
            case R.id.btn_passport:
                Intent intent2 = new Intent(CertificateActivity.this, LicenseActivity.class);
                //标签 ，2 --> 护照
                intent2.addFlags(2);
                startActivity(intent2);
                break;
            case R.id.btn_residence:
                Intent intent1 = new Intent(CertificateActivity.this, LicenseActivity.class);
                //标签 ，1 --> 居住证
                intent1.addFlags(1);
                startActivity(intent1);
                break;
            case R.id.btn_carLicense:
                Intent intent3 = new Intent(CertificateActivity.this, LicenseActivity.class);
                //标签 ，3 --> 行驶证
                intent3.addFlags(3);
                startActivity(intent3);
                break;
            case R.id.btn_other:
                startActivity(new Intent(CertificateActivity.this, OtherActivity.class));
                break;
            default:
                break;
        }
    }
}
