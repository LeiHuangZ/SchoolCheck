package com.example.huang.myapplication.plate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.utils.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class RectPhoto extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "RectPhoto";
    private boolean isPreview = false;
    private SurfaceView mPreviewSV = null; //预览SurfaceView
    private SurfaceHolder mySurfaceHolder = null;
    private ImageButton mPhotoImgBtn = null;
    private Camera myCamera = null;
    private Bitmap mBitmap = null;
    private AutoFocusCallback myAutoFocusCallback = null;
    public String flag1;//界面标签，存储图片路径时使用
    private Camera.Parameters parameters;
    private SpUtils mSpUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_rect_license);
        //初始化SurfaceView
        mPreviewSV = (SurfaceView) findViewById(R.id.previewSV);
        mPreviewSV.setZOrderOnTop(false);
        mySurfaceHolder = mPreviewSV.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明

        mySurfaceHolder.addCallback(this);
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //自动聚焦变量回调
        myAutoFocusCallback = new AutoFocusCallback() {

            public void onAutoFocus(boolean success, Camera camera) {
                if (success)//success表示对焦成功
                {
                    myCamera.cancelAutoFocus();
                    Log.i(TAG, "myAutoFocusCallback: success...");
                    //myCamera.setOneShotPreviewCallback(null);
                } else {
                    //未对焦成功
                    Log.i(TAG, "myAutoFocusCallback: 失败了...");
                }

            }
        };
        mPhotoImgBtn = (ImageButton) findViewById(R.id.photoImgBtn);
//        mPhotoImgBtn.setLayoutParams(lp);
        mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());
        mPhotoImgBtn.setOnTouchListener(new MyOnTouchListener());

        //获取上个界面传递的标签数据
        flag1 = getIntent().getStringExtra("flag");

        mPreviewSV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                myCamera.cancelAutoFocus();
                doAutoFocus();
            }
        });
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
                    if (!Build.MODEL.equals("KORIDY H30")) {
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        camera.setParameters(parameters);
                    }else{
                        parameters = camera.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(parameters);
                    }
                }
            }
        });
    }


    /*下面三个是SurfaceHolder.Callback创建的回调函数*/
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    // 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
    {
        Log.i(TAG, "SurfaceHolder.Callback:surfaceChanged!");
        myCamera.cancelAutoFocus();
        initCamera();
    }


    public void surfaceCreated(SurfaceHolder holder)
    // SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
    {
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
            Log.i(TAG, "SurfaceHolder.Callback: surfaceCreated!");
        } catch (IOException e) {
            if (null != myCamera) {
                myCamera.release();
                myCamera = null;
            }
            e.printStackTrace();
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder)
    //销毁时被调用
    {
        Log.i(TAG, "SurfaceHolder.Callback：Surface Destroyed");
        if (null != myCamera) {
            myCamera.setPreviewCallback(null); /*在启动PreviewCallback时这个必须在前不然退出出错。
            这里实际上注释掉也没关系*/
            myCamera.stopPreview();
            isPreview = false;
            myCamera.release();
            myCamera = null;
        }
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
//            parameters.setPreviewSize(1280, 720);
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
        public void onShutter() {
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };
    PictureCallback myRawCallback = new PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };
    PictureCallback myJpegCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            if (null != data) {
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                Log.e("===========", "mBitmap: width = " + mBitmap.getWidth() + " -- height = " + mBitmap.getHeight());
                myCamera.stopPreview();
                isPreview = false;
            }
            //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。图片竟然不能旋转了，故这里要旋转下
            Matrix matrix = new Matrix();
            matrix.postRotate((float) 90.0);
            Bitmap rotateBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            Log.e(TAG, "rotateBitmap: width = " + rotateBitmap.getWidth() + " -- height = " + rotateBitmap.getHeight());
            Bitmap bitmap = Bitmap.createBitmap(rotateBitmap,rotateBitmap.getWidth()/6,(rotateBitmap.getHeight()/32)*13,  (rotateBitmap.getWidth()/3)*2,(rotateBitmap.getHeight()/16)*3);
            //保存图片到sdcard
            if (null != bitmap) {
                int options = compressImage(bitmap);
                PhotoUtils.saveJpeg(RectPhoto.this,bitmap,options, flag1);
                mPhotoImgBtn.setEnabled(true);
                /*
                 * 车牌识别，成功则返回，失败要求重新拍摄
                 */
                final ProgressDialog progressDialog = new ProgressDialog(RectPhoto.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("正在识别车牌....");
                progressDialog.show();
                //获取存储的车牌路径
                String filePath = PhotoUtils.getPath(RectPhoto.this, MainActivity.count, PhotoUtils.KEY_PLATE);
                // 车牌识别参数设置
                OcrRequestParams param = new OcrRequestParams();
                // 设置image参数
                param.setImageFile(new File(filePath));
                // 调用车牌识别服务
                OCR.getInstance(RectPhoto.this).recognizeLicensePlate(param, new OnResultListener<OcrResponseResult>() {
                    @Override
                    public void onResult(OcrResponseResult result) {
                        // 调用成功，返回OcrResponseResult对象
                        String jsonRes = result.getJsonRes();
                        Log.v("Huang, RectPhoto", "jsonRes = " + jsonRes);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonRes);
                            JSONObject wordsResultObject = jsonObject.getJSONObject("words_result");
                            String plateNum = wordsResultObject.getString("number");
                            /*
                             * 存储车牌号
                             */
                            if (mSpUtils == null) {
                                mSpUtils = new SpUtils(RectPhoto.this);
                            }
                            mSpUtils.putPlateNum(MainActivity.count, plateNum);
                            progressDialog.cancel();
                            finish();
                        } catch (JSONException e) {
                            Log.e("Huang, RectPhoto", Log.getStackTraceString(e));
                            resetShotState();
                            progressDialog.cancel();
                        }
                    }

                    @Override
                    public void onError(OCRError error) {
                        // 调用失败，返回OCRError对象
                        Log.i("Huang, RectPhoto", "error.getCause = " + Log.getStackTraceString(error));
                        resetShotState();
                        progressDialog.cancel();
                    }
                });
            }
        }
    };

    private int compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>60) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 4;//每次都减少3
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        return options;
    }

    /**
     * 车牌识别失败，重置拍照状态
     */
    private void resetShotState(){
        Toast.makeText(RectPhoto.this, "车牌图片质量差请重新拍摄", Toast.LENGTH_SHORT).show();
        //清除图片
        PhotoUtils.clearPhoto(RectPhoto.this, MainActivity.count, PhotoUtils.KEY_PLATE);
        //清除车牌号
        if (mSpUtils == null){
            mSpUtils = new SpUtils(RectPhoto.this);
        }
        mSpUtils.putPlateNum(MainActivity.count, "");
        //重新开启预览
        myCamera.startPreview();
        mPhotoImgBtn.setEnabled(true);
        isPreview = true;
    }

    //拍照按键的监听
    public class PhotoOnClickListener implements OnClickListener {
        public void onClick(View v) {
            if (isPreview && myCamera != null) {
                myCamera.takePicture(myShutterCallback, null, myJpegCallback);
            }
            mPhotoImgBtn.setEnabled(false);
        }
    }


    /*为了使图片按钮按下和弹起状态不同，采用过滤颜色的方法.按下的时候让图片颜色变淡*/
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
        RectPhoto.this.finish();
        if (myCamera != null) {
            myCamera.cancelAutoFocus();
        }
    }
}
