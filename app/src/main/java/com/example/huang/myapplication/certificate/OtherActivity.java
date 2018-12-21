package com.example.huang.myapplication.certificate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.visitor.VisitorActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OtherActivity extends AppCompatActivity {

    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.storage_ensure_edt_name)
    EditText mStorageEnsureEdtName;
    @BindView(R.id.storage_ensure_edt_id)
    EditText mStorageEnsureEdtId;
    @BindView(R.id.sex_rg)
    RadioGroup mSexRg;

    private SpUtils mSpUtils;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        ButterKnife.bind(this);

        mTitle.setText("输入身份信息");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));
    }

    @Override
    public void onBackPressed() {
        clearData();
        super.onBackPressed();
    }

    /**
     * 清除存储的身份信息
     */
    private void clearData() {
        if (mSpUtils == null) {
            mSpUtils = new SpUtils(this);
        }
        //删除存储的上次存储的姓名、性别、地址、身份证号
        mSpUtils.saveName(MainActivity.count, "");
        mSpUtils.saveSex(MainActivity.count, -1);
        mSpUtils.saveIdentity(MainActivity.count, "");
    }

    @OnClick({R.id.other_btn_cancel, R.id.other_btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.other_btn_cancel:
                clearData();
                //取消操作，返回主界面
                startActivity(new Intent(OtherActivity.this, MainActivity.class));
                break;
            case R.id.other_btn_next:
                saveData();
                break;
            default:
                break;
        }
    }

    /**
     * 存储身份信息
     */
    private void saveData() {
        String nullStr = "";
        int idLen = 18;
        int sex = -1;
        String name = mStorageEnsureEdtName.getText().toString().trim();
        if (name.equals(nullStr)){
            showToast("请输入姓名");
            return;
        }
        int checkedRadioButtonId = mSexRg.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1){
            showToast("请选择性别");
            return;
        }
        if (checkedRadioButtonId == R.id.male_rb){
            sex = 1;
        } else if (checkedRadioButtonId == R.id.female_rb){
            sex = 2;
        }
        String id = mStorageEnsureEdtId.getText().toString().trim();
        if (id.equals(nullStr) || id.length() != idLen) {
            showToast("请输入正确的身份证号");
            return;
        }
        if (mSpUtils == null) {
            mSpUtils = new SpUtils(this);
        }
        //删除存储的上次存储的姓名、性别、地址、身份证号
        mSpUtils.saveName(MainActivity.count, name);
        mSpUtils.saveSex(MainActivity.count, sex);
        mSpUtils.saveIdentity(MainActivity.count, id);
        startActivity(new Intent(OtherActivity.this, VisitorActivity.class));
    }

    /**
     * 展示土司信息
     * @param content 需要展示的提示信息
     */
    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(OtherActivity.this, content, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(content);
            mToast.show();
        }
    }
}
