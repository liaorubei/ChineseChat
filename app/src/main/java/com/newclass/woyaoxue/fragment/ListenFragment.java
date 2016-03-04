package com.newclass.woyaoxue.fragment;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Level;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListenFragment extends Fragment {
    private static final String TAG = "ListenFragment";
    private Gson gson = new Gson();
    private LinearLayout ll_ctrl;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private MyAdapter adapter;
    int i = 0;
    private List<Level> levels;

    public ListenFragment() {
        Log.i(TAG, "ListenFragment: " + getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach: " + getActivity());
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: this=" + this);
        Log.i(TAG, "onCreateView: Activity=" + getActivity());
        return inflater.inflate(R.layout.fragment_listen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        android.util.Log.i(TAG, "onViewCreated: ");
        ll_ctrl = (LinearLayout) view.findViewById(R.id.ll_ctrl);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();
        adapter = new MyAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int childCount = ll_ctrl.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    CheckBox childAt = (CheckBox) ll_ctrl.getChildAt(i);
                    childAt.setChecked(position == i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (levels == null) {
            HttpUtil.post(NetworkUtil.levelAndFolders, null, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<List<Level>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Level>>>() {
                    }.getType());

                    levels = resp.info;
                    Collections.sort(levels, new Comparator<Level>() {
                        @Override
                        public int compare(Level lhs, Level rhs) {
                            return Integer.valueOf(rhs.Sort).compareTo(lhs.Sort);
                        }
                    });

                    initTabs();
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: " + msg);
                    CommonUtil.toast("网络异常");
                }
            });
        } else {
            initTabs();
        }
    }

    private void initTabs() {

        ll_ctrl.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        ColorStateList colors = getResources().getColorStateList(R.color.selector_text_normal);
        //添加等级数据
        for (int i = 0; i < levels.size(); i++) {
            CheckBox child = new CheckBox(getActivity());
            child.setGravity(Gravity.CENTER);
            child.setText(levels.get(i).Name);
            child.setTextColor(colors);
            child.setButtonDrawable(null);
            ll_ctrl.addView(child, params);
            FragmentFolder fragmentFolder = new FragmentFolder();
            Bundle args = new Bundle();
            args.putString("level", gson.toJson(levels.get(i)));
            fragmentFolder.setArguments(args);
            fragments.add(fragmentFolder);
        }

        //添加已下载项
        CheckBox child = new CheckBox(getActivity());
        child.setGravity(Gravity.CENTER);
        child.setText("已下载");
        child.setTextColor(colors);
        child.setButtonDrawable(null);
        ll_ctrl.addView(child, params);
        fragments.add(new FragmentDownload());
        adapter.notifyDataSetChanged();

        //添加点击事件
        int childCount = ll_ctrl.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final int position = i;
            CheckBox childAt = (CheckBox) ll_ctrl.getChildAt(i);
            childAt.setChecked(position == 0);
            childAt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(position);
                }
            });
        }
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.i(TAG, "onViewStateRestored: " + savedInstanceState);
        Log.i(TAG, "onViewStateRestored: CurrentItem=" + viewPager.getCurrentItem());
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: " + outState);
        outState.putString("sldk", "12306+");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
    }

    private class MyAdapter extends FragmentPagerAdapter {

        private List<Fragment> list;

        public MyAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.list = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.list.get(position);
        }

        @Override
        public int getCount() {
            return this.list.size();
        }
    }
}
