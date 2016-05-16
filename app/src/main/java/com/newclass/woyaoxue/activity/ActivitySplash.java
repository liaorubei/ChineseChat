package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.MainActivity;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.Level;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.List;

public class ActivitySplash extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivitySplash";
    private ViewPager viewpager;
    private View iv_splash;
    private View rl_splash;
    private View bt_skip;
    private View bt_enter;
    private LinearLayout ll_indicator;
    private int[] splashImages = new int[]{R.drawable.splash_1, R.drawable.splash_2, R.drawable.splash_3, R.drawable.splash_4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();

        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                loopPlay();
            }
        }.sendEmptyMessageDelayed(1, 2500);

        //预加载首页数据
        HttpUtil.post(NetworkUtil.levelAndFolders, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                //保存到数据库
                UrlCache urlCache = new UrlCache(this.getRequestUrl(), responseInfo.result, System.currentTimeMillis());
                ChineseChat.getDatabase().cacheInsertOrUpdate(urlCache);

                //再保存文件夹信息
                Response<List<Level>> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Level>>>() {
                }.getType());

                for (Level l : o.info) {
                    for (Folder f : l.Folders) {
                        if (!ChineseChat.getDatabase().folderExists(f.Id)) {
                            ChineseChat.getDatabase().folderInsert(f);
                        }
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(getString(R.string.network_error));
            }
        });
    }

    private void initView() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        iv_splash = findViewById(R.id.iv_splash);
        rl_splash = findViewById(R.id.rl_splash);
        bt_skip = findViewById(R.id.bt_skip);
        bt_enter = findViewById(R.id.bt_enter);
        ll_indicator = (LinearLayout) findViewById(R.id.ll_indicator);

        bt_skip.setOnClickListener(this);
        bt_enter.setOnClickListener(this);
    }

    private void loopPlay() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String version = sharedPreferences.getString("versionName", "");
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            sharedPreferences.edit().putString("versionName", versionName).apply();

            if (!TextUtils.equals(version, versionName)) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
                int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(margin, 0, margin, 0);
                for (int i = 0; i < splashImages.length; i++) {
                    TextView textView = new TextView(getApplicationContext());
                    //textView.setText((i + 1) + "");
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundResource(R.drawable.selector_splash_indicator);
                    textView.setSelected(i == 0);
                    ll_indicator.addView(textView, params);
                    Log.i(TAG, "loopPlay: " + i);
                }

                viewpager.setAdapter(new AdapterSplash());
                viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        int childCount = ll_indicator.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            ll_indicator.getChildAt(i).setSelected(i == position);
                        }
                        bt_skip.setVisibility((position + 1) != splashImages.length ? View.VISIBLE : View.INVISIBLE);
                        bt_enter.setVisibility((position + 1) == splashImages.length ? View.VISIBLE : View.INVISIBLE);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });


                Animation animation = new TranslateAnimation(0, 0, displayMetrics.heightPixels, 0);
                animation.setDuration(500);
                animation.setFillAfter(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        rl_splash.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                rl_splash.startAnimation(animation);

                Animation animationRemove = new TranslateAnimation(0, 0, 0, -displayMetrics.heightPixels);
                animationRemove.setDuration(500);
                animationRemove.setFillAfter(true);
                iv_splash.startAnimation(animationRemove);
            } else {
                enterMain();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_skip:
            case R.id.bt_enter:
                enterMain();
                break;
        }
    }

    private void enterMain() {
        if (!ChineseChat.isStudent() && NIMClient.getStatus() != StatusCode.LOGINED) {
            Intent intent = new Intent(this, ActivitySignIn.class);
            intent.putExtra("enter_main", true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private class AdapterSplash extends PagerAdapter {
        @Override
        public int getCount() {
            return splashImages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv_splash = new ImageView(getApplicationContext());
            iv_splash.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv_splash.setImageResource(splashImages[position]);
            container.addView(iv_splash, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return iv_splash;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
