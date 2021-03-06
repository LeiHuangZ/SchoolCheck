package com.example.huang.myapplication.certificate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.plate.PlateActivity;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.visitor.VisitorActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class LicenseActivity extends BaseActivity {
    @BindView(R.id.title)
    DrawableTextView title;
    @BindView(R.id.next)
    Button next;
    @BindView(R.id.take_photo)
    Button takePhoto;
    @BindView(R.id.img_tips_license)
    ImageView mImgTipsLicense;
    private static boolean FLAG = false;
    /**存储图片的KEY，通过判断界面类型，赋予不同的值*/
    private static String KEY;
    private SpUtils mSpUtils;
    private int mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);
        //获取从上一个界面传入的标签
        mFlag = getIntent().getFlags();
        int passport = 2;
        int carLicense = 3;

        if (mFlag == passport){
            mImgTipsLicense.setImageDrawable(getResources().getDrawable(R.drawable.passport));
            title.setText("护照拍照");
            KEY = PhotoUtils.KEY_PASSPORT;
        }else if (mFlag == 0){
            title.setText("驾照拍照");
            KEY = PhotoUtils.KEY_LICENSE;
        }else if (mFlag == 1){
            mImgTipsLicense.setImageDrawable(getResources().getDrawable(R.drawable.residence));
            title.setText("居住证拍照");
            KEY = PhotoUtils.KEY_RESIDENCE;
        }else if (mFlag == carLicense){
            title.setText("行驶证拍照");
            KEY = PhotoUtils.KEY_CAR_LICENSE;
        }
        title.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });
        //使“下一步”按钮不可点击
        next.setEnabled(false);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));
        Log.i("==============", "onCreate: " + KEY);
    }

    @OnClick({R.id.take_photo, R.id.next, R.id.cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //开启自定义相机界面，拍摄照片
            case R.id.take_photo:
                if (FLAG){
                    FLAG = false;
                    clearData();
                }
                Intent intent = new Intent(LicenseActivity.this, LicenseRectPhoto.class);
                intent.putExtra("flag", KEY);
                startActivityForResult(intent, 0);
                break;
            case R.id.cancel:
                //取消操作，返回主界面
                startActivity(new Intent(LicenseActivity.this, MainActivity.class));
                clearData();
                break;
            case R.id.next:
                startActivity(new Intent(LicenseActivity.this, VisitorActivity.class));
                break;
            default:
                break;
        }
    }

    //按下返回键，清除本页收集的数据
    @Override
    public void onBackPressed() {
        clearData();
        finish();
    }

    /**
     * 清除存储的数据
     */
    private void clearData(){
        PhotoUtils.clearPhoto(this, MainActivity.count, KEY);
        if (mSpUtils == null){
            mSpUtils = new SpUtils(LicenseActivity.this);
        }
        //删除存储的上次存储的姓名、性别、地址、身份证号
        mSpUtils.saveName(MainActivity.count, "");
        mSpUtils.saveSex(MainActivity.count, -1);
        mSpUtils.saveAddress(MainActivity.count, "");
        mSpUtils.saveIdentity(MainActivity.count, "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        // 2018/12/19 测试,正式删除 获取存储身份信息
//        if (mSpUtils == null) {
//            mSpUtils = new SpUtils(this);
//        }
//        String name = mSpUtils.getName(MainActivity.count);
//        int sex = mSpUtils.getSex(MainActivity.count);
//        String address = mSpUtils.getAddress(MainActivity.count);
//        String identity = mSpUtils.getIdentity(MainActivity.count);
//        Log.v("Huang, PlateActivity", "saved name = " + name);
//        Log.v("Huang, PlateActivity", "saved sex = " + sex);
//        Log.v("Huang, PlateActivity", "saved address = " + address);
//        Log.v("Huang, PlateActivity", "saved identity = " + identity);
        String license = PhotoUtils.getPath(this, MainActivity.count, KEY);
        Bitmap bitmap = BitmapFactory.decodeFile(license);
        if (bitmap == null) {
            if (mFlag == 1){
                mImgTipsLicense.setImageDrawable(getDrawable(R.drawable.residence));
            } else {
                mImgTipsLicense.setImageDrawable(getDrawable(R.drawable.licence));
            }
            Toast.makeText(this, "取消了拍照", Toast.LENGTH_SHORT).show();
            return;
        }
        mImgTipsLicense.setImageBitmap(bitmap);
        next.setEnabled(true);
        takePhoto.setText("重拍");
        FLAG = true;
    }
}