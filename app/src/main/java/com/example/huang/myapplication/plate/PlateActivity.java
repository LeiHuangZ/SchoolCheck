package com.example.huang.myapplication.plate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.end.EndActivity;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.certificate.CertificateActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlateActivity extends BaseActivity {
    @BindView(R.id.title)
    DrawableTextView title;
    @BindView(R.id.next)
    Button next;
    @BindView(R.id.car_card)
    ImageView carCard;
    @BindView(R.id.take_photo)
    Button takePhoto;
    private boolean FLAG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_plate);
        ButterKnife.bind(this);

        title.setText("车牌拍照");
        title.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });
        next.setEnabled(false);//使“下一步”按钮不可点击

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.scienceBlue));
    }

    @OnClick({R.id.take_photo, R.id.skip, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //开启自定义相机界面，拍摄照片
            case R.id.take_photo:
                if (FLAG) {
                    FLAG = false;
                    PhotoUtils.clearPhoto(this, MainActivity.count, PhotoUtils.KEY_PLATE);
                }
                Intent intent = new Intent(PlateActivity.this, RectPhoto.class);
                intent.putExtra("flag", PhotoUtils.KEY_PLATE);
                startActivityForResult(intent, 0);
                break;
            case R.id.skip:
                startActivity(new Intent(PlateActivity.this, EndActivity.class));
                takePhoto.setText("拍照");
                carCard.setImageResource(R.drawable.car_card);
                FLAG = false;
                next.setEnabled(false);
                PhotoUtils.clearPhoto(this, MainActivity.count, PhotoUtils.KEY_PLATE);
                break;
            case R.id.next:
                startActivity(new Intent(PlateActivity.this, EndActivity.class));
                break;
            default:
                break;
        }
    }

    //按下返回键，清除本页收集的数据
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String licensePlate = PhotoUtils.getPath(this, MainActivity.count, PhotoUtils.KEY_PLATE);
        Bitmap bitmap = BitmapFactory.decodeFile(licensePlate);
        if (bitmap == null) {
            Toast.makeText(this, "取消了拍照", Toast.LENGTH_SHORT).show();
            return;
        }
        carCard.setImageBitmap(bitmap);
        next.setEnabled(true);
        takePhoto.setText("重拍");
        FLAG = true;
    }
}