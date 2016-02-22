package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.HsLevel;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.fragment.FragmentThemes;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.LazyViewPager;
import com.voc.woyaoxue.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThemeActivity extends FragmentActivity {
    private LinearLayout ll_indicator;
    private LazyViewPager viewPager;
    private List<Fragment> fragments;
    private FragmentPagerAdapter adapter;
    private Gson gson = new Gson();
    private LinearLayout.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        initView();
        initData();
        params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
    }

    private void initData() {
        HttpUtil.post(NetworkUtil.hsLevelAndTheme, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<HsLevel>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<HsLevel>>>() {
                }.getType());

                if (resp.code == 200) {
                    for (int i = 0; i < resp.info.size(); i++) {
                        final int post = i;
                        HsLevel h = resp.info.get(i);
                        TextView textView = new TextView(ThemeActivity.this);
                        textView.setGravity(Gravity.CENTER);
                        textView.setText(h.Name);
                        textView.setBackgroundResource(R.drawable.selector_levels);
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
                }

            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });

    }

    private void initView() {
        ll_indicator = (LinearLayout) findViewById(R.id.ll_indicator);
        viewPager = (LazyViewPager) findViewById(R.id.viewpager);
        fragments = new ArrayList<Fragment>();
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
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
