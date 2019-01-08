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
     * @param loginName 登陆名 暂时传""
     * @param numPerPage 每页记录数 根据实际需求传
     * @param schName 学校名称 配置界面填写的学校名称
     * @param schNameList 多个学校名称 暂传空List
     * @param pageNo 页码：从1开始
     * @param lastModifyTime 同步时间点接口每次返回该时间点之后变更的数据; yyyy-MM-dd HH:mm:ss
     *                       （因接口未提供唯一标识，无法进行本地数据库更新，暂时固定为"1970-01-01 00:00:00"）
     * @return 获取教师通讯录信息的Call对象，用作异步或同步调用请求
     */
    @FormUrlEncoded
    @POST("queryTeachers.php")
    Call<ContactBean> queryTeachersInter(@Field("loginName") String loginName, @Field("numPerPage") int numPerPage, @Field("schName") String schName, @Field("schNameList[]")List<String> schNameList,
                                          @Field("pageNo")int pageNo, @Field("lastModifyTime") String lastModifyTime);

    /**
     * 访客手持设备上传访客记录
     * @param list 来访数据数组
     * @return 访客手持设备上传访客记录的Call对象，用作异步或同步调用请求
     */
    @FormUrlEncoded
    @POST("uploadVisitors.php")
    Call<UploadResponseBean> uploadVisitorsInter(@Field("list[]") List<VisitorComeBean> list);

    /**
     * 访客手持设备上传访客离开记录
     * @param list 访客离开记录
     * @return 访客手持设备上传访客离开记录的Call对象，用作异步或同步调用请求
     */
    @FormUrlEncoded
    @POST("uploadVisitorsLeave.php")
    Call<UploadResponseBean> uploadVisitorsLeaveInter(@Field("list[]") List<VisitorsLeave> list);

    /**
     * 访客手持设备上传学生离校记录
     * @param list 学生离校记录集合
     * @return 访客手持设备上传学生离校记录的Call对象，用作异步或同步调用请求
     */
    @FormUrlEncoded
    @POST("uploadStudentsLeave.php")
    Call<UploadResponseBean> uploadStudentsLeaveInter(@Field("list[]") List<StudentLeave> list);
}
