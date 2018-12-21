package com.example.huang.myapplication.retrofit;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;

/**
 * @author huang 3004240957@qq.com
 */
public class RetrofitHelper {

    private static RetrofitHelper sRetrofitHelper;
    private static RetrofitInterface sRetrofitInter;

    private RetrofitHelper(){}

    public static RetrofitHelper getInstance(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://zhhs.yixiaobang.com/visitterminal/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (sRetrofitInter == null){
            sRetrofitInter = retrofit.create(RetrofitInterface.class);
        }
        if (sRetrofitHelper == null){
            sRetrofitHelper = new RetrofitHelper();
        }
        return sRetrofitHelper;
    }

    public void queryTeachers(String loginName, int numPerPage, String schName, List<String> schNameList, int pageNo, String lastModifyTime, RetrofitListener listener){
        Call<ContactBean> call = sRetrofitInter.queryTeachersInter(loginName, numPerPage, schName, schNameList, pageNo, lastModifyTime);
        call.enqueue(new Callback<ContactBean>() {
            @Override
            public void onResponse(@NonNull Call<ContactBean> call, @NonNull Response<ContactBean> response) {
                ContactBean body = response.body();
                Log.i("Huang", "onResponse: " + body.toString());
                List<ContactBean.PageInfoBean.ListBean> list = body.getPageInfo().getList();
                Log.i("Huang, RetrofitHelper", "PageInfoBean.size = " + list.size());
            }

            @Override
            public void onFailure(@NonNull Call<ContactBean> call, @NonNull Throwable t) {

            }
        });
    }

    public interface RetrofitListener{
        void onResponse();
        void onFailure();
    }
}
