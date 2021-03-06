package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.HsLevel;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.fragment.FragmentThemes;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

public class ActivityTheme extends FragmentActivity implements FragmentThemes.OnFragmentInteractionListener {
    private static final String TAG = "ActivityTheme";
    private static final String key_current_theme = "key_current_theme";
    private LinearLayout ll_indicator;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private MyPagerAdapter adapter;
    private Gson gson = new Gson();
    private int[] colors = new int[]{Color.parseColor("#BCE0AF"), Color.parseColor("#A1C9D6"), Color.parseColor("#E0AFE0"), Color.parseColor("#E0CBAF")};

    private Theme currentTheme = null;
    private View iv_menu;
    private View ll_context;
    private View pd_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        initView();
        initData();
    }

    public static void start(Activity context, String themeJson) {
        Intent intent = new Intent(context, ActivityTheme.class);
        intent.putExtra(key_current_theme, themeJson);
        context.startActivityForResult(intent, ActivityChat.REQUEST_CODE_THEME);
    }

    private void initData() {
        String stringExtra = getIntent().getStringExtra(key_current_theme);
        currentTheme = gson.fromJson(stringExtra, Theme.class);
        if (currentTheme == null) {
            currentTheme = new Theme();
            currentTheme.Id = 0;
            currentTheme.Name = "异常主题";
        }

        ll_context.setVisibility(View.INVISIBLE);
        HttpUtil.post(NetworkUtil.hsLevelAndTheme, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<HsLevel>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<HsLevel>>>() {
                }.getType());

                if (resp.code == 200) {
                    int currentItem = 0;//当前ViewPager的选中项,选中页
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    for (int i = 0; i < resp.info.size(); i++) {
                        final int post = i;
                        HsLevel h = resp.info.get(i);
                        for (Theme t : h.Theme) {
                            if (t.Id == currentTheme.Id) {
                                currentItem = i;
                            }
                        }

                        h.Color = colors[i];
                        TextView textView = new TextView(ActivityTheme.this);
                        textView.setGravity(Gravity.CENTER);
                        textView.setText(h.Name);
                        textView.setSelected(i == 0);
                        textView.setTextColor(getResources().getColorStateList(R.color.selector_text_normal));
                        textView.setBackgroundResource(R.drawable.selector_hslevels);
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewPager.setCurrentItem(post);
                            }
                        });
                        ll_indicator.addView(textView, params);

                        FragmentThemes fragmentThemes = new FragmentThemes();
                        Bundle bundle = new Bundle();
                        bundle.putString("HsLevel", gson.toJson(h));
                        fragmentThemes.setArguments(bundle);
                        fragments.add(fragmentThemes);
                    }
                    adapter.notifyDataSetChanged();
                    viewPager.setCurrentItem(currentItem);
                    ll_context.setVisibility(View.VISIBLE);
                    pd_loading.setVisibility(View.INVISIBLE);
                } else {
                    ll_context.setVisibility(View.INVISIBLE);
                    pd_loading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                ll_context.setVisibility(View.INVISIBLE);
                pd_loading.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initView() {
        //标题
        ImageView iv_home = (ImageView) findViewById(R.id.iv_home);
        iv_menu = findViewById(R.id.iv_menu);
        iv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTheme == null) {
                    CommonUtil.toast(R.string.ActivityTheme_no_topic);
                } else {
                    Intent data = new Intent();
                    data.putExtra("theme", gson.toJson(currentTheme));
                    setResult(FragmentThemes.RESULTCODE_CHOOSE, data);
                    finish();
                }
            }
        });

        //内容页和加载页
        ll_context = findViewById(R.id.ll_content);
        pd_loading = findViewById(R.id.pb_loading);

        ll_indicator = (LinearLayout) findViewById(R.id.ll_indicator);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int count = ll_indicator.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childAt = ll_indicator.getChildAt(i);
                    childAt.setSelected(position == i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Theme theme, Fragment item) {
        iv_menu.setVisibility(View.VISIBLE);
        currentTheme = theme;

        for (Fragment f : fragments) {
            if (!f.equals(item)) {
                f.onResume();
            }
        }
    }

    @Override
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int location) {
            return fragments.get(location);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
