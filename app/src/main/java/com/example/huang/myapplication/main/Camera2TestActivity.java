package com.example.huang.myapplication.main;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.example.huang.myapplication.R;

import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 测试类，各种测试，请忽略
 *
 * @author huang
 * @date 2017/10/26
 */

public class Camera2TestActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private static String TAG = "Camera2TestActivity";

    @BindView(R.id.test_tuv_preview)
    TextureView mTestTuvPreview;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idread_improve);
        ButterKnife.bind(this);

        mHandlerThread = new HandlerThread("CAMERA2");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //1.监听Surface的状态
        mTestTuvPreview.setSurfaceTextureListener(this);
    }

    //==========================================SurfaceTextureListener四个实现方法============================================================//
    /**当Surface可用时*/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //获取CameraManager对象
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            //获取可用的Camera的ID列表
            String[] cameraIdList = cameraManager.getCameraIdList();
            //获取指定ID的Camera
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdList[0]);
            //在这里可以通过CameraCharacteristics设置Camera功能，当然需要相机支持才行
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            //2.监听相机设备的状态CameraDeviceStateListener
            cameraManager.openCamera(cameraIdList[0], mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**当Surface尺寸改变时*/
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }
    /**当Surface销毁时*/
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }
    /**当Surface更新时*/
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**===================================================CameraDevice.StateCallback的方法实现============================================================*/
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        /**Camera已经打开*/
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //3.开启Preview
            startPreview(camera);
        }
        /**Camera关闭*/
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        }
        /**Camera状态错误*/
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
        }
    };

    /** 图片读取器,保存图片？*/
    private ImageReader mImageReader = ImageReader.newInstance(1280,720, ImageFormat.JPEG,2);

    private void startPreview(CameraDevice camera) {
        SurfaceTexture surfaceTexture = mTestTuvPreview.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(1280,720);
        Surface surface = new Surface(surfaceTexture);

        //4.利用CameraDevice创建CaptureRequest,captureRequest代表一次捕获请求，
        // 用于描述捕获图片的各种参数设置。比如对焦模式，曝光模式...等，程序对照片所做的各种控制，都通过CaptureRequest参数来进行设置
        try {
            mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mBuilder.addTarget(surface);
            //5.利用CameraDevice创建CaptureSession
            camera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**=============================================CameraCaptureSession.StateCallback会话状态监听回掉==========================================================*/
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        /**Session配置完成*/
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraCaptureSession = session;
            //开启预览
            try {
                session.setRepeatingRequest(mBuilder.build(),mSessionCaptureCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        /**配置失败*/
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    /**================================================CameraCaptureSession.CaptureCallback照片生成监听回掉=============================================*/
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        /**Capture生成过程中，可以对Capture进行操作*/
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }
        /**Capture生成*/
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i(TAG, "onCaptureCompleted: !!!!");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraDevice.close();
        }
    }
}
