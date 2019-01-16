package com.example.huang.myapplication.leave;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.huang.myapplication.DrawableTextView;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.student.StuIDActivity;
import com.example.huang.myapplication.utils.PhotoUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author huang
 */
public class VisitorFaceActivity extends Activity implements SurfaceHolder.Callback {
    private static final String tag = "yan";
    @BindView(R.id.title)
    DrawableTextView mTitle;
    @BindView(R.id.btn_face_next)
    TextView mBtnFaceNext;
    private boolean isPreview = false;
    private SurfaceView mPreviewSV = null; //预览SurfaceView
    private SurfaceHolder mySurfaceHolder = null;
    private TextView mPhotoImgBtn = null;
    private Camera myCamera = null;
    private Bitmap mBitmap = null;
    private AutoFocusCallback myAutoFocusCallback = null;
    private Camera.Parameters parameters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_rect_face);
        ButterKnife.bind(this);

        mTitle.setText("访客拍照");
        mTitle.setDrawableLeftClickListener(new DrawableTextView.DrawableLeftClickListener() {
            @Override
            public void onDrawableLeftClickListener(View view) {
                onBackPressed();
            }
        });

        //初始化SurfaceView
        mPreviewSV = (SurfaceView) findViewById(R.id.previewSV);
        mPreviewSV.setZOrderOnTop(false);
        mySurfaceHolder = mPreviewSV.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明

        mySurfaceHolder.addCallback(this);
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //自动聚焦变量回调
        myAutoFocusCallback = new AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success)//success表示对焦成功
                {
                    myCamera.cancelAutoFocus();
                    Log.i(tag, "myAutoFocusCallback: success...");
                    //myCamera.setOneShotPreviewCallback(null);
                } else {
                    //未对焦成功
                    Log.i(tag, "myAutoFocusCallback: 失败了...");
                }

            }
        };
        mPhotoImgBtn = (TextView) findViewById(R.id.photoImgBtn);
        mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());
        mPhotoImgBtn.setOnTouchListener(new MyOnTouchListener());

        mBtnFaceNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VisitorFaceActivity.this, VisitorIDActivity.class));
                // TODO: 2018/12/26 测试使用NFC，正式使用另外一个不知道什么模块
//                startActivity(new Intent(VisitorFaceActivity.this, VisitorIDActivityBak.class));
            }
        });

        //自动聚焦，监听SurfaceView的点击事件
        mPreviewSV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                myCamera.cancelAutoFocus();
                doAutoFocus();
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dodgerBlue));
    }

    //手动聚焦
    private void doAutoFocus() {
        parameters = myCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        myCamera.setParameters(parameters);
        myCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    if (!"KORIDY H30".equals(Build.MODEL)) {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        camera.setParameters(parameters);
                    } else {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(parameters);
                    }
                }
            }
        });
    }

    /*下面三个是SurfaceHolder.Callback创建的回调函数*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    // 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
    {
        Log.i(tag, "SurfaceHolder.Callback:surfaceChanged!");
        myCamera.cancelAutoFocus();
        initCamera();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder)
    // SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
    {
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
            Log.i(tag, "SurfaceHolder.Callback: surfaceCreated!");
        } catch (IOException e) {
            if (null != myCamera) {
                myCamera.release();
                myCamera = null;
            }
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //初始化相机
    public void initCamera() {
        if (isPreview) {
            myCamera.stopPreview();
        }
        if (null != myCamera) {
            parameters = myCamera.getParameters();

            parameters.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            //设置大小和方向等参数
            parameters.setPictureSize(1280, 960);
//            parameters.setPreviewSize(560, 320);
            //myParam.set("rotation", 90);
            myCamera.setDisplayOrientation(90);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            myCamera.setParameters(parameters);
            myCamera.startPreview();
            myCamera.autoFocus(myAutoFocusCallback);
            isPreview = true;
        }
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback myShutterCallback = new ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        @Override
        public void onShutter() {
            Log.i(tag, "myShutterCallback:onShutter...");
        }
    };
    PictureCallback myRawCallback = new PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(tag, "myRawCallback:onPictureTaken...");

        }
    };
    PictureCallback myJpegCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(tag, "myJpegCallback:onPictureTaken...");
            if (null != data) {
                //data是字节数据，将其解析成位图
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                myCamera.stopPreview();
                isPreview = false;
            }
            Matrix matrix = new Matrix();
            matrix.setRotate((float) 90);
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            //保存图片到sdcard
            if (null != bitmap) {
                PhotoUtils.saveJpeg(VisitorFaceActivity.this, bitmap, PhotoUtils.KEY_VISITOR_FACE);
                mPreviewSV.setBackgroundDrawable(new BitmapDrawable(bitmap));
                mPhotoImgBtn.setText("重拍");
                mPhotoImgBtn.setEnabled(true);
                mBtnFaceNext.setVisibility(View.VISIBLE);
                isPreview = false;
            }
        }
    };

    //拍照按键的监听
    public class PhotoOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == mPhotoImgBtn) {
                if (isPreview && myCamera != null) {
                    myCamera.takePicture(myShutterCallback, null, myJpegCallback);
                    mPhotoImgBtn.setEnabled(false);
                } else if (!isPreview) {
                    myCamera.startPreview();
                    myCamera.autoFocus(myAutoFocusCallback);
                    mPhotoImgBtn.setText("拍照");
                    mPreviewSV.setBackground(null);
                    mBtnFaceNext.setVisibility(View.GONE);
                    isPreview = true;
                    PhotoUtils.clearPhoto(VisitorFaceActivity.this, MainActivity.count, PhotoUtils.KEY_VISITOR_FACE);
                }
            }
        }
    }


    /**为了使图片按钮按下和弹起状态不同，采用过滤颜色的方法.按下的时候让图片颜色变淡*/
    public class MyOnTouchListener implements OnTouchListener {

        public final float[] BT_SELECTED = new float[]
                {2, 0, 0, 0, 2,
                        0, 2, 0, 0, 2,
                        0, 0, 2, 0, 2,
                        0, 0, 0, 1, 0};

        public final float[] BT_NOT_SELECTED = new float[]
                {1, 0, 0, 0, 0,
                        0, 1, 0, 0, 0,
                        0, 0, 1, 0, 0,
                        0, 0, 0, 1, 0};

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());

            }
            return false;
        }

    }

    @Override
    public void onBackPressed()
    //无意中按返回键时要释放内存
    {
        super.onBackPressed();
        VisitorFaceActivity.this.finish();
        if (myCamera != null) {
            myCamera.cancelAutoFocus();
        }
        PhotoUtils.clearPhoto(this, MainActivity.count, PhotoUtils.KEY_VISITOR_FACE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myCamera != null) {
            myCamera.cancelAutoFocus();
            myCamera.stopPreview();
            myCamera.release();
        }
    }
}
