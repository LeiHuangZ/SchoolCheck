package com.example.huang.myapplication.certificate.identification.handheld.instance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huang.myapplication.BaseActivity;
import com.example.huang.myapplication.R;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.example.huang.myapplication.certificate.identification.handheld.idcard.IDCardManager;
import com.example.huang.myapplication.main.MainActivity;
import com.example.huang.myapplication.utils.PhotoUtils;
import com.example.huang.myapplication.utils.SpUtils;
import com.example.huang.myapplication.visitor.VisitorActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 读取身份证信息，不带指纹的身份证信息
 *
 * @author huang
 */
public class IDReadActivity extends BaseActivity {
    @BindView(R.id.editText_name)
    TextView editTextName;
    @BindView(R.id.editText_sex)
    TextView editTextSex;
    @BindView(R.id.editText_nation)
    TextView editTextNation;
    @BindView(R.id.editText_year)
    TextView editTextYear;
    @BindView(R.id.editText_month)
    TextView editTextMonth;
    @BindView(R.id.editText_day)
    TextView editTextDay;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.editText_address)
    TextView editTextAddress;
    @BindView(R.id.editText_IDCard)
    TextView editTextIDCard;
    @BindView(R.id.editText_office)
    TextView editTextOffice;
    @BindView(R.id.editText_effective)
    TextView editTextEffective;
    @BindView(R.id.button_next)
    Button buttonFinish;

    private IDCardManager manager;
    private Toast toast;
    private Bitmap photoBitmap = null;
    /**
     * 重复查找标签
     */
    private boolean findFlag = true;
    private SpUtils mSpUtils;

    @SuppressLint("ShowToast")
    private void showToast(String info) {
        if (toast == null) {
            toast = Toast.makeText(IDReadActivity.this, info, Toast.LENGTH_SHORT);
        } else {
            toast.setText(info);
        }
        toast.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idread_improve);

        ButterKnife.bind(this);
        manager = new IDCardManager(IDReadActivity.this);
        mSpUtils = new SpUtils(IDReadActivity.this);
        Util.initSoundPool(IDReadActivity.this);

        //初始化线程池
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                while (findFlag) {
                    if (manager.findCard()) {
                        handler.sendEmptyMessage(1);
                        if(!manager.selectCard()){
                            handler.sendEmptyMessage(2);
                            continue;
                        }
                        IDCardInfo idCardInfo = manager.readCard(200);
                        if (idCardInfo == null)
                            continue;
                        if (idCardInfo != null) {
                            String birth = idCardInfo.getBirth();
                            String year = "", month = "", day = "";
                            if(birth == null || birth.length()!=8+3){
                                handler.sendEmptyMessage(3);
                                birth = Util.formatStr(birth, 8);
                            }else{
                                year = birth.substring(0,4);
                                month = birth.substring(5,7);
                                day = birth.substring(8,10);
                            }

                            String validityTime = idCardInfo.getValidityTime();
                            String start = "",end = "";
                            if(validityTime == null || validityTime.length()!=16+5){
                                validityTime = Util.formatStr(validityTime, 16);
                            }else{
                                start = validityTime.substring(0,10);
                                end = validityTime.substring(11,21);
                            }

                            sendMessage(idCardInfo.getName(), idCardInfo.getSex(), idCardInfo.getNation(),
                                    year, month, day,
                                    idCardInfo.getAddress(), idCardInfo.getId().trim(), idCardInfo.getDepart(),
                                    start, end,
                                    manager.getBitmap(idCardInfo));
                        }
                        //存储身份证相关信息
                        if (idCardInfo != null) {
                            mSpUtils.saveIdentity(MainActivity.count, idCardInfo.getId());
                            mSpUtils.saveAddress(MainActivity.count, idCardInfo.getAddress());
                            mSpUtils.saveAddTime(MainActivity.count, idCardInfo.getValidityTime().substring(6, 12));
                            mSpUtils.saveName(MainActivity.count, idCardInfo.getName());
                            Log.i("IDReadActivity", "sex: " + idCardInfo.getSex());
                            if ("男".equals(idCardInfo.getSex().trim())){
                                mSpUtils.saveSex(MainActivity.count, 0);
                            }else {
                                mSpUtils.saveSex(MainActivity.count,1);
                            }
                            PhotoUtils.saveJpeg(IDReadActivity.this, manager.getBitmap(idCardInfo), PhotoUtils.KEY_IDENTIFY);
                        }
                        findFlag = false;
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset]&0xFF)
        | ((ary[offset+1]<<8) & 0xFF00)
        | ((ary[offset+2]<<16)& 0xFF0000)
        | ((ary[offset+3]<<24) & 0xFF000000));
        return value;
    }



    /**
     * 使用静态的Handler内部类，防止内存泄漏
     */
    private MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<IDReadActivity> mIDReadActivityWeakReference;

        MyHandler(IDReadActivity idReadActivity) {
            mIDReadActivityWeakReference = new WeakReference<>(idReadActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            IDReadActivity idReadActivity = mIDReadActivityWeakReference.get();
            switch (msg.what) {
                case 0:
                    Util.play(1, 0);
                    idReadActivity.showToast("读取完成！");
                    Bundle bundle = msg.getData();
                    //获取身份证信息：姓名、性别、出生年、月、日、住址、身份证号、签发机关、有效期开始、结束、（额外信息新地址（一般情况为空））
                    String name = bundle.getString("name");
                    String sex = bundle.getString("sex");
                    String nation = bundle.getString("nation");
                    String year = bundle.getString("year");
                    String month = bundle.getString("month");
                    String day = bundle.getString("day");
                    String address = bundle.getString("address");
                    String id = bundle.getString("id");
                    String office = bundle.getString("office");
                    String start = bundle.getString("begin");
                    String stop = bundle.getString("end");
                    idReadActivity.updateView(name, sex, nation, year, month, day, address, id, office, start, stop);
                    break;
                case 1:
                    idReadActivity.clear();
                    idReadActivity.showToast("找到身份证\n正在读取");
                    break;
                case 2:
                    idReadActivity.showToast("选卡失败\n请重试");
                    break;
                case 3:
                    idReadActivity.showToast("出生日期长度不对");
                    break;
                case 4:
                    idReadActivity.showToast("有效期长度不对");
                    break;
                default:
                    break;
            }
        }
    }

    private void updateView(String name, String sex, String nation,
                            String year, String month, String day, String address, String id,
                            String office, String start, String stop) {
        //获取图片位图，并显示：
        imageView.setImageBitmap(photoBitmap);
        editTextName.setText(name);
        editTextSex.setText(sex);
        editTextNation.setText(nation);
        editTextYear.setText(year);
        editTextMonth.setText(month);
        editTextDay.setText(day);
        editTextAddress.setText(address);
        editTextIDCard.setText(id);
        editTextOffice.setText(office);
        editTextEffective.setText(start);
        editTextEffective.append("-" + stop);
        //使下一步按钮可点击
        buttonFinish.setEnabled(true);
    }

    private String format(String str) {
        StringBuilder buffer = new StringBuilder(str);
        StringBuilder buffer1 = buffer.insert(4, ".");
        return buffer1.insert(7, ".").toString();
    }

    private void clear() {
        editTextName.setText("");
        editTextSex.setText("");
        editTextNation.setText("");
        editTextYear.setText("");
        editTextMonth.setText("");
        editTextDay.setText("");
        editTextAddress.setText("");
        editTextIDCard.setText("");
        editTextOffice.setText("");
        editTextEffective.setText("");
        imageView.setImageResource(R.drawable.photo);
    }

    private void sendMessage(String name, String sex, String nation,
                             String year, String month, String day, String address, String id,
                             String office, String start, String stop, Bitmap bitmap) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("sex", sex);
        bundle.putString("nation", nation);
        bundle.putString("year", year);
        bundle.putString("month", month);
        bundle.putString("day", day);
        bundle.putString("address", address);
        bundle.putString("id", id);
        bundle.putString("office", office);
        bundle.putString("begin", start);
        bundle.putString("end", stop);
        bundle.putString("fp1", "");
        bundle.putString("fp2", "");
        photoBitmap = bitmap;
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        findFlag = false;
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @OnClick({R.id.button_cancel, R.id.button_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                findFlag = false;
                if (manager != null) {
                    manager.close();
                    manager = null;
                }
                mSpUtils.saveIdentity(MainActivity.count, "");
                mSpUtils.saveAddress(MainActivity.count, "");
                mSpUtils.saveAddTime(MainActivity.count, "");
                mSpUtils.saveName(MainActivity.count, "");
                mSpUtils.saveSex(MainActivity.count, -1);
                PhotoUtils.clearPhoto(this, MainActivity.count, PhotoUtils.KEY_IDENTIFY);
                startActivity(new Intent(IDReadActivity.this, MainActivity.class));
                break;
            //"下一步"按钮
            case R.id.button_next:
                findFlag = false;
                if (manager != null) {
                    manager.close();
                    manager = null;
                }
                startActivity(new Intent(this, VisitorActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSpUtils.saveIdentity(MainActivity.count, "");
        mSpUtils.saveAddress(MainActivity.count, "");
        mSpUtils.saveAddTime(MainActivity.count, "");
        mSpUtils.saveName(MainActivity.count, "");
        mSpUtils.saveSex(MainActivity.count, -1);
        PhotoUtils.clearPhoto(this, MainActivity.count, PhotoUtils.KEY_IDENTIFY);
        super.onBackPressed();
    }
}
