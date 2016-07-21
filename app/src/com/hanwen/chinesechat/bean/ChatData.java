package com.hanwen.chinesechat.bean;

import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

/**
 * Created by 儒北 on 2016-07-20.
 */
public class ChatData implements AVChatData {

    private long chatId;
    private String accid;
    private AVChatType chatType;
    private String extra;
    private String pushSound;

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public void setChatType(AVChatType chatType) {
        this.chatType = chatType;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setPushSound(String pushSound) {
        this.pushSound = pushSound;
    }

    @Override
    public long getChatId() {
        return this.chatId;
    }

    @Override
    public String getAccount() {
        return this.accid;
    }

    @Override
    public AVChatType getChatType() {
        return this.chatType;
    }

    @Override
    public long getTimeTag() {
        return 0;
    }

    @Override
    public String getExtra() {
        return this.extra;
    }

    @Override
    public String getPushSound() {
        return this.pushSound;
    }

    @Override
    public String toString() {
        return "ChatData{" +
                "Account='" + accid + '\'' +
                ", chatType=" + chatType +
                ", chatId=" + chatId +
                '}';
    }
}
