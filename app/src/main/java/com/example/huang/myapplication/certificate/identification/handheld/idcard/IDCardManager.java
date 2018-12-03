package com.example.huang.myapplication.certificate.identification.handheld.idcard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.example.testdemo.ZstBarcodeCtrl;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.nidfpsensor.NIDFPSensor;

import java.util.HashMap;
import java.util.Map;

public class IDCardManager {
	private static String TAG = "IDCardManager";
	private IDCardReader idCardReader;
	private static final int VID = 6997;    //zkteco device VID  6997
	private static final int PID = 772;    //NIDFPSensor PID 根据实际设置
	private static final int _VID = 1024;
	private static final int _PID = 50010;
	private final String ACTION_USB_PERMISSION_1 = "com.zkteco.idfprdemo.USB_PERMISSION_1";
	private final String ACTION_USB_PERMISSION_2 = "com.zkteco.idfprdemo.USB_PERMISSION_2";
	private Context mContext;

	private final int DEVICES_USB = 0x01;
	private final int DEVICES_SERIAL = 0x02;
	private int mDevices = DEVICES_SERIAL;

	private final String idSerialName = "/dev/ttyHSL1";
	private final int idbaudrate = 115200;

	private byte[] mFingerFirst = null;
	private byte[] mFingerSecond = null;
	private byte[] fingerBuffer;
	public boolean isRunFirst = true;
	public boolean isVerifyFinger = false;
	public boolean verifyFingerSuccess = false;
	private NIDFPSensor mNIDFPSensor = null;
	private byte[] mBufImage;
	private boolean isOpenIdDevice = false;
	private boolean isRegisterReceiver = false;

	public IDCardManager(Context context){
		mContext = context;
		ZstBarcodeCtrl.getInstance().Set_WD220B_OTG(1);
		startIDCardReader();
	}

	private void startIDCardReader() {
		Map params = new HashMap();
		if(mDevices == DEVICES_USB) {
			params.put(ParameterHelper.PARAM_KEY_VID, _VID);
			params.put(ParameterHelper.PARAM_KEY_PID, _PID);
			idCardReader = IDCardReaderFactory.createIDCardReader(mContext, TransportType.USB, params);
			RegisterReceiver();
		}else{
			params.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, idSerialName);
			params.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, idbaudrate);
			idCardReader = IDCardReaderFactory.createIDCardReader(mContext, TransportType.SERIALPORT, params);
			openIDCard();
		}
	}

	private void RegisterReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION_1);
		filter.addAction(ACTION_USB_PERMISSION_2);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		mContext.registerReceiver(mUsbReceiver, filter);
		isRegisterReceiver = true;
	}

	private void openIDCard() {
		if (isOpenIdDevice) {
			return;
		}
		try {
			idCardReader.open(0);
			isOpenIdDevice = true;
			return;
		} catch (IDCardReaderException e) {
			Toast.makeText(mContext, "open idCard fail", Toast.LENGTH_SHORT).show();
		}
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION_1.equals(action)) {
				synchronized (this) {
					UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openIDCard();
					} else {
						Toast.makeText(mContext, "USB未授权", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};

	public void close() {
		if (isOpenIdDevice) {
			try {
				idCardReader.close(0);
			} catch (IDCardReaderException e) {
				e.printStackTrace();
			}
			isOpenIdDevice = false;
		}
		if(mDevices == DEVICES_USB) {
			if (isRegisterReceiver) {
				mContext.unregisterReceiver(mUsbReceiver);
				isRegisterReceiver = false;
			}
		}
		ZstBarcodeCtrl.getInstance().Set_WD220B_OTG(0);
	}
	
	public boolean findCard() {
		if(!isOpenIdDevice)
			return false;
		try {
			return idCardReader.findCard(0);
		} catch (IDCardReaderException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean selectCard() {
		if(!isOpenIdDevice)
			return false;
		try {
			return idCardReader.selectCard(0);
		} catch (IDCardReaderException e) {
			e.printStackTrace();
		}
		return false;
	}

	public IDCardInfo readCard(long timeout) {
		if(!isOpenIdDevice)
			return null;
		final IDCardInfo idCardInfo = new IDCardInfo();
		long nTickSet = System.currentTimeMillis();
		while (System.currentTimeMillis() - nTickSet < timeout) {
			try {
				if(idCardReader.readCard(0, 0, idCardInfo) == true)
					return idCardInfo;
			} catch (IDCardReaderException e) {
			}
		}
		return null;
	}

	public Bitmap getBitmap(final IDCardInfo idCardInfo) {
		if (idCardInfo.getPhoto() != null) {
			byte[] buf = new byte[WLTService.imgLength];
			if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
				final Bitmap bitmap = IDPhotoHelper.Bgr2Bitmap(buf);
				if (null != bitmap) {
					return bitmap;
				}
			}
		}
		return null;
	}
}
