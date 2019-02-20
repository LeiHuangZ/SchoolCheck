package com.example.huang.myapplication.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * SP工具类,管理存储的string类型的信息
 *
 * @author huang
 * @date 2017/10/19
 */

public class SpUtils {
    private final SharedPreferences mPreferences;
    private final SharedPreferences mSIPreferences;

    @SuppressLint("CommitPrefEdits")
    public SpUtils(Context context) {
        mPreferences = context.getSharedPreferences("check_in", Context.MODE_PRIVATE);
        mSIPreferences = context.getSharedPreferences("school_ip", Context.MODE_PRIVATE);
    }

    /**
     * 存储性别
     *
     * @param sex 性别
     */
    public void saveSex(long num, int sex) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putInt("saveSex"+num, sex);
        edit.apply();
    }

//    public void saveSexStr(long num, String sex) {
//        SharedPreferences.Editor edit = mPreferences.edit();
//        edit.putString("saveSex"+num, sex);
//        edit.apply();
//    }

    /**
     * 获取性别
     *
     * @return 性别
     */
    public int getSex(long num) {
        return mPreferences.getInt("saveSex"+num, -1);
    }

//    public String getSexStr(long num) {
//        return mPreferences.getString("saveSex"+num, "");
//    }

    /**
     * 存储身份证号
     *
     * @param idCard 身份证号
     */
    public void saveIdentity(long num, String idCard) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveIdentity"+num, idCard);
        edit.apply();
    }

    /**
     * 获取身份证号
     *
     * @return 身份证号
     */
    public String getIdentity(long num) {
        return mPreferences.getString("saveIdentity"+num, "");
    }

    /**
     * 存储受访者电话
     *
     * @param num 受访者电话
     */
    public void savePhoneNbr(long num, String pNum) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("savePhoneNbr"+num, pNum);
        edit.apply();
    }

    /**
     * 获取受访者电话
     *
     * @return 受访者电话
     */
    public String getPhoneNbr(long num) {
        return mPreferences.getString("savePhoneNbr"+num, "");
    }

    /**
     * 清除受访者电话
     */
    public void clearPhoneNbr(long num) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("savePhoneNbr"+num, "");
        edit.apply();
    }

    /**
     * 存储访客电话
     *
     * @param num 访客电话
     */
    public void saveVisitorNbr(long num, String pNum) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveVisitorNbr"+num, pNum);
        edit.apply();
    }

    /**
     * 获取访客电话
     *
     * @return 访客电话
     */
    public String getVisitorNbr(long num) {
        return mPreferences.getString("saveVisitorNbr"+num, "");
    }

    /**
     * 清除访客电话
     */
    public void clearVisitorNbr(long num) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveVisitorNbr"+num, "");
        edit.apply();
    }

    /**
     * 存储身份证地址
     *
     * @param address 身份证地址
     */
    public void saveAddress(long num, String address) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveAddress"+num, address);
        edit.apply();
    }

    /**
     * 获取身份证地址
     *
     * @return 身份证地址
     */
    public String getAddress(long num) {
        return mPreferences.getString("saveAddress"+num, "");
    }

    /**
     * 存储身份证姓名
     *
     * @param name 身份证姓名
     */
    public void saveName(long num, String name) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveName"+num, name);
        edit.apply();
    }

    /**
     * 获取身份证姓名
     *
     * @return 身份证姓名
     */
    public String getName(long num) {
        return mPreferences.getString("saveName"+num, "");
    }

    /**
     * 存储身份证有效期
     *
     * @param addTime 身份证有效期
     */
    public void saveAddTime(long num, String addTime) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveAddTime"+num, addTime);
        edit.apply();
    }

    /**
     * 获取身份证有效期
     *
     * @return 身份证有效期
     */
    String getAddTime(long num) {
        return mPreferences.getString("saveAddTime"+num, "");
    }
    /**
     * 存储访客临时卡号
     */
    public void saveVisitorCard(long num, String cardNum){
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("saveVisitorCard" + num, cardNum);
        edit.apply();
    }

    /**
     * 获取访客临时卡号
     */
    public String getVisitorCard(long num){
        return mPreferences.getString("saveVisitorCard" + num, "");
    }

    /*================================================ 卡号的8位正反序，10位正反序（未采用每次读到之后都转换并存储这些的方式，采用发送卡号前读取上面存取的卡号进行转换）=======================================================*/

    public void putCardNumEightPositive(long num, String cardNum){
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("CardNumEightPositive" + num, cardNum);
        edit.apply();
    }

    public void putCardNumTenPositive(long num, String cardNum){
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("CardNumTenPositive" + num, cardNum);
        edit.apply();
    }

    public void putCardNumEightReverse(long num, String cardNum){
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("CardNumEightReverse" + num, cardNum);
        edit.apply();
    }

    public void putCardNumTenReverse(long num, String cardNum){
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("CardNumTenReverse" + num, cardNum);
        edit.apply();
    }

    public String getCardNumEightPositive(long num, String cardNum){
        return mPreferences.getString("CardNumEightPositive" + num, "");
    }

    public String getCardNumTenPositive(long num, String cardNum){
        return mPreferences.getString("CardNumTenPositive" + num, "");
    }

    public String getCardNumEightReverse(long num, String cardNum){
        return mPreferences.getString("CardNumEightReverse" + num, "");
    }

    public String getCardNumTenReverse(long num, String cardNum){
        return mPreferences.getString("CardNumTenReverse" + num, "");
    }

    /**
     * 存储学生证证件号
     *
     * @param stuCard 学生证证件号
     */
    public void saveStuCard(long num, String stuCard) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString("StuCard"+num, stuCard);
        edit.apply();
    }

    /**
     * 获取学生证证件号
     *
     * @return StuCard 学生证证件号
     */
    public String getStuCard(long num) {
        return mPreferences.getString("StuCard"+num, "");
    }

    /**
     * 存储车牌号
     * @param num 记录编码
     * @param plateNum 车牌号
     */
    public void putPlateNum(long num, String plateNum){
        mPreferences.edit().putString("PlateNum"+num, plateNum).apply();
    }

    /**
     * 获取存储车牌号
     * @param num 记录编码
     * @return 存储的车牌号
     */
    public String getPlateNum(long num){
        return mPreferences.getString("PlateNum"+num, "");
    }

    /**
     * 存储获取获取Activity标记，用于判断Setting界面结束后，跳入哪个界面
     */

    public void saveTag(String tag) {
        mPreferences.edit().putString("tag", tag).apply();
    }

    public String getTag() {
        return mPreferences.getString("tag", "");
    }

    /**
     * 清除所有SP数据
     */
    public void clearAll() {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.clear();
        edit.apply();
    }

    /**
     * 存储IP信息
     *
     * @param ip 服务器地址信息
     */
    public void saveIP(String ip) {
        SharedPreferences.Editor edit = mSIPreferences.edit();
        edit.putString("saveIP", ip);
        edit.apply();
    }

    /**
     * 获取IP信息
     *
     * @return IP信息
     */
    public String getIP() {
        return mSIPreferences.getString("saveIP", "");
    }

    /**
     * 存储学校信息
     *
     * @param school 学校信息
     */
    public void saveSchool(String school) {
        SharedPreferences.Editor edit = mSIPreferences.edit();
        edit.putString("saveSchool", school);
        edit.apply();
    }

    /**
     * 获取学校信息
     *
     * @return 学校信息
     */
    public String getSchool() {
        return mSIPreferences.getString("saveSchool", "深圳市XX高级中学");
    }

    /**
     * 存储大门名称
     * @param doorName  大门名称
     */
    public void saveDoorName(String doorName){
        SharedPreferences.Editor edit = mSIPreferences.edit();
        edit.putString("saveDoorName", doorName);
        edit.apply();
    }

    /**
     * 获取大门名称
     * @return 大门名称
     */
    public String getDoorName(){
        return mSIPreferences.getString("saveDoorName", "");
    }

    /**
     * 存储客户端IP
     * @param clientIP  大门名称
     */
    public void saveClientIP(String clientIP){
        SharedPreferences.Editor edit = mSIPreferences.edit();
        edit.putString("saveClientIP", clientIP);
        edit.apply();
    }

    /**
     * 获取客户端IP
     * @return 客户端IP
     */
    public String getClientIP(){
        return mSIPreferences.getString("saveClientIP", "");
    }

    public void saveVersion(int serverVersion){
        mSIPreferences.edit().putInt("saveVersion", serverVersion).apply();
    }

    public int getVersion(){
        return mSIPreferences.getInt("saveVersion", 65536);
    }

    /**
     * 保存上一次通讯录同步时间
     * @param lastSyncTime 1970-01-01 00:00:00
     */
    public void putLastSyncTime(String lastSyncTime){
        mSIPreferences.edit().putString("LastSyncTime", lastSyncTime).apply();
    }

    /**
     * 获取上一次通讯录同步时间
     * @return 1970-01-01 00:00:00
     */
    public String getLastSyncTime(){
        return mSIPreferences.getString("LastSyncTime", "1970-01-01 00:00:00");
    }

    /**
     * 存储设备ID
     * @param imei 设备IMEI号（更推荐AndroidID）
     */
    public void putIMEI(String imei){
        mSIPreferences.edit().putString("IMEI", imei).apply();
    }

    /**
     * 获取设备ID
     * @return imei
     */
    public String getIMEI(){
        return mSIPreferences.getString("IMEI", "");
    }

    /**
     * 存储初次启动时间
     */
    public void putInitTime(long time){
        mSIPreferences.edit().putLong("InitTime", time).apply();
    }
    /**
     * 获取初次启动时间
     */
    public long getInitTime(){
        return mSIPreferences.getLong("InitTime", 0);
    }

    /**
     * 存储鉴权
     */
    public void putAuth(boolean success){
        mSIPreferences.edit().putBoolean("AuthInfo", success).apply();
    }
    /**
     * 获取鉴权
     */
    public boolean getAuth(){
        return mSIPreferences.getBoolean("AuthInfo", false);
    }
}
