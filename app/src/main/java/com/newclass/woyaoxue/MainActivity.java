package com.newclass.woyaoxue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentTabHost;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatRingerConfig;
import com.newclass.woyaoxue.activity.ActivityTake;
import com.newclass.woyaoxue.fragment.FragmentChoose;
import com.newclass.woyaoxue.fragment.FragmentLineUp;
import com.newclass.woyaoxue.fragment.FragmentPerson;
import com.newclass.woyaoxue.fragment.FragmentListen;
import com.newclass.woyaoxue.service.AutoUpdateService;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends FragmentActivity {
    // Monkey测试代码
    // adb shell monkey -p com.voc.woyaoxue -s 500 --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v 10000 > E:\log.txt
    // gradlew assemblerelease
    //代码折叠/展开[ctrl shift -+]

    //云信相关
    //App Key: 599551c5de7282b9a1d686ee40abf74c
    //App Secret: 64e52bd091da
    protected static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();

        //初始化云信铃声及添加来电观察者(注:不要放在Application里面,有些机子会出现异常)
        enableAVChat();


        // 下载任务服务
        Intent sIntent = new Intent(this, DownloadService.class);
        startService(sIntent);
        // 自动升级服务
        Intent service = new Intent(this, AutoUpdateService.class);
        startService(service);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager WM = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        WM.getDefaultDisplay().getMetrics(metrics);
        Log.i(TAG, "onCreate: heightPixels=" + metrics.density);
        Log.i(TAG, "onCreate: scaledDensity=" + metrics.scaledDensity);
    }

    private void enableAVChat() {
        setupAVChat();
        registerAVChatIncomingCallObserver(true);
    }

    private void setupAVChat() {
        AVChatRingerConfig config = new AVChatRingerConfig();
        config.res_connecting = R.raw.avchat_connecting;
        config.res_no_response = R.raw.avchat_no_response;
        config.res_peer_busy = R.raw.avchat_peer_busy;
        config.res_peer_reject = R.raw.avchat_peer_reject;
        config.res_ring = R.raw.avchat_ring;
        AVChatManager.getInstance().setRingerConfig(config); // 铃声配置
    }

    private void registerAVChatIncomingCallObserver(boolean register) {

        // 注册来电监听
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData chatData) {
                ActivityTake.start(MainActivity.this, chatData);
            }
        }, register);
    }

    private void initView() {
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(MainActivity.this, getSupportFragmentManager(), R.id.ff_content);
        tabHost.addTab(tabHost.newTabSpec("chat").setIndicator(initIndicator("Chat")), MyApplication.isStudent() ? FragmentChoose.class : FragmentLineUp.class, null);
        tabHost.addTab(tabHost.newTabSpec("listen").setIndicator(initIndicator("Listen")), FragmentListen.class, null);
        tabHost.addTab(tabHost.newTabSpec("me").setIndicator(initIndicator("Me")), FragmentPerson.class, null);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.i(TAG, "onTabChanged: tabId=" + tabId);
            }
        });
        tabHost.setCurrentTabByTag("listen");
    }

    private View initIndicator(String person) {
        View inflate = getLayoutInflater().inflate(R.layout.tabhost_indicator, null);
        TextView tabhost_title = (TextView) inflate.findViewById(R.id.tabhost_title);
        tabhost_title.setText(person);
        ImageView image = (ImageView) inflate.findViewById(R.id.tabhost_image);
        switch (person) {
            case "Me":
                image.setImageResource(R.drawable.selector_tab_person);
                break;
            case "Listen":
                image.setImageResource(R.drawable.selector_tab_listen);
                break;
            case "Chat":
                image.setImageResource(R.drawable.selector_tab_chat);
                break;
        }
        return inflate;
    }

}
