package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.hanwen.chinesechat.fragment.FragmentLineUp;
import com.hanwen.chinesechat.fragment.FragmentListen;
import com.hanwen.chinesechat.fragment.FragmentPerson;
import com.hanwen.chinesechat.receiver.NetworkReceiver;
import com.hanwen.chinesechat.service.DownloadService;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends FragmentActivity implements View.OnClickListener {
    //Monkey测试代码
    //adb shell monkey -p com.hanwen.chinesechat -s 500 --ignore-crashes --ignore-timeouts --monitor-native-crashes -v -v 50000 > E:\log.txt
    //gradlew assemblerelease
    //代码折叠/展开[ctrl shift -+]

    //云信相关
    //App Key: 599551c5de7282b9a1d686ee40abf74c
    //App Secret: 64e52bd091da
    public static final String TAG = "ActivityMain";
    public static final String KEY_TAB_INDEX = "KEY_TAB_INDEX";
    public static final String KEY_DOCUMENT_ID = "KEY_DOCUMENT_ID";
    private static final int REQUEST_CODE_STORAGE = 1;
    private RelativeLayout rl_main;
    private long lastTime = 0;
    private View tv_refresh;
    private BroadcastReceiver receiver;
    private int tabIndex = 1;
    private int documentId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: ");

        Intent intent = getIntent();
        tabIndex = intent.getIntExtra(KEY_TAB_INDEX, 1);
        documentId = intent.getIntExtra(KEY_DOCUMENT_ID, -1);

        initView();

        //check();

        //exportDatabase();

        // 下载任务服务
        Intent sIntent = new Intent(this, DownloadService.class);
        startService(sIntent);

        if (!ChineseChat.isStudent()) {
            receiver = new NetworkReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        }
    }

    private void check() {
        boolean b = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, "check是否有权限: " + b);

        boolean b1 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.i(TAG, "check弹出对话框: " + b1);

        List<String> ps = new ArrayList<>();
        ps.add(Manifest.permission.RECORD_AUDIO);
        ps.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(this, ps.toArray(new String[ps.size()]), REQUEST_CODE_STORAGE);
        if (b) {


        } else {
        }
    }

    private void exportDatabase() {
        File[] files = getFilesDir().getParentFile().listFiles();
        Log.i(TAG, "exportDatabase: " + files.length);
        for (File f : files) {
            Log.i(TAG, "exportDatabase: " + f.getAbsolutePath());
        }

        try {
            FileInputStream fis = new FileInputStream(new File(getFilesDir().getParentFile(), "databases/woyaoxue.db"));
            FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir("database"), "chinesechat.db"));
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            fis.close();
            Log.i(TAG, "exportDatabase: " + fos.getFD().toString());
        } catch (Exception ex) {

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (documentId < 0) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - lastTime) > 2000) {
                    CommonUtil.toast(R.string.MainActivity_one_more_time_quit);
                    lastTime = System.currentTimeMillis();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setTypeface(Typeface.createFromAsset(getAssets(), "font/MATURASC.TTF"));
        tv_refresh = findViewById(R.id.tv_refresh);
        tv_refresh.setOnClickListener(this);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);

        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(ActivityMain.this, getSupportFragmentManager(), R.id.ff_content);

        Bundle argsChat = new Bundle();
        argsChat.putInt("documentId", documentId);
        tabHost.addTab(tabHost.newTabSpec("chat").setIndicator(initIndicator("Chat")), FragmentLineUp.class, argsChat);
        tabHost.addTab(tabHost.newTabSpec("listen").setIndicator(initIndicator("Listen")), FragmentListen.class, null);
        tabHost.addTab(tabHost.newTabSpec("me").setIndicator(initIndicator("Me")), FragmentPerson.class, null);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.i(TAG, "onTabChanged: tabId=" + tabId);
                switch (tabId) {
                    case "chat":
                        tv_refresh.setVisibility(View.VISIBLE);
                        break;
                    case "listen":
                        tv_refresh.setVisibility(View.INVISIBLE);
                        break;
                    case "me":
                        tv_refresh.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });
        tabHost.setCurrentTab(tabIndex);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: ");
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                Log.i(TAG, "Result: " + grantResults[0]);
                break;
        }
    }
}
