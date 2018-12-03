package com.example.huang.myapplication.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.huang.myapplication.R;
import com.example.huang.myapplication.greendao.PersonDaoManager;
import com.example.huang.myapplication.main.MainActivity;
import com.zsy.words.bean.Person;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import okio.Buffer;

/**
 * @author Administrator
 * @date 2017/12/1
 * 上传任务，异步任务会串行执行，效果相当于队列
 */

public class MyTask extends AsyncTask<Integer, Integer, String> {

    private static String TAG = MyTask.class.getSimpleName();

    private WeakReference<Context> mReference;
    /**
     * 图片路径，访客或者学生离校的人脸图片的路径
     */
    private String mFilePath;
    private static Socket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private static String mIP;
    private long mNum;
    private Context mContext;

    /**
     * 访客或学生离校时，使用此构造函数
     *
     * @param context  传入上下文，使用弱引用，避免内存泄漏
     * @param filePath 传入图片路径，访客或者学生
     */
    public MyTask(Context context, final String filePath) {
        mNum = MainActivity.count;
        Log.i(TAG, "mNum:  == " + mNum);
        mReference = new WeakReference<>(context);
        mContext = context;
        mFilePath = filePath;
    }

    /**
     * 发送访客信息时，使用此构造函数
     *
     * @param context 传入上下文，使用弱引用，避免内存泄漏
     */
    public MyTask(Context context) {
        mNum = MainActivity.count;
        Log.i(TAG, "MyTask:  == " + mNum);
        mReference = new WeakReference<>(context);
        mContext = context;
    }

    @Override
    protected String doInBackground(Integer[] integer) {
        mIP = new SpUtils(mReference.get()).getIP();
        //根据传入的flag值，判断需要上传的数据类型，0-->访客来访，1-->访客离校，2-->学生离校
        Integer flag = integer[0];
        switch (flag) {
            case 0:
                sendVisitor(mReference.get());
                break;
            case 1:
                sendVisitorLeave(mReference.get(), mFilePath);
                break;
            case 2:
                sendStu(mReference.get(), mFilePath);
                break;
            case 3:
                sendUpdate();
                break;
            default:
                getConnect();
                break;
        }
        return null;
    }

    /***
     * 发送获取升级信息
     */
    private void sendUpdate() {
        try {
            SpUtils spUtils = new SpUtils(mReference.get());
            mIP = spUtils.getIP();
            mSocket = new Socket();
            Log.i(TAG, "sendUpdate, mIP" + mIP);
            SocketAddress address = new InetSocketAddress(mIP, 8100);
            mSocket.connect(address, 5000);
            /* 获取本地版本号并转换为int */
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            String versionName = info.versionName;
            Log.i(TAG, "sendUpdate, localVersion = " + versionName);
            int versionNameToInt = versionNameToInt(versionName);
            Log.i(TAG, "sendUpdate, localVersionInt = " + versionNameToInt);
            byte[] header = getHeader("zobaotmupdate\0", versionNameToInt);
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            mOutputStream.write(header);
            byte[] b = new byte[8];
            int read = mInputStream.read(b);
            Log.i(TAG, "sendUpdate, resHead = " + Arrays.toString(b));
            byte[] bytes = new byte[4];
            System.arraycopy(b, 0, bytes, 0, 4);
            int serverVersion = bytesToInt(bytes, 0);
            Log.i(TAG, "sendUpdate, serverVersion = " + serverVersion);
            //保存服务器版本号
            spUtils.saveVersion(serverVersion);
            int length = bytesToInt(b, 4);
            Log.i(TAG, "sendUpdate, length = " + length);
            Log.i(TAG, "sendUpdate, resHead.length = " + read);
            if (length > 0) {
                EventBus.getDefault().post("update");
            } else {
                return;
            }
            byte[] b1 = new byte[1024];
            int nLen;
            File file = new File(Environment.getExternalStorageDirectory(), "app.apk");
            if (!file.exists()) {
                // 在文件系统中根据路径创建一个新的空文件
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            while ((nLen = mInputStream.read(b1)) > 0) {
                bufferedOutputStream.write(b1, 0, nLen);
                // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
                bufferedOutputStream.flush();
            }
            outputStream.close();
            bufferedOutputStream.close();
            EventBus.getDefault().post("updatefinish");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "sendUpdate, error = " + e.getMessage());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendUpdate();
        } finally {
            try {
                mInputStream.close();
                mOutputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int versionNameToInt(String versionName) {
        byte[] version = new byte[4];
        int location = 0;
        while (versionName.lastIndexOf(".") != -1) {
            String str = versionName.substring(versionName.lastIndexOf(".") + 1);
            version[location] = Byte.decode(str);
            location++;
            versionName = versionName.substring(0, versionName.lastIndexOf("."));
        }
        version[location] = Byte.decode(versionName);
        return bytesToInt(version, 0);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    /**
     * 发送访客信息
     *
     * @param context 上下文环境
     */
    private void sendVisitor(final Context context) {
        try {
            mIP = new SpUtils(context).getIP();
            SpUtils spUtils = new SpUtils(context);
            String identity = spUtils.getIdentity(mNum);
            String idPath = PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_IDENTIFY);
            byte[] licenseByte = getJPGBody(idPath);
            String platePath = PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_PLATE);
            byte[] plateByte = getJPGBody(platePath);
            String facePath = PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_VISITOR_FACE);
            byte[] faceByte = getJPGBody(facePath);
            String licensePath = PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_LICENSE);
            int nType = -1;
            if (licenseByte.length != 0) {
                nType = 0;
            } else {
                licenseByte = getJPGBody(licensePath);
                if (licenseByte.length == 0) {
                    licenseByte = getJPGBody(PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_CAR_LICENSE));
                    if (licenseByte.length != 0) {
                        nType = 2;
                    } else {
                        licenseByte = getJPGBody(PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_RESIDENCE));
                        if (licenseByte.length != 0) {
                            nType = 3;
                        } else {
                            licenseByte = getJPGBody(PhotoUtils.getPath(context, mNum, PhotoUtils.KEY_PASSPORT));
                            if (licenseByte.length != 0) {
                                nType = 4;
                            }
                        }
                    }
                } else {
                    nType = 1;
                }
            }
            Log.i(TAG, "sendVisitor, nType = " + nType);
            String phoneNbr = spUtils.getPhoneNbr(mNum);
            String visitorNbr = spUtils.getVisitorNbr(mNum);
            String address = spUtils.getAddress(mNum);
            Log.i(TAG, "sendVisitor, address = " + address + "---len = " + address.length());
            String name = spUtils.getName(mNum);
            String addTime = spUtils.getAddTime(mNum);
            String doorName = spUtils.getDoorName() + "\0";
            String cCardno = spUtils.getVisitorCard(mNum);
            String clientIP = spUtils.getClientIP();
            int sex = spUtils.getSex(mNum);
            byte[] visitorBody = getVisitorBody(identity, licenseByte, plateByte, faceByte, phoneNbr, visitorNbr, address, name, addTime, doorName, nType, cCardno, clientIP, sex);
            byte[] header = getHeader("zobaotmvisit\0", 368928);

            mSocket = new Socket();
            Log.i(TAG, " sendVisitor, mIP = " + mIP);
            SocketAddress sAddress = new InetSocketAddress(mIP, 8100);
            mSocket.connect(sAddress, 5000);
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            mOutputStream.write(header);
            Thread.sleep(5);
            mOutputStream.flush();
            mOutputStream.write(visitorBody);
            mOutputStream.flush();
            byte[] response = new byte[12];
            int length = mInputStream.read(response);
            Log.i(TAG, "sendVisitor, response.length: " + length);
            Thread.sleep(1000);
            if (length > 0) {
                EventBus.getDefault().post("success");
            } else {
                Thread.sleep(5000);
                sendVisitor(mReference.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendVisitor(mReference.get());
        } finally {
            try {
                mInputStream.close();
                mOutputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送学生离校信息
     *
     * @param context  上下文环境,获取大门名称和学生证号
     * @param filePath 学生照片的路径
     */
    private void sendStu(final Context context, final String filePath) {
        try {
            mIP = new SpUtils(context).getIP();
            mSocket = new Socket();
            Log.i(TAG, "sendStu, mIP = " + mIP);
            SocketAddress address = new InetSocketAddress(mIP, 8100);
            mSocket.connect(address, 3500);
            byte[] header = getHeader("zobaotmstudt\0", 194620);
            byte[] stuByte = getJPGBody(filePath);
            SpUtils spUtils = new SpUtils(context);
            String doorName = spUtils.getDoorName();
            String cCardno = spUtils.getStuCard(mNum);
            String clientIP = spUtils.getClientIP();
            Log.i(TAG, "sendStu, doorname = " + doorName + "cardno = " + cCardno + "clientIP = " + clientIP);
            byte[] stuBody = getStuBody(clientIP, doorName, cCardno, stuByte != null ? stuByte.length : 0, stuByte);
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            mOutputStream.write(header);
            Thread.sleep(5);
            mOutputStream.write(stuBody);
            mOutputStream.flush();
            byte[] response = new byte[12];
            int length = mInputStream.read(response);
            Log.i(TAG, "sendStu, response.length : " + length);
            Thread.sleep(1000);
            if (length > 0) {
                EventBus.getDefault().post("" + filePath);
            } else {
                //发送学生离校信息失败，重新发送
                Thread.sleep(5000);
                sendStu(context, filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendStu(context, filePath);
        } finally {
            try {
                mInputStream.close();
                mOutputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送访客离校信息
     *
     * @param context  上下文环境,获取大门名称和访客临时卡证号
     * @param filePath 访客照片的路径
     */
    private void sendVisitorLeave(final Context context, final String filePath) {
        try {
            mIP = new SpUtils(mReference.get()).getIP();
            mSocket = new Socket();
            Log.i(TAG, "sendVisitorLeave, mIP" + mIP);
            SocketAddress address = new InetSocketAddress(mIP, 8100);
            mSocket.connect(address, 5000);
            byte[] header = getHeader("zobaotmleave\0", 194620);
            byte[] stuByte = getJPGBody(filePath);
            SpUtils spUtils = new SpUtils(context);
            String doorName = spUtils.getDoorName();
            String cCardno = spUtils.getVisitorCard(mNum);
            String clientIP = spUtils.getClientIP();
            Log.i(TAG, "sendVisitorLeave, doorname = " + doorName + "cardno = " + cCardno + "clientIP = " + clientIP);
            byte[] stuBody = getStuBody(clientIP, doorName, cCardno, stuByte != null ? stuByte.length : 0, stuByte);
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            mOutputStream.write(header);
            Thread.sleep(5);
            mOutputStream.write(stuBody);
            mOutputStream.flush();
            byte[] response = new byte[12];
            int length = mInputStream.read(response);
            Log.i(TAG, "sendVisitorLeave, response.length: " + length);
            Thread.sleep(1000);
            if (length > 0) {
                EventBus.getDefault().post("" + mFilePath);
            } else {
                Thread.sleep(5000);
                sendVisitorLeave(context, filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendVisitorLeave(context, filePath);
        } finally {
            try {
                mInputStream.close();
                mOutputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取服务器通讯录信息
     */
    private void getConnect() {
        try {
            mIP = new SpUtils(mReference.get()).getIP();
            mSocket = new Socket();
            Log.i(TAG, "getConnect, mIP" + mIP);
            SocketAddress address = new InetSocketAddress(mIP, 8100);
            mSocket.connect(address, 5000);
            byte[] header = getHeader("zobaotmaddrs\0", 0);
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            mOutputStream.write(header);
            byte[] b = new byte[8];
            int headerLength = mInputStream.read(b);
            byte[] contactCountBytes = new byte[4];
            System.arraycopy(b, 0, contactCountBytes, 0, 4);
            int contactCount = bytesToInt(contactCountBytes, 0);
            byte[] contactDataLengthBytes = new byte[4];
            System.arraycopy(b, 4, contactDataLengthBytes, 0, 4);
            int contactDataLength = bytesToInt(contactDataLengthBytes, 0);
            Log.i(TAG, "getConnect, headerLength = " + headerLength);
            Log.i(TAG, "getConnect, contactCount = " + contactCount);
            Log.i(TAG, "getConnect, contactDataLength = " + contactDataLength);
            byte[] contactDataBodyBytes = new byte[1000];
            int contactDataBodyLength = mInputStream.read(contactDataBodyBytes);
            while (contactDataBodyLength != contactDataLength) {
                byte[] bytes = new byte[1000];
                int supplementContactDataBodyLength = mInputStream.read(bytes);
                Log.i(TAG, "getConnect, supplementContactDataBodyLength = " + supplementContactDataBodyLength);
                if (supplementContactDataBodyLength < 1000) {
                    byte[] bytes1 = new byte[supplementContactDataBodyLength];
                    System.arraycopy(bytes, 0, bytes1, 0, supplementContactDataBodyLength);
                    contactDataBodyBytes = addBytes(contactDataBodyBytes, bytes1);
                } else {
                    contactDataBodyBytes = addBytes(contactDataBodyBytes, bytes);
                }
                contactDataBodyLength = supplementContactDataBodyLength + contactDataBodyLength;
            }
            Log.i(TAG, "getConnect, contactDataBodyLength = " + contactDataBodyLength);
            String contactDataBody = new String(contactDataBodyBytes, "UTF-8");
            String separator = mContext.getString(R.string.separator);
            String comma = mContext.getString(R.string.comma);
            int nameOrNum = 1;
            Person person = null;
            List<Person> list = new ArrayList<>();
            /* 根据,和;分隔符进行数据的解析 */
            while (contactDataBody.lastIndexOf(comma) != -1) {
                if (nameOrNum == 1) {
                    person = new Person();
                    person.setMPhoto(contactDataBody.substring(contactDataBody.lastIndexOf(comma) + 1, contactDataBody.lastIndexOf(separator)));
                    Log.i(TAG, "getConnect, Contact.Number = " + contactDataBody.substring(contactDataBody.lastIndexOf(comma) + 1, contactDataBody.lastIndexOf(separator)));
                    nameOrNum = 0;
                    contactDataBody = contactDataBody.substring(0, contactDataBody.lastIndexOf(comma) + 1);

                } else if (nameOrNum == 0) {
                    person.setName(contactDataBody.substring(contactDataBody.lastIndexOf(separator) + 1, contactDataBody.lastIndexOf(comma)));
                    Log.i(TAG, "getConnect, Contact.Name = " + contactDataBody.substring(contactDataBody.lastIndexOf(separator) + 1, contactDataBody.lastIndexOf(comma)));
                    contactDataBody = contactDataBody.substring(0, contactDataBody.lastIndexOf(separator) + 1);
                    nameOrNum = 2;
                } else {
                    list.add(person);
                    nameOrNum = 1;
                }
            }
            list.add(person);
            Log.i(TAG, "list.size = " + list.size());
            PersonDaoManager personDaoManager = PersonDaoManager.getInstance(mContext);
            personDaoManager.deleteAll();
            personDaoManager.insertContactList(list);
            EventBus.getDefault().post("contact_success");
            mOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "getConnect: e = " + e.getMessage());
            EventBus.getDefault().post("contact_fail");
        } finally {
            try {
                if(mInputStream != null)
                    mInputStream.close();
                if(mOutputStream != null)
                    mOutputStream.close();
                if(mSocket != null)
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param data1 1
     * @param data2 2
     * @return data1 与 data2拼接的结果
     */
    public byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    /**
     * Little-endian --> Big-endian
     */
    private byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /**
     * 获取头结构体
     */
    private byte[] getHeader(String cflag, int nLen) {
        byte[] b = new byte[20];
        byte[] temp;
        //分别将struct的成员格式为byte数组
        // 标签cflag
        try {
            System.arraycopy(cflag.getBytes("UTF-8"), 0, b, 0, cflag.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //后续包体长度nLen
        temp = toLH(nLen);
        System.arraycopy(temp, 0, b, 16, temp.length);
        return b;
    }

    /**
     * 获取学生离校信息结构体
     */
    private byte[] getStuBody(String clientIP, String doorName, String cCardno, int nFilelen1, byte[] data) {
        byte[] b = new byte[194620];
        byte[] temp;
        //分别将struct的成员格式为byte数组。
        try {
            System.arraycopy((cCardno).getBytes("UTF-8"), 0, b, 0, cCardno.length());
            temp = toLH(nFilelen1);
            Log.i(TAG, "getStuBody, nFilelen1 = " + temp.length);
            System.arraycopy(temp, 0, b, 16, temp.length);
            Log.i(TAG, "getStuBody, StuBody.length = " + data.length);
            System.arraycopy(data, 0, b, 20, data.length);
            System.arraycopy(doorName.getBytes("UTF-8"), 0, b, 194580, doorName.getBytes("UTF-8").length);
            System.arraycopy(clientIP.getBytes("UTF-8"), 0, b, 194604, clientIP.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 获取访客登记结构体
     */
    private byte[] getVisitorBody(String identity, byte[] idByte, byte[] plateByte, byte[] faceByte, String phoneNbr, String visitorNbr, String address, String name, String addTime,
                                  String doorName, int nType, String cCardno, String clientIP, int sex) {
        try {
            byte[] bytes = new byte[368928];
            byte[] temp;
            System.arraycopy((identity).getBytes("UTF-8"), 0, bytes, 0, identity.length());

            temp = toLH(idByte.length);
            Log.i(TAG, "getVisitorBody, idByte = " + idByte.length);
            System.arraycopy(temp, 0, bytes, 20, temp.length);
            System.arraycopy(idByte, 0, bytes, 24, idByte.length);

            temp = toLH(plateByte.length);
            Log.i(TAG, "getVisitorBody, plateByte = " + plateByte.length);
            System.arraycopy(temp, 0, bytes, 153624, temp.length);
            System.arraycopy(plateByte, 0, bytes, 153628, plateByte.length);

            temp = toLH(faceByte.length);
            Log.i(TAG, "getVisitorBody, faceByte = " + faceByte.length);
            System.arraycopy(temp, 0, bytes, 215068, temp.length);
            System.arraycopy(faceByte, 0, bytes, 215072, faceByte.length);

            System.arraycopy((phoneNbr).getBytes("UTF-8"), 0, bytes, 368672, phoneNbr.length());
            System.arraycopy((visitorNbr).getBytes("UTF-8"), 0, bytes, 368684, visitorNbr.length());
            System.arraycopy((address).getBytes("UTF-8"), 0, bytes, 368696, (address).getBytes("UTF-8").length);
            System.arraycopy((name).getBytes("UTF-8"), 0, bytes, 368824, (name).getBytes("UTF-8").length);
            System.arraycopy((addTime).getBytes("UTF-8"), 0, bytes, 368848, addTime.length());
            System.arraycopy((doorName).getBytes("UTF-8"), 0, bytes, 368861, doorName.getBytes("UTF-8").length);
            temp = toLH(nType);
            System.arraycopy(temp, 0, bytes, 368888, temp.length);
            System.arraycopy((cCardno).getBytes("UTF-8"), 0, bytes, 368892, cCardno.getBytes("UTF-8").length);
            System.arraycopy((clientIP).getBytes("UTF-8"), 0, bytes, 368908, clientIP.getBytes("UTF-8").length);
            temp = toLH(sex);
            Log.i(TAG, "getVisitorBody, sex = " + sex);
            System.arraycopy(temp, 0, bytes, 368924, temp.length);

            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * 根据图片路径获取图片字节流
     *
     * @param filePath 图片路径
     * @return 返回图片字节流
     */
    private byte[] getJPGBody(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            byte[] temp = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len;
            while ((len = fileInputStream.read(temp)) != -1) {
                byteArrayOutputStream.write(temp, 0, len);
            }
            byteArrayOutputStream.close();
            fileInputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    private void createFileWithByte(byte[] bytes, int len) {
        /*
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(Environment.getExternalStorageDirectory(),
                "app.apk");
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (!file.exists()) {
                // 在文件系统中根据路径创建一个新的空文件
                file.createNewFile();
            }
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes, 0, len);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}
