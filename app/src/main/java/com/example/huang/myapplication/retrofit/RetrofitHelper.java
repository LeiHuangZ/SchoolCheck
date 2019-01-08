package com.example.huang.myapplication.retrofit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.huang.myapplication.greendao.PersonDaoManager;
import com.zsy.words.bean.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.huang.myapplication.utils.Tools.formateTime;
import static com.example.huang.myapplication.utils.Tools.notifySystemToScan;

/**
 * @author huang 3004240957@qq.com
 */
public class RetrofitHelper {

    private static RetrofitHelper sRetrofitHelper;
    private static RetrofitInterface sRetrofitInter;
    private final ExecutorService mExecutorService;
    private static WeakReference<Context> mWeakReference;

    private RetrofitHelper(Context context) {
        //初始化线程池
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        mWeakReference = new WeakReference<>(context);
    }

    public static RetrofitHelper getInstance(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://zhhs.yixiaobang.com/visitterminal/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (sRetrofitInter == null) {
            sRetrofitInter = retrofit.create(RetrofitInterface.class);
        }
        if (sRetrofitHelper == null) {
            sRetrofitHelper = new RetrofitHelper(context);
        }
        return sRetrofitHelper;
    }

    /**
     * 获取通讯录
     * @param isRepeat 是否是在一次请求中的再次获取（因为每一页记录数量的设置关系，可能会有下一页数据，此时需要再次请求获取下一页数据）
     * @param loginName 登录名（暂时填空）
     * @param numPerPage 每一页的记录数
     * @param schName 学校名称
     * @param schNameList 学校名称集合（暂时传空）
     * @param pageNo 页码
     * @param lastModifyTime 同步时间点接口每次返回该时间点之后变更的数据; yyyy-MM-dd HH:mm:ss
     */
    public void queryTeachers(final boolean isRepeat, final String loginName, final int numPerPage, final String schName, final List<String> schNameList, final int pageNo, final String lastModifyTime) {
        Call<ContactBean> call = sRetrofitInter.queryTeachersInter(loginName, numPerPage, schName, schNameList, pageNo, lastModifyTime);
        call.enqueue(new Callback<ContactBean>() {
            @Override
            public void onResponse(@NonNull Call<ContactBean> call, @NonNull Response<ContactBean> response) {
                ContactBean body = response.body();
                Log.i("Huang", "onResponse: " + body.toString());
                final List<ContactBean.PageInfoBean.ListBean> list = body.getPageInfo().getList();
                Log.i("Huang, RetrofitHelper", "PageInfoBean.size = " + list.size());
                /*
                 * 清空本地通讯录数据库
                 */
                if (!isRepeat) {
                    PersonDaoManager personDaoManager = PersonDaoManager.getInstance(mWeakReference.get());
                    personDaoManager.deleteAll();
                }
                /*
                 * 将新获取的通讯录存入本地数据库
                 */
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Person> personList = new ArrayList<>();
                        for (ContactBean.PageInfoBean.ListBean listBean : list) {
                            personList.add(new Person(listBean.getName(), listBean.getPhone()));
                        }
                        PersonDaoManager personDaoManager = PersonDaoManager.getInstance(mWeakReference.get());
                        personDaoManager.insertContactList(personList);
                    }
                });
                // 是否还有下一页通讯录数据
                boolean hasNextPage = body.getPageInfo().isHasNextPage();
                if (!hasNextPage) {
                    // 如果没有下一页，结束请求，通知主界面更新
                    EventBus.getDefault().post("contact_success");
                } else {
                    // 如果还有下一页，将pageNo页码加1，继续获取
                    int pageNoPlus = pageNo + 1;
                    queryTeachers(true, loginName, numPerPage, schName, schNameList, pageNoPlus, lastModifyTime);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ContactBean> call, @NonNull Throwable t) {
                EventBus.getDefault().post("contact_fail");
            }
        });
    }

    /**
     * 上传访客来访数据
     * @param visitorsList 访客来访数据集合
     */
    public void uploadVisitors(final List<VisitorComeBean> visitorsList) {
        Call<UploadResponseBean> call = sRetrofitInter.uploadVisitorsInter(visitorsList);
        call.enqueue(new Callback<UploadResponseBean>() {
            @Override
            public void onResponse(@NonNull Call<UploadResponseBean> call, @NonNull Response<UploadResponseBean> response) {
//                try {
//                    String responseStr = response.body().string();
//                    Log.e("Huang, RetrofitHelper", "responseStr = " + responseStr);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                UploadResponseBean body = response.body();
                UploadResponseBean.ServerResultBean serverResult = body.getServerResult();
                int resultCode = serverResult.getResultCode();
                String resultMessage = serverResult.getResultMessage();
                String internalMessage = serverResult.getInternalMessage();
                EventBus.getDefault().post("success");
                saveUploadLog("访客来访数据上传", formateTime(System.currentTimeMillis()), "resultCode = " + resultCode + ", resultMessage = " + resultMessage + ", internalMessage = " + internalMessage);
            }

            @Override
            public void onFailure(@NonNull Call<UploadResponseBean> call, @NonNull Throwable t) {
                try {
                    Log.e("Huang, RetrofitHelper", "uploadVisitors t =" + Log.getStackTraceString(t));
                    Thread.sleep(5000);
                    uploadVisitors(visitorsList);
                } catch (InterruptedException e) {
                    Log.e("Huang, RetrofitHelper", "uploadVisitors e = " + Log.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * 上传访客离开的数据
     * @param visitorsLeaveList 访客离校的数据集合
     */
    public void uploadVisitorsLeave(final List<VisitorsLeave> visitorsLeaveList) {
        Call<UploadResponseBean> call = sRetrofitInter.uploadVisitorsLeaveInter(visitorsLeaveList);
        call.enqueue(new Callback<UploadResponseBean>() {
            @Override
            public void onResponse(@NonNull Call<UploadResponseBean> call, @NonNull Response<UploadResponseBean> response) {
//                try {
//                    Log.e("Huang, RetrofitHelper", "response = " + response.body().string());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                UploadResponseBean body = response.body();
                UploadResponseBean.ServerResultBean serverResult = body.getServerResult();
                int resultCode = serverResult.getResultCode();
                String resultMessage = serverResult.getResultMessage();
                String internalMessage = serverResult.getInternalMessage();
                EventBus.getDefault().post("success");
                saveUploadLog("访客离开数据上传", formateTime(System.currentTimeMillis()), "resultCode = " + resultCode + ", resultMessage = " + resultMessage + ", internalMessage = " + internalMessage);
            }

            @Override
            public void onFailure(@NonNull Call<UploadResponseBean> call, @NonNull Throwable t) {
                try {
                    Log.e("Huang, RetrofitHelper", "uploadVisitorsLeave t =" + Log.getStackTraceString(t));
                    Thread.sleep(5000);
                    uploadVisitorsLeave(visitorsLeaveList);
                } catch (InterruptedException e) {
                    Log.e("Huang, RetrofitHelper", "uploadVisitorsLeave e = " + Log.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * 上传学生离校信息
     * @param studentLeaveList 学生离校的数据集合
     */
    public void uploadStudentsLeave(final List<StudentLeave> studentLeaveList) {
        Call<UploadResponseBean> call = sRetrofitInter.uploadStudentsLeaveInter(studentLeaveList);
        call.enqueue(new Callback<UploadResponseBean>() {
            @Override
            public void onResponse(@NonNull Call<UploadResponseBean> call, @NonNull Response<UploadResponseBean> response) {
//                try {
//                    String responseStr = response.body().string();
//                    Log.e("Huang, RetrofitHelper", "responseStr = " + responseStr);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                UploadResponseBean body = response.body();
                UploadResponseBean.ServerResultBean serverResult = body.getServerResult();
                int resultCode = serverResult.getResultCode();
                String resultMessage = serverResult.getResultMessage();
                String internalMessage = serverResult.getInternalMessage();
                Log.i("Huang, RetrofitHelper", "uploadStudentsLeave response = " + response);
                EventBus.getDefault().post("success");
                saveUploadLog("学生离校数据上传", formateTime(System.currentTimeMillis()), "resultCode = " + resultCode + ", resultMessage = " + resultMessage + ", internalMessage = " + internalMessage);
            }

            @Override
            public void onFailure(@NonNull Call<UploadResponseBean> call, @NonNull Throwable t) {
                try {
                    Log.e("Huang, RetrofitHelper", "uploadStudentsLeave t =" + Log.getStackTraceString(t));
                    Thread.sleep(5000);
                    uploadStudentsLeave(studentLeaveList);
                } catch (InterruptedException e) {
                    Log.e("Huang, RetrofitHelper", "uploadStudentsLeave e = " + Log.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * 保存上传日志，以便后续现场调试
     * @param uploadType 上传类型
     * @param uploadTime 上传时间
     * @param uploadResult 上传结果
     */
    // TODO: 2018/12/28 保存log，正式版本发布记得关闭
    private void saveUploadLog(String uploadType, String uploadTime, String uploadResult) {
        File file = Environment.getExternalStorageDirectory();
        String absolutePath = file.getAbsolutePath();
        String dirPath = absolutePath + "/scLog";
        File dirFile = new File(dirPath);
        if (!dirFile.exists()){
            boolean mkdir = dirFile.mkdir();
            Log.v("Huang, RetrofitHelper", "saveUploadLog, mkdir = " + mkdir);
            notifySystemToScan(mWeakReference.get(), dirPath);
        }
        String filePath = dirPath + "/uploadLog.txt";
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            fileWriter.write(uploadType + "  ");
            fileWriter.write(uploadTime + "  ");
            fileWriter.write(uploadResult + "  \r\n");
            fileWriter.flush();
            fileWriter.close();
            notifySystemToScan(mWeakReference.get().getApplicationContext(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
