package com.example.huang.myapplication.certificate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.Environment;
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
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.example.huang.myapplication.R;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.plate.RectPhoto;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.utils.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
    /**
     * 拍照按钮
     */
    @BindView(R.id.photoImgBtn)
    MyView mPhotoImgBtn;
    private boolean isPreview = false;
    private SurfaceHolder mySurfaceHolder = null;
    private Camera myCamera = null;
    private Bitmap mBitmap = null;
    private AutoFocusCallback myAutoFocusCallback = null;
    public String flag1;//界面标签，存储图片路径时使用
    private Camera.Parameters parameters;
    private SpUtils mSpUtils;

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
                /*
                 * 证件识别，成功则返回，失败要求重新拍摄
                 */
                final ProgressDialog progressDialog = new ProgressDialog(LicenseRectPhoto.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("正在识别证件....");
                progressDialog.show();
                //获取存储的车牌路径
                String filePath = PhotoUtils.getPath(LicenseRectPhoto.this, MainActivity.count, flag1);
                if (flag1.equals(PhotoUtils.KEY_LICENSE)) {
                    // 驾驶证识别参数设置
                    OcrRequestParams param = new OcrRequestParams();
                    // 设置image参数
                    param.setImageFile(new File(filePath));
                    // 设置其他参数
                    param.putParam("detect_direction", true);
                    // 调用驾驶证识别服务
                    OCR.getInstance(LicenseRectPhoto.this).recognizeDrivingLicense(param, new OnResultListener<OcrResponseResult>() {
                        @Override
                        public void onResult(OcrResponseResult result) {
                            // 调用成功，返回OcrResponseResult对象
                            String jsonRes = result.getJsonRes();
                            Log.v("Huang, RectPhoto", "jsonRes = " + jsonRes);
                            try {
                                JSONObject jsonObject = new JSONObject(jsonRes);
                                JSONObject wordsResultObject = jsonObject.getJSONObject("words_result");
                                JSONObject nameObject = wordsResultObject.getJSONObject("姓名");
                                String name = nameObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "name = " + name);
                                JSONObject sexObject = wordsResultObject.getJSONObject("性别");
                                String sexStr = sexObject.getString("words");
                                int sex = -1;
                                if (sexStr.equals("男")){
                                    sex = 1;
                                } else if (sexStr.equals("女")){
                                    sex = 2;
                                }
                                Log.v("Huang, LicenseRectPhoto", "sex = " + sex);
                                JSONObject addressObject = wordsResultObject.getJSONObject("住址");
                                String address = addressObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "address = " + address);
                                JSONObject idCardNumObject = wordsResultObject.getJSONObject("证号");
                                String idCardNum = idCardNumObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "idCardNum = " + idCardNum);
                                if (!name.equals("")&&sex!=-1&&!address.equals("")&&!idCardNum.equals("")) {
                                    /*
                                     * 存储姓名，性别，地址，身份证号
                                     */
                                    if (mSpUtils == null) {
                                        mSpUtils = new SpUtils(LicenseRectPhoto.this);
                                    }
                                    mSpUtils.saveName(MainActivity.count, name);
                                    mSpUtils.saveSex(MainActivity.count, sex);
                                    mSpUtils.saveAddress(MainActivity.count, address);
                                    mSpUtils.saveIdentity(MainActivity.count, idCardNum);
                                    progressDialog.cancel();
                                    finish();
                                } else {
                                    resetShotState();
                                    progressDialog.cancel();
                                }
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
                }else {
                    // 身份证识别参数设置
                    IDCardParams param = new IDCardParams();
                    param.setImageFile(new File(filePath));
                    // 设置身份证正面
                    param.setIdCardSide(IDCardParams.ID_CARD_SIDE_FRONT);
                    // 设置方向检测
                    param.setDetectDirection(true);
                    param.setImageFile(new File(filePath));
                    // 调用身份证识别服务
                    OCR.getInstance(LicenseRectPhoto.this).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
                        @Override
                        public void onResult(IDCardResult result) {
                            // 调用成功，返回IDCardResult对象
                            String jsonRes = result.getJsonRes();
                            Log.v("Huang, LicenseRectPhoto", "jsonRes = " + jsonRes);
                            try{
                                JSONObject jsonObject = new JSONObject(jsonRes);
                                JSONObject wordsResultObject = jsonObject.getJSONObject("words_result");
                                JSONObject nameObject = wordsResultObject.getJSONObject("姓名");
                                String name = nameObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "name = " + name);
                                JSONObject sexObject = wordsResultObject.getJSONObject("性别");
                                String sexStr = sexObject.getString("words");
                                int sex = -1;
                                if (sexStr.equals("男")){
                                    sex = 1;
                                } else if (sexStr.equals("女")){
                                    sex = 2;
                                }
                                Log.v("Huang, LicenseRectPhoto", "sex = " + sex);
                                JSONObject addressObject = wordsResultObject.getJSONObject("住址");
                                String address = addressObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "address = " + address);
                                JSONObject idCardNumObject = wordsResultObject.getJSONObject("公民身份号码");
                                String idCardNum = idCardNumObject.getString("words");
                                Log.v("Huang, LicenseRectPhoto", "idCardNum = " + idCardNum);
                                if (!name.equals("")&&sex!=-1&&!address.equals("")&&!idCardNum.equals("")){
                                    /*
                                     * 存储姓名，性别，地址，身份证号
                                     */
                                    if (mSpUtils == null) {
                                        mSpUtils = new SpUtils(LicenseRectPhoto.this);
                                    }
                                    mSpUtils.saveName(MainActivity.count, name);
                                    mSpUtils.saveSex(MainActivity.count, sex);
                                    mSpUtils.saveAddress(MainActivity.count, address);
                                    mSpUtils.saveIdentity(MainActivity.count, idCardNum);
                                    progressDialog.cancel();
                                    finish();
                                } else {
                                    resetShotState();
                                    progressDialog.cancel();
                                }
                            }catch (JSONException e){
                                Log.e("Huang, RectPhoto", Log.getStackTraceString(e));
                                resetShotState();
                                progressDialog.cancel();
                            }
                        }
                        @Override
                        public void onError(OCRError error) {
                            // 调用失败，返回OCRError对象
                            Log.e("Huang, RectPhoto", Log.getStackTraceString(error));
                            resetShotState();
                            progressDialog.cancel();
                        }
                    });

                }
            }
        }
    };

    /**
     * 证件识别失败，重置拍照状态
     */
    private void resetShotState(){
        Toast.makeText(LicenseRectPhoto.this, "证件图片质量差请重新拍摄", Toast.LENGTH_SHORT).show();
        //清除图片
        PhotoUtils.clearPhoto(LicenseRectPhoto.this, MainActivity.count, flag1);
        //清除存储的存储的姓名、性别、地址、身份证号
        if (mSpUtils == null){
            mSpUtils = new SpUtils(LicenseRectPhoto.this);
        }
        mSpUtils.saveName(MainActivity.count, "");
        mSpUtils.saveSex(MainActivity.count, -1);
        mSpUtils.saveAddress(MainActivity.count, "");
        mSpUtils.saveIdentity(MainActivity.count, "");
        //重新开启预览
        myCamera.startPreview();
        mPhotoImgBtn.setEnabled(true);
        isPreview = true;
    }

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
    private OnTouchListener mOnTouchListener = new OnTouchListener() {

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
