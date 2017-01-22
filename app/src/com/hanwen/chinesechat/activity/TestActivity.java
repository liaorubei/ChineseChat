package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.fragment.FragmentChatHskk;
import com.hanwen.chinesechat.fragment.FragmentCourse;
import com.hanwen.chinesechat.fragment.FragmentCourseNest;
import com.hanwen.chinesechat.fragment.FragmentHaveDownloaded;
import com.hanwen.chinesechat.fragment.FragmentNews;
import com.hanwen.chinesechat.fragment.FragmentTopics;
import com.hanwen.chinesechat.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TestActivity extends FragmentActivity {
    private static final String TAG = "TestActivity";
    private ListView listView;

    String[] strings = new String[100];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        for (int i = 0; i < strings.length; i++) {
            strings[i] = "String " + i;
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: " + this);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: " + this);
        super.onDestroy();
    }
}
