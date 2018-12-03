package com.example.testdemo;

/**
 * Created by Administrator on 2017/9/1.
 */

public class ZstBarcodeCtrl {

    private static ZstBarcodeCtrl mZstBarcodeCtrl = new ZstBarcodeCtrl();

    public ZstBarcodeCtrl(){ }

    public static ZstBarcodeCtrl getInstance(){
        return mZstBarcodeCtrl;
    }


    public native void Setgpio42(int state);
    public native void Set_WD220B_OTG(int state);
    static {
        System.loadLibrary("ZstOtgCtrl");
    }

}