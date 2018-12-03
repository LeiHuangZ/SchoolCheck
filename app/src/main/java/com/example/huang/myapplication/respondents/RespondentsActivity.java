package com.example.huang.myapplication.respondents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.end.EndActivity;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.visitor.VisitorActivity;
import com.zsy.words.ConnectActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class RespondentsActivity extends BaseActivity {

    private SpUtils mSpUtils;
    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.edt_respondents_num)
    EditText mEdtRespondentsNum;
    @BindView(R.id.btn_respondents_next)
    Button mBtnRespondentsNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respondents);

        ButterKnife.bind(this);
        mSpUtils = new SpUtils(RespondentsActivity.this);

        mTitle.setText("拨打受访人电话");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });

        mEdtRespondentsNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    return;
                }
                mBtnRespondentsNext.setEnabled(true);
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));

        String phoneNbr = mSpUtils.getPhoneNbr(MainActivity.count);
        if (!"".equals(phoneNbr)){
           mBtnRespondentsNext.setEnabled(true);
           mEdtRespondentsNum.setText(phoneNbr);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEdtRespondentsNum.setText(mSpUtils.getPhoneNbr(MainActivity.count));
    }

    @Override
    public void onBackPressed() {
        new SpUtils(RespondentsActivity.this).clearPhoneNbr(MainActivity.count);
        super.onBackPressed();
    }

    @OnClick({R.id.btn_respondents_cancel, R.id.btn_respondents_skip, R.id.img_respondents_call, R.id.btn_respondents_next, R.id.img_respondents_book})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_respondents_cancel:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.btn_respondents_skip:
                mSpUtils.clearPhoneNbr(MainActivity.count);
                Intent intent = new Intent(this, VisitorCardActivity.class);
                startActivity(intent);
                break;
            //拨打受访人电话，并记录受访人电话
            case R.id.img_respondents_call:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (mEdtRespondentsNum.getText().length() > 0 && mEdtRespondentsNum.getText().length() <= 11) {
                    mSpUtils.savePhoneNbr(MainActivity.count, mEdtRespondentsNum.getText().toString());
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mEdtRespondentsNum.getText().toString())));
                } else{
                    Toast.makeText(this, "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
                }
                break;
            //下一步按钮，保存并进入下一个界面
            case R.id.btn_respondents_next:
                if (mEdtRespondentsNum.getText().length() > 0 && mEdtRespondentsNum.getText().length() <= 11) {
                    mSpUtils.savePhoneNbr(MainActivity.count, mEdtRespondentsNum.getText().toString());
                    startActivity(new Intent(this, VisitorCardActivity.class));
                } else{
                    Toast.makeText(this, "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
                }
                break;
            //通讯录
            case R.id.img_respondents_book:
                startActivity(new Intent(RespondentsActivity.this, ConnectActivity.class));
                break;
            default:
                break;
        }
    }
}
