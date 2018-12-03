package com.example.huang.myapplication.end;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.MyTask;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.utils.SocketClientUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * @author huang
 */
public class EndActivity extends BaseActivity {

    @BindView(R.id.title)
    DrawableTextView mTitle;
    /**
     * 记录从哪一个Activity跳转来，0 --> Respondents，1 --> StuID
     */
    private int flag;
    private SpUtils mSpUtils;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        ButterKnife.bind(this);
        mSpUtils = new SpUtils(this);

        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                finish();
            }
        });
        mTitle.setText("登记结束");
        flag = getIntent().getFlags();
        Log.i("==================", "onCreate: flag = " + flag);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));

//        EventBus.getDefault().register(this);

    }

    @OnClick({R.id.btn_end_sure, R.id.btn_end_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_end_sure:
                //mProgressDialog = new ProgressDialog(this);
                //mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // 设置ProgressDialog 标题
                //mProgressDialog.setTitle("正在上传...");
                // 设置ProgressDialog 是否可以按退回按键取消
                //mProgressDialog.setCancelable(false);
                //mProgressDialog.show();
                int visitorLeave = 2;
                if (flag == 0) {
                    MyTask task = new MyTask(EndActivity.this.getApplicationContext());
                    MainActivity.mList.add(task);
                    task.execute(0);
                } else if (flag == 1) {
                    MyTask task = new MyTask(EndActivity.this.getApplicationContext(), PhotoUtils.getPath(EndActivity.this.getApplicationContext(), MainActivity.count, PhotoUtils.KEY_FACE));
                    MainActivity.mList.add(task);
                    task.execute(2);
                }else if (flag == visitorLeave){
                    MyTask task = new MyTask(EndActivity.this.getApplicationContext(), PhotoUtils.getPath(EndActivity.this.getApplicationContext(), MainActivity.count, PhotoUtils.KEY_VISITOR_FACE));
                    MainActivity.mList.add(task);
                    task.execute(1);
                }
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.btn_end_cancel:
                PhotoUtils.deleteDirectory(this, "/storage/sdcard0/tempPhoto/");
                mSpUtils.clearAll();
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
    }

//    @Subscribe(threadMode = ThreadMode.MainThread)
//    public void onEventBus(String flag) {
//        String success = "success";
//        String fail = "fail";
//        if (success.equals(flag)) {
//            mProgressDialog.dismiss();
//            Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show();
//            PhotoUtils.deleteDirectory(this, "/storage/sdcard0/tempPhoto/");
//            mSpUtils.clearAll();
//            startActivity(new Intent(this, MainActivity.class));
//        } else if (fail.equals(flag)) {
//            mProgressDialog.dismiss();
//            Toast.makeText(this, "上传失败，请检查网络和IP地址重试", Toast.LENGTH_SHORT).show();
//        }
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//    }
}
