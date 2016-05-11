package com.newclass.woyaoxue.activity;

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
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.HsLevel;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.fragment.FragmentThemes;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityTheme extends FragmentActivity implements FragmentThemes.OnFragmentInteractionListener {
    private static final String TAG = "ActivityTheme";
    private LinearLayout ll_indicator;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private FragmentPagerAdapter adapter;
    private Gson gson = new Gson();
    private int[] colors = new int[]{Color.parseColor("#BCE0AF"), Color.parseColor("#A1C9D6"), Color.parseColor("#E0AFE0"), Color.parseColor("#E0CBAF")};

    private FragmentThemes.ViewModel currentTheme = null;
    private View iv_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        initView();
        initData();
    }

    private void initData() {
        HttpUtil.post(NetworkUtil.hsLevelAndTheme, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<HsLevel>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<HsLevel>>>() {
                }.getType());

                if (resp.code == 200) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    for (int i = 0; i < resp.info.size(); i++) {
                        final int post = i;
                        HsLevel h = resp.info.get(i);
                        h.Color = colors[i];
                        TextView textView = new TextView(ActivityTheme.this);
                        textView.setGravity(Gravity.CENTER);
                        textView.setText(h.Name);
                        textView.setBackgroundResource(R.drawable.selector_levels);
                        textView.setSelected(i == 0);
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
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
            }
        });
    }

    private void initView() {
        //标题
        ImageView iv_home = (ImageView) findViewById(R.id.iv_home);
        iv_menu = findViewById(R.id.iv_menu);
        iv_menu.setVisibility(View.INVISIBLE);
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
                    CommonUtil.toast("还没有选择主题");
                } else {
                    Intent data = new Intent();
                    data.putExtra("theme", gson.toJson(currentTheme));
                    setResult(FragmentThemes.RESULTCODE_CHOOSE, data);
                    finish();
                }
            }
        });

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
    public void onFragmentInteraction(FragmentThemes.ViewModel theme) {
        if (currentTheme != null) {
            currentTheme.isChecked = false;
        }
        this.currentTheme = theme;
        for (Fragment f : fragments) {
            f.onResume();
        }

        iv_menu.setVisibility(currentTheme == null ? View.INVISIBLE : View.VISIBLE);
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
