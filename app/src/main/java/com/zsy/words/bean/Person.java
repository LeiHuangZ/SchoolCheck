package com.zsy.words.bean;
/*
 * 文件名:     Person
 * 创建者:     阿钟
 * 创建时间:   2016/11/17 19:07
 * 描述:       封装联系人列表信息
 */

import com.zsy.words.utils.PinYinUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Person {
    //姓名
    private String name;
    //拼音
    private String pinyin;
    //拼音首字母
    private String headerWord;
    //photo_number
    @Id
    @NotNull
    private String mPhoto;
    public Person(String name){
        this.name = name;
        this.pinyin = PinYinUtils.getPinyin(name);
        headerWord = pinyin.substring(0, 1);
    }

    public Person(String name, String photo) {
        mPhoto = photo;
        this.name = name;
        this.pinyin = PinYinUtils.getPinyin(name);
        headerWord = pinyin.substring(0, 1);
    }

    @Generated(hash = 393937833)
    public Person(String name, String pinyin, String headerWord,
            @NotNull String mPhoto) {
        this.name = name;
        this.pinyin = pinyin;
        this.headerWord = headerWord;
        this.mPhoto = mPhoto;
    }

    @Generated(hash = 1024547259)
    public Person() {
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.pinyin = PinYinUtils.getPinyin(name);
        this.name = name;
        headerWord = pinyin.substring(0, 1);
    }

    public String getHeaderWord() {
        return headerWord;
    }

    void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    void setHeaderWord(String headerWord) {
        this.headerWord = headerWord;
    }

    public String getMPhoto() {
        return this.mPhoto;
    }

    public void setMPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }
}
