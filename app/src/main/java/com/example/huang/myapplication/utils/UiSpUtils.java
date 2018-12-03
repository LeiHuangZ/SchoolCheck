package com.example.huang.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author huang
 * @package com.example.huang.myapplication.utils
 * @fileName UiSpUtils
 * @email huanglei1252@qq.com
 * @date 2018/1/6  9:34
 * @Describe 保存等级流程定制结果
 */

public class UiSpUtils {

    private final SharedPreferences mVisitorSharedPreferences;
    private final SharedPreferences mLeaveSharedPreferences;
    private final SharedPreferences mStudentSharedPreferences;

    /**
     * SP存储时区分流程类别需要的标记
     * 0-->访客,2-->访客离校,3-->学生离校
     */
    public static int SIGN_VISITOR = 0;
    public static int SIGN_LEAVE = 1;
    public static int SIGN_STUDENT = 2;

    /**
     * SP存储时使用到的各个KEY值
     */
    public static String KEY_ID = "id";
    public static String KEY_PHONE = "phone";
    public static String KEY_RESP = "resp";
    public static String KEY_CARD = "card";
    public static String KEY_PICTURE = "picture";
    public static String KEY_PLATE = "plate";

    public UiSpUtils(Context context) {
        //0
        mVisitorSharedPreferences = context.getSharedPreferences("VISITOR", Context.MODE_PRIVATE);
        //1
        mLeaveSharedPreferences = context.getSharedPreferences("LEAVE", Context.MODE_PRIVATE);
        //2
        mStudentSharedPreferences = context.getSharedPreferences("STUDENT", Context.MODE_PRIVATE);
    }

    /**
     * 获取存储的界面标记值
     *
     * @param spSign 0-->访客信息，1-->访客离校, 2-->学生离校
     * @param key    key值
     * @return 返回的界面标记值，是否被选择
     */
    public boolean getSign(int spSign, String key) {
        switch (spSign) {
            case 0:
                return mVisitorSharedPreferences.getBoolean(key, true);
            case 1:
                return mLeaveSharedPreferences.getBoolean(key, true);
            case 2:
                return mStudentSharedPreferences.getBoolean(key, true);
            default:
                return false;
        }
    }

    public void saveSign(int spSign, String key, boolean sign) {
        switch (spSign) {
            case 0:
                mVisitorSharedPreferences.edit().putBoolean(key, sign).apply();
                break;
            case 1:
                mLeaveSharedPreferences.edit().putBoolean(key, sign).apply();
                break;
            case 2:
                mStudentSharedPreferences.edit().putBoolean(key, sign).apply();
                break;
            default:
                break;
        }
    }
}
