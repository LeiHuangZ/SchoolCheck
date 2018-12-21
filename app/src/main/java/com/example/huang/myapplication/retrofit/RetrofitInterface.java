package com.example.huang.myapplication.retrofit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author huang 3004240957@qq.com
 */
public interface RetrofitInterface {
    /**
     * 获取教师通讯录信息
     * @param loginName 登陆名
     * @param numPerPage 每页记录数
     * @param schName 学校名称
     * @param schNameList 多个学校名称
     * @param pageNo 页码：从1开始
     * @param lastModifyTime 同步时间点接口每次返回该时间点之后变更的数据; yyyy-MM-dd HH:mm:ss
     * @return 获取教师通讯录信息的Call对象，用作异步或同步调用请求
     */
    @FormUrlEncoded
    @POST("queryTeachers.php")
    Call<ContactBean> queryTeachersInter(@Field("loginName") String loginName, @Field("numPerPage") int numPerPage, @Field("schName") String schName, @Field("schNameList[]")List<String> schNameList,
                                          @Field("pageNo")int pageNo, @Field("lastModifyTime") String lastModifyTime);
}
