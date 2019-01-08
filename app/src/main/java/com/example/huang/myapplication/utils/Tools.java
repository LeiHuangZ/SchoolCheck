package com.example.huang.myapplication.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.example.huang.myapplication.main.MainActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Tools {

    //byte[] --> hex
    public static String Bytes2HexString(byte[] b, int size) {
        String ret = "";
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    //hex --> byte[]
    public static byte[] HexString2Bytes(String src) {
        int len = src.length() / 2;
        byte[] ret = new byte[len];
        byte[] tmp = src.getBytes();

        for (int i = 0; i < len; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    //byte[] --> int
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 25) & 0xFF000000);
        return addr;

    }

    //int --> byte[]
    public static byte[] intToByte(int i) {
        byte[] abyte0 = new byte[4];
        abyte0[0] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[2] = (byte) ((0xff0000 & i) >> 16);
        abyte0[3] = (byte) ((0xff000000 & i) >> 24);
        return abyte0;
    }

    /* ======================================================卡号转换算法================================================================== */

    public static final int EIGHT_POSITIVE = 1;
    public static final int TEN_POSITIVE = 2;
    public static final int EIGHT_REVERSE = 3;
    public static final int TEN_REVERSE = 4;

    /**
     * 转换函数，按类型输出4种10进制字符串
     *
     * @param nType    1 --> 8位正序十进制字符串, 2 --> 10位正序十进制字符串, 3 --> 8位反序十进制字符串, 4 --> 10位反序十进制字符串
     * @param strCardno 待转换卡号
     * @return 按类型输出4种10进制字符串
     */
    public static String convertCarno(int nType, CharSequence strCardno) {
        String result = "";
        if (nType < 0 || strCardno.length() < 8) {
            return result;
        }
        long id;
        String str = "";
        switch (nType) {
            case EIGHT_POSITIVE:
                // 8位正序
                str += strCardno;
                id = hex2dec(str);
                id = id % (256 * 256) + (((int) (id / (256 * 256))) % 256) * 100000;
                DecimalFormat decimalFormat = new DecimalFormat("00000000");
                result = decimalFormat.format(id);
                break;
            case TEN_POSITIVE:
                //10 正
                str += strCardno;
                id = hex2dec(str);
                DecimalFormat decimalFormat2 = new DecimalFormat("0000000000");
                result = decimalFormat2.format(id);
            break;
            case EIGHT_REVERSE:
                // 8 反
                str += strCardno.charAt(6);
                str += strCardno.charAt(7);
                str += strCardno.charAt(4);
                str += strCardno.charAt(5);
                str += strCardno.charAt(2);
                str += strCardno.charAt(3);
                str += strCardno.charAt(0);
                str += strCardno.charAt(1);
                id = hex2dec(str);
                id = id % (256 * 256) + (((int)(id / (256 * 256))) %256) *100000;
                DecimalFormat decimalFormat3 = new DecimalFormat("00000000");
                result = decimalFormat3.format(id);
            break;
            case TEN_REVERSE:
                // 10 反
                str += strCardno.charAt(6);
                str += strCardno.charAt(7);
                str += strCardno.charAt(4);
                str += strCardno.charAt(5);
                str += strCardno.charAt(2);
                str += strCardno.charAt(3);
                str += strCardno.charAt(0);
                str += strCardno.charAt(1);
                id = hex2dec(str);
                DecimalFormat decimalFormat4 = new DecimalFormat("0000000000");
                result = decimalFormat4.format(id);
            break;
            default:
                break;
        }
        return result;
    }

    /**
     * 将字符转换为数值
     */
    private static int c2i(char ch) {
        // 如果是数字，则用数字的ASCII码减去48, 如果ch = '2' ,则 '2' - 48 = 2
        if (Character.isDigit(ch)) {
            return ch - 48;
        }

        // 如果是字母，但不是A~F,a~f则返回
        if (ch < 'A' || (ch > 'F' && ch < 'a') || ch > 'z') {
            return -1;
        }

        // 如果是大写字母，则用数字的ASCII码减去55, 如果ch = 'A' ,则 'A' - 55 = 10
        // 如果是小写字母，则用数字的ASCII码减去87, 如果ch = 'a' ,则 'a' - 87 = 10
        if (Character.isLowerCase(ch) || Character.isUpperCase(ch)) {
            return Character.isUpperCase(ch) ? ch - 55 : ch - 87;
        }

        return -1;
    }

    /**
     * 功能：将十六进制字符串转换为长整型(long)数值
     */
    private static long hex2dec(CharSequence hex) {
        int len;
        long num = 0;
        long temp;
        int bits;
        int i;

        // 此例中 hex = "1de" 长度为3, hex是main函数传递的
        len = hex.length();

        for (i = 0, temp = 0; i < len; i++, temp = 0) {
            // 第一次：i=0, *(hex + i) = *(hex + 0) = '1', 即temp = 1
            // 第二次：i=1, *(hex + i) = *(hex + 1) = 'd', 即temp = 13
            // 第三次：i=2, *(hex + i) = *(hex + 2) = 'd', 即temp = 14
            temp = c2i(hex.charAt(i));
            // 总共3位，一个16进制位用 4 bit保存
            // 第一次：'1'为最高位，所以temp左移 (len - i -1) * 4 = 2 * 4 = 8 位
            // 第二次：'d'为次高位，所以temp左移 (len - i -1) * 4 = 1 * 4 = 4 位
            // 第三次：'e'为最低位，所以temp左移 (len - i -1) * 4 = 0 * 4 = 0 位
            bits = (len - i - 1) * 4;
            temp = temp << bits;

            // 此处也可以用 num += temp;进行累加
            num = num | temp;
        }

        // 返回结果
        return num;
    }

    //==============================================格式化时间========================================================//
    /**
     * 格式化时间
     * @param time 时间毫秒数
     * @return 格式化完成的时间字符串
     */
    public static String formateTime(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * 发送广播使系统刷新指定文件，解决创建的文件无妨被访问
     * @param context 上下文
     * @param filePath 需要刷新的文件
     */
    public static void notifySystemToScan(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.getApplicationContext().sendBroadcast(intent);
    }
}
