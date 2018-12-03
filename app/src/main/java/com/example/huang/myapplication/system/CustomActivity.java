package com.example.huang.myapplication.system;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.utils.UiSpUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 * @date 2018/1/6 15:16
 * @Describe 自定义业务流程界面，可根据需求选择功能模块
 * @email huanglei1252@qq.com
 */
public class CustomActivity extends BaseActivity {
    private static String TAG = CustomActivity.class.getSimpleName();

    @BindView(R.id.custom_cb_visitor)
    CheckBox mCustomCbVisitor;
    @BindView(R.id.custom_cb_visitor_ID)
    CheckBox mCustomCbVisitorID;
    @BindView(R.id.custom_cb_visitor_phone)
    CheckBox mCustomCbVisitorPhone;
    @BindView(R.id.custom_cb_visitor_resp)
    CheckBox mCustomCbVisitorResp;
    @BindView(R.id.custom_cb_visitor_card)
    CheckBox mCustomCbVisitorCard;
    @BindView(R.id.custom_cb_visitor_picture)
    CheckBox mCustomCbVisitorPicture;
    @BindView(R.id.custom_cb_visitor_plate)
    CheckBox mCustomCbVisitorPlate;

    @BindView(R.id.custom_cb_leave)
    CheckBox mCustomCbLeave;
    @BindView(R.id.custom_cb_leave_picture)
    CheckBox mCustomCbLeavePicture;
    @BindView(R.id.custom_cb_leave_card)
    CheckBox mCustomCbLeaveCard;

    @BindView(R.id.custom_cb_student)
    CheckBox mCustomCbStudent;
    @BindView(R.id.custom_cb_student_picture)
    CheckBox mCustomCbStudentPicture;
    @BindView(R.id.custom_cb_student_card)
    CheckBox mCustomCbStudentCard;
    @BindView(R.id.custom_tv_cancel)
    TextView mCustomTvCancel;
    @BindView(R.id.custom_tv_sure)
    TextView mCustomTvSure;
    private UiSpUtils mUiSpUtils;
    private static ExecutorService mExecutorService;
    private MaterialDialog mSavingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        ButterKnife.bind(this);

        initUtils();
        initView();
        initCheckState();
    }

    private void initUtils() {
        mUiSpUtils = new UiSpUtils(CustomActivity.this);
        //初始化线程池管理
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    }

    private void initView() {
        //设置透明式状态栏
        getWindow().setStatusBarColor(ContextCompat.getColor(CustomActivity.this, R.color.colorPrimary));

        /* 备选框和子选择框勾选状态保持一致 */
        mCustomCbVisitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 当主选择框被选择时，子选择框被全选 */
                boolean checked = mCustomCbVisitor.isChecked();
                mCustomCbVisitorID.setChecked(checked);
                mCustomCbVisitorPhone.setChecked(checked);
                mCustomCbVisitorResp.setChecked(checked);
                mCustomCbVisitorCard.setChecked(checked);
                mCustomCbVisitorPicture.setChecked(checked);
                mCustomCbVisitorPlate.setChecked(checked);
            }
        });
        mCustomCbVisitorID.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCustomCbVisitorPhone.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCustomCbVisitorResp.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCustomCbVisitorCard.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCustomCbVisitorPicture.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCustomCbVisitorPlate.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mCustomCbLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mCustomCbLeave.isChecked();
                mCustomCbLeavePicture.setChecked(isChecked);
                mCustomCbLeaveCard.setChecked(isChecked);
            }
        });
        mCustomCbLeavePicture.setOnCheckedChangeListener(mOnCheckedChangeListener1);
        mCustomCbLeaveCard.setOnCheckedChangeListener(mOnCheckedChangeListener1);

        mCustomCbStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mCustomCbStudent.isChecked();
                mCustomCbStudentPicture.setChecked(isChecked);
                mCustomCbStudentCard.setChecked(isChecked);
            }
        });
        mCustomCbStudentPicture.setOnCheckedChangeListener(mOnCheckedChangeListener2);
        mCustomCbStudentCard.setOnCheckedChangeListener(mOnCheckedChangeListener2);
    }

    private void initCheckState() {
        boolean visitorCardNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_CARD);
        boolean visitorIdNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_ID);
        boolean visitorPhoneNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PHONE);
        boolean visitorPictureNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PICTURE);
        boolean visitorPlateNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PLATE);
        boolean visitorRespNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_RESP);

        boolean leavePictureNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_LEAVE, UiSpUtils.KEY_PICTURE);
        boolean leaveCardNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_LEAVE, UiSpUtils.KEY_CARD);

        boolean studentPictureNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_STUDENT, UiSpUtils.KEY_PICTURE);
        boolean studentCardNeed = mUiSpUtils.getSign(UiSpUtils.SIGN_STUDENT, UiSpUtils.KEY_CARD);

        mCustomCbVisitorID.setChecked(visitorIdNeed);
        mCustomCbVisitorCard.setChecked(visitorCardNeed);
        mCustomCbVisitorPhone.setChecked(visitorPhoneNeed);
        mCustomCbVisitorPicture.setChecked(visitorPictureNeed);
        mCustomCbVisitorPlate.setChecked(visitorPlateNeed);
        mCustomCbVisitorResp.setChecked(visitorRespNeed);

        mCustomCbLeavePicture.setChecked(leavePictureNeed);
        mCustomCbLeaveCard.setChecked(leaveCardNeed);

        mCustomCbStudentPicture.setChecked(studentPictureNeed);
        mCustomCbStudentCard.setChecked(studentCardNeed);
    }

    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                mCustomCbVisitor.setChecked(false);
            } else if (mCustomCbVisitorID.isChecked() && mCustomCbVisitorPhone.isChecked() && mCustomCbVisitorResp.isChecked() && mCustomCbVisitorCard.isChecked()
                    && mCustomCbVisitorPicture.isChecked() && mCustomCbVisitorPlate.isChecked()) {
                mCustomCbVisitor.setChecked(true);
            }
        }
    };
    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener1 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                mCustomCbLeave.setChecked(false);
            } else if (mCustomCbLeavePicture.isChecked() && mCustomCbLeaveCard.isChecked()) {
                mCustomCbLeave.setChecked(true);
            }
        }
    };
    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener2 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                mCustomCbStudent.setChecked(false);
            } else if (mCustomCbStudentPicture.isChecked() && mCustomCbStudentCard.isChecked()) {
                mCustomCbStudent.setChecked(true);
            }
        }
    };

    private MyHandler mHandler = new MyHandler(CustomActivity.this);

    private static class MyHandler extends Handler {
        private WeakReference<CustomActivity> mWeakReference;

        MyHandler(CustomActivity customActivity) {
            if (mWeakReference == null) {
                mWeakReference = new WeakReference<>(customActivity);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            mWeakReference.get().hideSavingDialog();
            if (msg.what == 0) {
                mWeakReference.get().showToast("配置保存成功");
                mWeakReference.get().finish();
            } else if (msg.what == 1) {
                mWeakReference.get().showToast("配置保存失败！");
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 显示正在保存进度对话框
     */
    private void showSavingDialog() {
        mSavingDialog = new MaterialDialog.Builder(this)
                .title(R.string.please_wait)
                .cancelable(false)
                .content(R.string.saving_setting)
                .progress(true, 100)
                .show();
    }

    /**
     * 隐藏正在保存进度对话框
     */
    private void hideSavingDialog() {
        mSavingDialog.dismiss();
    }

    /**
     * 土司提醒用户保存结果
     */
    private Toast mToast;

    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(CustomActivity.this, content, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(content);
            mToast.show();
        }
    }

    @OnClick({R.id.custom_tv_cancel, R.id.custom_tv_sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.custom_tv_cancel:
                finish();
                break;
            case R.id.custom_tv_sure:
                showSavingDialog();
                /*
                 * 开启子线程保存设置数据
                 */
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_CARD, mCustomCbVisitorCard.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_ID, mCustomCbVisitorID.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PHONE, mCustomCbVisitorPhone.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PICTURE, mCustomCbVisitorPicture.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_PLATE, mCustomCbVisitorPlate.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_VISITOR, UiSpUtils.KEY_RESP, mCustomCbVisitorResp.isChecked());

                        mUiSpUtils.saveSign(UiSpUtils.SIGN_LEAVE, UiSpUtils.KEY_PICTURE, mCustomCbLeavePicture.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_LEAVE, UiSpUtils.KEY_CARD, mCustomCbLeaveCard.isChecked());

                        mUiSpUtils.saveSign(UiSpUtils.SIGN_STUDENT, UiSpUtils.KEY_PICTURE, mCustomCbStudentPicture.isChecked());
                        mUiSpUtils.saveSign(UiSpUtils.SIGN_STUDENT, UiSpUtils.KEY_CARD, mCustomCbStudentCard.isChecked());

                        /*
                         * 线程休眠200ms，让进度条展示完全
                         */
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            Log.e(TAG, "savingSetting, fail with >>>>>> " + e.getMessage());
                            //设置保存失败，通知主线程，更新UI
                            mHandler.sendEmptyMessage(1);
                        }

                        //设置保存完毕，通知主线程，更新UI
                        mHandler.sendEmptyMessage(0);
                    }
                });
                break;
            default:
                break;
        }
    }
}
