package com.example.huang.myapplication.visitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.respondents.RespondentsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class VisitorActivity extends BaseActivity {

    private SpUtils mSpUtils;
    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.edt_visitor_num)
    EditText mEdtVisitorNum;
    @BindView(R.id.btn_visitor_next)
    Button mBtnVisitorNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);

        ButterKnife.bind(this);
        mSpUtils = new SpUtils(VisitorActivity.this);

        mTitle.setText("访客电话记录");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });
        mBtnVisitorNext.setEnabled(false);
        mEdtVisitorNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    mBtnVisitorNext.setEnabled(false);
                    return;
                }
                mBtnVisitorNext.setEnabled(true);
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));
    }

    @Override
    protected void onStart() {
        mEdtVisitorNum.setText(mSpUtils.getVisitorNbr(MainActivity.count));
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        mSpUtils.clearVisitorNbr(MainActivity.count);
        super.onBackPressed();
    }

    @OnClick({R.id.btn_visitor_skip, R.id.btn_visitor_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_visitor_skip:
                mSpUtils.clearVisitorNbr(MainActivity.count);
                startActivity(new Intent(VisitorActivity.this, RespondentsActivity.class));
                break;
            case R.id.btn_visitor_next:
                String visitorNum = mEdtVisitorNum.getText().toString();
                if (visitorNum.length() > 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSpUtils.saveVisitorNbr(MainActivity.count, visitorNum);
                startActivity(new Intent(VisitorActivity.this, RespondentsActivity.class));
                break;
            default:
                break;
        }
    }
}
