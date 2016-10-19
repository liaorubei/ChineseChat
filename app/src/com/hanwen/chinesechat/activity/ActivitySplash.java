package com.hanwen.chinesechat.activity;

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
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.service.AutoUpdateService;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.SystemUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

public class ActivitySplash extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivitySplash";
    private ViewPager viewpager;
    private View iv_splash;
    private View rl_splash;
    private View bt_skip;
    private View bt_enter;
    private LinearLayout ll_indicator;
    private int[] splashImages = new int[]{R.drawable.splash_1, R.drawable.splash_2, R.drawable.splash_3, R.drawable.splash_4, R.drawable.splash_5};
    private static int WHAT_SPLASH = 1;
    private long first;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == WHAT_SPLASH) {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String version = sharedPreferences.getString("versionName", "");
                    String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    sharedPreferences.edit().putString("versionName", versionName).apply();

                    if (!TextUtils.equals(version, versionName)) {
                        guideView();
                    } else {
                        enterMain();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        first = System.currentTimeMillis();


        initView();
        if (NIMClient.getStatus() != StatusCode.LOGINED) {
            signInApp();
        } else {
            handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
        }
/*

        //预加载首页数据
        HttpUtil.post(NetworkUtil.levelAndFolders, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                //保存到数据库
                UrlCache urlCache = new UrlCache(this.getRequestUrl(), responseInfo.result, System.currentTimeMillis());
                ChineseChat.database().cacheInsertOrUpdate(urlCache);

                //再保存文件夹信息
                Response<List<Level>> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Level>>>() {
                }.getType());

                for (Level l : o.info) {
                    for (Folder f : l.Folders) {
                        if (!ChineseChat.database().folderExists(f.Id)) {
                            ChineseChat.database().folderInsert(f);
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
*/

        // 自动升级服务
        Intent service = new Intent(this, AutoUpdateService.class);
        startService(service);
    }

    private void signInApp() {
        SharedPreferences user = getSharedPreferences("user", MODE_PRIVATE);
        String username = user.getString("username", null);
        String password = user.getString("password", null);

        RequestParams params = new RequestParams();
        params.addBodyParameter("username", username);
        params.addBodyParameter("password", password);
        params.addBodyParameter("category", (ChineseChat.isStudent() ? 0 : 1) + "");
        params.addBodyParameter("system", 1 + "");
        params.addBodyParameter("device", SystemUtil.getDeviceName());

        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.userSignIn, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "汉问登录成功: " + responseInfo.result);
                Response<User> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (resp.code == 200) {
                    ChineseChat.CurrentUser = resp.info;
                    signInNim(resp.info.Accid, resp.info.Token);
                } else {
                    handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "汉问登录失败: " + msg);
                CommonUtil.toast(R.string.network_error);
                handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void signInNim(String accid, String token) {
        NIMClient.getService(AuthService.class).login(new LoginInfo(accid, token)).setCallback(new RequestCallback<LoginInfo>() {

            @Override
            public void onSuccess(LoginInfo loginInfo) {
                Log.i(TAG, "云信登录成功: Accid=" + loginInfo.getAccount() + " Token=" + loginInfo.getToken());
                handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "云信登录失败: " + i);
                CommonUtil.toast(R.string.network_error);
                handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "云信登录异常: " + throwable.getMessage());
                CommonUtil.toast(R.string.network_error);
                handler.sendEmptyMessageDelayed(WHAT_SPLASH, 2500 - (System.currentTimeMillis() - first));
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

    private void guideView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, 0, margin, 0);
        for (int i = 0; i < splashImages.length; i++) {
            TextView textView = new TextView(getApplicationContext());
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.selector_splash_indicator);
            textView.setSelected(i == 0);
            ll_indicator.addView(textView, params);
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
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra(ActivityMain.KEY_TAB_INDEX, 1);
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
