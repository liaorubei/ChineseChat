package com.hanwen.chinesechat.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.fragment.FragmentChoose;
import com.hanwen.chinesechat.fragment.FragmentLineUp;
import com.hanwen.chinesechat.fragment.FragmentListen;
import com.hanwen.chinesechat.fragment.FragmentPerson;
import com.hanwen.chinesechat.fragment.FragmentTextBook;
import com.hanwen.chinesechat.receiver.NetworkReceiver;
import com.hanwen.chinesechat.service.DownloadService;
import com.hanwen.chinesechat.service.TeacherAutoRefreshService;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;

public class ActivityMain extends FragmentActivity implements View.OnClickListener {
    //Monkey测试代码
    //adb shell monkey -p com.hanwen.chinesechat -s 500 --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v 50000 > E:\log.txt
    //gradlew assemblerelease
    //代码折叠/展开[ctrl shift -+]

    //云信相关
    //App Key: 599551c5de7282b9a1d686ee40abf74c
    //App Secret: 64e52bd091da
    protected static final String TAG = "ActivityMain";
    private TextView tv_delete;
    private RelativeLayout rl_main;
    private long lastTime = 0;
    private View tv_refresh;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //初始化云信铃声及添加来电观察者(注:不要放在Application里面,有些机子会出现异常)
        // enableAVChat();
        //文件夹情况
/*        File[] files = getFilesDir().getParentFile().listFiles();
        for (File f : files) {
            Log.i(TAG, "onCreate: File=" + f.getAbsolutePath());

            File[] files1 = f.listFiles();
            for (File f1 : files1) {
                Log.i(TAG, "onCreate: __File=" + f1.getAbsolutePath());
            }
        }*/

        // 下载任务服务
        Intent sIntent = new Intent(this, DownloadService.class);
        startService(sIntent);

        if (!ChineseChat.isStudent()) {
            receiver = new NetworkReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        }
        Intent i = new Intent(this, TeacherAutoRefreshService.class);
        startService(i);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - lastTime) > 2000) {
                CommonUtil.toast(R.string.MainActivity_one_more_time_quit);
                lastTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setTypeface(Typeface.createFromAsset(getAssets(), "font/MATURASC.TTF"));
        tv_refresh = findViewById(R.id.tv_refresh);
        tv_delete = (TextView) findViewById(R.id.tv_delete);
        tv_refresh.setOnClickListener(this);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);

        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(ActivityMain.this, getSupportFragmentManager(), R.id.ff_content);
        tabHost.addTab(tabHost.newTabSpec("chat").setIndicator(initIndicator("Chat")), ChineseChat.isStudent() ? FragmentChoose.class : FragmentLineUp.class, null);
        tabHost.addTab(tabHost.newTabSpec("listen").setIndicator(initIndicator("Listen")), FragmentListen.class, null);
        tabHost.addTab(tabHost.newTabSpec("me").setIndicator(initIndicator("Me")), FragmentPerson.class, null);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.i(TAG, "onTabChanged: tabId=" + tabId);
                switch (tabId) {
                    case "chat":
                        tv_refresh.setVisibility(View.VISIBLE);
                        tv_delete.setVisibility(View.INVISIBLE);
                        break;
                    case "listen":
                        tv_refresh.setVisibility(View.INVISIBLE);
                        tv_delete.setVisibility(View.INVISIBLE);
                        break;
                    case "me":
                        tv_refresh.setVisibility(View.INVISIBLE);
                        tv_delete.setVisibility(View.INVISIBLE);
                        break;
                }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_refresh:
                Fragment chat = getSupportFragmentManager().findFragmentByTag("chat");
                chat.onResume();
                break;
            case R.id.delete:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
