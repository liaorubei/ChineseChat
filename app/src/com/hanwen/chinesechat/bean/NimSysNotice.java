package com.hanwen.chinesechat.bean;

import com.google.gson.JsonElement;

public class NimSysNotice<T> {
    public static final int NoticeType_Card = 0;//发送主题
    public static final int NoticeType_Call = 1;//发送话中话通知
    public static final int NoticeType_Chat = 2;//发送通话创建通知,通知对方,通话记录已经创建成功
    public static final int NOTICE_TYPE_COURSE = 3;//发送课程Id类型
    public static final int NoticeType_Hskk = 4;//发送HSKK通知
    public T info;
    public int type;
}
