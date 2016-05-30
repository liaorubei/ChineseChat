package com.newclass.woyaoxue.bean;

public class NimSysNotice<T> {
    public static final int NoticeType_Card = 0;//发送主题
    public static final int NoticeType_Call = 1;//发送话中话通知
    public static final int NoticeType_Chat = 2;//发送通话创建通知,通知对方,通话记录已经创建成功
    public T info;
    public int type;
}
