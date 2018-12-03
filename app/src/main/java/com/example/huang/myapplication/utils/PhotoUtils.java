package com.example.huang.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.huang.myapplication.main.MainActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片工具类，存储图片路径，删除图片
 *
 * @author huang
 * @date 2017/10/18
 */

public class PhotoUtils {
    private static String TAG = PhotoUtils.class.getSimpleName();
    public static String KEY_PLATE = "PLATE";
    public static String KEY_LICENSE = "LICENSE";
    public static String KEY_PASSPORT = "PASSPORT";
    public static String KEY_RESIDENCE = "RESIDENCE";
    public static String KEY_CAR_LICENSE = "CAR_LICENSE";
    public static String KEY_IDENTIFY = "IDENTIFY";
    public static String KEY_FACE = "FACE";
    public static String KEY_VISITOR_FACE = "VISITOR_FACE";

    private static void savePath(Context context, long num, String key, String path) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PHOTO"+num, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, path);
        edit.apply();
    }

    public static String getPath(Context context, long num, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PHOTO"+num, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static void clearPhoto(Context context, long num, String key) {
        String path = getPath(context, num, key);
        boolean isDeleted = new File(path).delete();
        Log.i(TAG, "clearPhoto: " + key + " = " + isDeleted);
    }

    /**
     * 删除缓存图片的文件夹以及目录下的文件,并且删除存储的图片路径
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     * "/storage/sdcard0/tempPhoto/"
     */
    public static boolean deleteDirectory(Context context, String filePath) {
        boolean flag ;
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (File file : files) {
            if (file.isFile()) {
                //删除子文件
                flag = new File(file.getAbsolutePath()).delete();
                if (!flag) {break;}
            } else {
                //删除子目录
                flag = deleteDirectory(context, file.getAbsolutePath());
                if (!flag) {break;}
            }
        }
        //删除存储的图片路径
        SharedPreferences sharedPreferences = context.getSharedPreferences("PHOTO", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
        if (!flag) {return false;}
        //删除当前空目录
        return dirFile.delete();
    }

    /**给定一个Bitmap，进行保存*/
    public static void saveJpeg(Context context, Bitmap bm, String flag1) {
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/tempPhoto/";
        File folder = new File(savePath);
        //如果文件夹不存在则创建
        if (!folder.exists())
        {
            boolean makeDir = folder.mkdir();
            Log.i(TAG, "saveJpeg: makeDir = " + makeDir);
        }
        long dataTake = System.currentTimeMillis();
        String jpegName = savePath + dataTake + ".jpg";
        Log.i(TAG, "savePath: jpegName = " + jpegName);
        //File jpegFile = new File(jpegName);
        try {
            FileOutputStream fos = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            //如果需要改变大小(默认的是宽960×高1280),如改成宽600×高800
            bm.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            Log.i(TAG, "saveJpeg: bitmap.width = " + bm.getWidth()+", " + "bitmap.height = " +bm.getHeight());
            bos.flush();
            bos.close();
            //将图片路径存储至SP
            PhotoUtils.savePath(context, MainActivity.count, flag1, jpegName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
