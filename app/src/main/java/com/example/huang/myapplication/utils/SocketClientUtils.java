package com.example.huang.myapplication.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Socket通信帮助类，心跳
 * 其他发送数据的方法请查看 {@link com.example.huang.myapplication.utils.MyTask}
 *
 * @author huang
 * @date 2017/10/31
 */

public class SocketClientUtils {
    private static String TAG = "SocketClientUtils";

    private static Socket mSocket;
    private static SocketClientUtils mSocketClientUtils = new SocketClientUtils();
    private static ExecutorService mExecutorService;
    private OutputStream mOutputStream;
    private static String mIP;
    /**
     * 心跳标签
     */
    private boolean mFlag = true;
    private InputStream mInputStream;

    private SocketClientUtils() {
        //初始化线程池
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static SocketClientUtils getInstance(final String ip) {
        mIP = ip;
        return mSocketClientUtils;
    }

    /**
     * 开始心跳，隔一定时间心跳(30s),通过EventBus，通知心跳状态
     */
    public void heartBeat() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    try {
                        mSocket = new Socket();
                        Log.i(TAG, "heartBeat, mIP" + mIP);
                        SocketAddress address = new InetSocketAddress(mIP, 8100);
                        mSocket.connect(address, 3500);

                        mOutputStream = mSocket.getOutputStream();
                        mInputStream = mSocket.getInputStream();

                        byte[] header = getHeader();
                        mOutputStream.write(header);
                        mOutputStream.flush();
                        byte[] response = new byte[12];
                        int read = mInputStream.read(response);
                        Log.i(TAG, "heartBeat, response.length : " + read);
                        if (read > 0) {
                            EventBus.getDefault().post("0");
                        } else {
                            //心跳失败，立即开始新一轮心跳
                            EventBus.getDefault().post("1");
                            Thread.sleep(5000);
                            heartBeat();
                            return;
                        }
                        //与服务器心跳连接成功，30秒后开启下一次心跳
                        try {
                            Thread.sleep(60000);
                            mSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        //心跳失败，开启新一轮心跳
                        EventBus.getDefault().post("1");
                        heartBeat();
                        e.printStackTrace();
                        return;
                    }finally {
                        try {
                            // TODO: 2018/1/20 判断Socket连接是否为空，如果为空的话，返回；否则会出现空指针，导致程序报错 之所以会为空，是因为，有可能客户端启动了，但是服务端没有启动。
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
            }
        });
    }

    /**
     * 心跳
     */
    public void changeFlag(boolean flag) {
        mFlag = flag;
    }

    /**
     * Little-endian --> Big-endian
     */
    private byte[] toLH() {
        byte[] b = new byte[4];
        b[0] = (byte) (0);
        b[1] = (byte) (0);
        b[2] = (byte) (0);
        b[3] = (byte) (0);
        return b;
    }

    /**
     * 获取头结构体
     */
    private byte[] getHeader() {
        byte[] b = new byte[20];
        byte[] temp;
        //分别将struct的成员格式为byte数组
        // 标签cflag
        try {
            System.arraycopy("zobaotmhtbit\0".getBytes("UTF-8"), 0, b, 0, "zobaotmhtbit\0".length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //后续包体长度nLen
        temp = toLH();
        System.arraycopy(temp, 0, b, 16, temp.length);
        return b;
    }

}
