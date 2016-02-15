package com.newclass.woyaoxue.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Level;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.fragment.FolderFragment;
import com.newclass.woyaoxue.fragment.FragmentDownload;
import com.newclass.woyaoxue.util.ConstantsUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.LazyViewPager.OnPageChangeListener;
import com.voc.woyaoxue.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ContentViewListen extends ContentView {
    private static final String TAG = "ContentViewListen";
    private MyAdapter adapter;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private Gson gson = new Gson();
    private List<Level> levels = new ArrayList<Level>();
    private RadioGroup ll_levels;

    private LazyViewPager viewpager;
    private FragmentManager fragmentManager;

    public ContentViewListen(Context context, FragmentManager fm) {
        super(context);
        this.fragmentManager = fm;
        initData();
    }

    public void initData() {
        // 由于FragmentPagerAdapter要求使用兼容包的FragmentManager,所以相关设置的代码不放onCreateSuccessView
        adapter = new MyAdapter(this.fragmentManager);
        viewpager.setAdapter(adapter);
        viewpager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: " + position);
                int childCount = ll_levels.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    TextView childAt = (TextView) ll_levels.getChildAt(i);
                    childAt.setTextColor(position == i ? ConstantsUtil.ColorOne : ConstantsUtil.ColorTwo);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        HttpUtil.post(NetworkUtil.levelSelect, null, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException error, String msg) {
                showView(ViewState.FAILURE);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<Level>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Level>>>() {
                }.getType());
                if (resp.code == 200) {
                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, RadioGroup.LayoutParams.MATCH_PARENT, 1);
                    params.gravity = Gravity.CENTER;

                    levels = resp.info;
                    Collections.sort(levels, new Comparator<Level>() {

                        @Override
                        public int compare(Level lhs, Level rhs) {
                            return Integer.valueOf(rhs.Sort).compareTo(lhs.Sort);
                        }
                    });


                    for (int i = 0; i < levels.size(); i++) {
                        Level level = levels.get(i);

                        TextView child = new TextView(getContext());
                        //  child.
                        // child.setButtonDrawable(android.R.color.transparent);
                        child.setGravity(Gravity.CENTER);
                        child.setText(level.Name);
                        child.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        child.setTextColor(i == 0 ? ConstantsUtil.ColorOne : ConstantsUtil.ColorTwo);
                        ll_levels.addView(child, i, params);
                        FolderFragment folderFragment = new FolderFragment();
                        folderFragment.setLevelId(level.Id);
                        fragments.add(folderFragment);
                    }
                    fragments.add(new FragmentDownload());

                    adapter.notifyDataSetChanged();

                    int childCount = ll_levels.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        final int position = i;
                        TextView childAt = (TextView) ll_levels.getChildAt(i);
                        childAt.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewpager.setCurrentItem(position);
                            }
                        });
                    }
                }
                showView(ViewState.SUCCESS);
            }
        });

    }

    @Override
    public View onCreateSuccessView() {
        View inflate = View.inflate(getContext(), R.layout.contentview_listen, null);
        viewpager = (LazyViewPager) inflate.findViewById(R.id.viewpager);
        ll_levels = (RadioGroup) inflate.findViewById(R.id.ll_levels);
        return inflate;
    }

    private class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
    }

}
