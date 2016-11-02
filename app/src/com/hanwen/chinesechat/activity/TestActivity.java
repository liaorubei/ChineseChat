package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.fragment.FragmentChatHskk;
import com.hanwen.chinesechat.fragment.FragmentCourse;
import com.hanwen.chinesechat.fragment.FragmentCourseNest;
import com.hanwen.chinesechat.fragment.FragmentTopics;
import com.hanwen.chinesechat.util.Log;

public class TestActivity extends FragmentActivity {
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, FragmentChatHskk.newInstance(ChineseChat.isStudent() ? FragmentChatHskk.OPEN_MODE_PICK : FragmentChatHskk.OPEN_MODE_SHOW, 22)).commit();
    }

}
