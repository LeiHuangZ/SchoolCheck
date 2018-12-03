package com.example.huang.myapplication.certificate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.huang.myapplication.R;
import com.example.huang.myapplication.utils.PhotoUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 横屏拍照界面
 *
 * @author huang
 */
public class LicenseRectPhoto extends Activity implements SurfaceHolder.Callback {
    //private static String TAG = LicenseRectPhoto.class.getSimpleName();
    @BindView(R.id.license_photo_tv_tip)
    TextView mLicensePhotoTvTip;
    /** 拍照按钮 */
    @BindView(R.id.photoImgBtn)
    MyView mPhotoImgBtn;
    private boolean isPreview = false;
    private SurfaceHolder mySurfaceHolder = null;
    private Camera myCamera = null;
    private Bitmap mBitmap = null;
    private AutoFocusCallback myAutoFocusCallback = null;
    public String flag1;//界面标签，存储图片路径时使用
    private Camera.Parameters parameters;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_rect_photo);
        ButterKnife.bind(this);
        //初始化SurfaceView
        /*预览SurfaceView*/
        SurfaceView previewSV = (SurfaceView) findViewById(R.id.previewSV);
        previewSV.setZOrderOnTop(false);
        mySurfaceHolder = previewSV.getHolder();
        //translucent半透明 transparent透明
        mySurfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        mySurfaceHolder.addCallback(this);
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //自动聚焦变量回调
        myAutoFocusCallback = new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                //success表示对焦成功
                if (success) {
                    myCamera.cancelAutoFocus();
                }
            }
        };
        mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());
        mPhotoImgBtn.setOnTouchListener(mOnTouchListener);

        //获取上个界面传递的标签数据
        flag1 = getIntent().getStringExtra("flag");

        previewSV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                myCamera.cancelAutoFocus();
                doAutoFocus();
            }
        });

        Animation mAnimationRight = AnimationUtils.loadAnimation(this, R.anim.rotate_right);
        mAnimationRight.setFillAfter(true);
        mLicensePhotoTvTip.setAnimation(mAnimationRight);
    }

    /**
     * 手动聚焦
     */
    private void doAutoFocus() {
        parameters = myCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        myCamera.setParameters(parameters);
        myCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    String model = "KORIDY H30";
                    if (!model.equals(Build.MODEL)) {
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

    //---------下面三个是SurfaceHolder.Callback创建的回调函数------------------//

    /**
     * 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        myCamera.cancelAutoFocus();
        initCamera();
    }

    /**
     * SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
        } catch (IOException e) {
            if (null != myCamera) {
                myCamera.release();
                myCamera = null;
            }
            e.printStackTrace();
        }
    }

    /**
     * SurfaceView被销毁时，该方法被调用
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != myCamera) {
            myCamera.setPreviewCallback(null); /*在启动PreviewCallback时这个必须在前不然退出出错。
            这里实际上注释掉也没关系*/
            myCamera.stopPreview();
            isPreview = false;
            myCamera.release();
            myCamera = null;
        }
    }

    /**
     * 初始化相机
     */
    public void initCamera() {
        if (isPreview) {
            myCamera.stopPreview();
        }
        if (null != myCamera) {
            parameters = myCamera.getParameters();
            //查询屏幕的宽和高
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            //设置拍照后存储的图片格式
            parameters.setPictureFormat(PixelFormat.JPEG);
            //设置大小和方向等参数
            parameters.setPictureSize(height, width);
//            parameters.setPreviewSize(960, 720);
            myCamera.setDisplayOrientation(90);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            myCamera.setParameters(parameters);
            myCamera.startPreview();
            myCamera.autoFocus(myAutoFocusCallback);
            isPreview = true;
        }
    }

    /**
     * 快门按下的回掉
     */
    ShutterCallback myShutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };

    /**
     * 对jpeg图像数据的回调,最重要的一个回调
     */
    PictureCallback myJpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //data是字节数据，将其解析成位图
            if (null != data) {
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.e("===========", "rotateBitmap: width = " + mBitmap.getWidth() + " -- height = " + mBitmap.getHeight());
                myCamera.stopPreview();
                isPreview = false;
            }
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, (mBitmap.getWidth() / 4), (mBitmap.getHeight() / 6), (mBitmap.getWidth() / 2), (mBitmap.getHeight() / 3) * 2);
            //保存图片到sdcard
            if (null != bitmap) {
                PhotoUtils.saveJpeg(LicenseRectPhoto.this, bitmap, flag1);
                finish();
            }
        }
    };

    /**
     * 拍照按键的监听
     */
    public class PhotoOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (isPreview && myCamera != null) {
                myCamera.takePicture(myShutterCallback, null, myJpegCallback);
            }
            mPhotoImgBtn.setEnabled(false);
        }
    }


    /**
     * 为了使图片按钮按下和弹起状态不同，采用过滤颜色的方法.按下的时候让图片颜色变淡
     */
    private OnTouchListener mOnTouchListener = new OnTouchListener(){

        final float[] BT_SELECTED = new float[]{2, 0, 0, 0, 2, 0, 2, 0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 1, 0};

        final float[] BT_NOT_SELECTED = new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
                v.performClick();
            }
            return false;
        }
    };

    /**
     * 无意中按返回键时要释放内存
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LicenseRectPhoto.this.finish();
        if (myCamera != null) {
            myCamera.cancelAutoFocus();
        }
    }
}
