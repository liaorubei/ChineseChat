package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.UrlCache;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;

public class FragmentListen extends Fragment {
    private static final String TAG = "FragmentListen";
    private ProgressBar pb_loading;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private FragmentPagerAdapter adapter;
    private UrlCache urlCache;
    private ViewPager.OnPageChangeListener listener;
    private LinearLayout ll_indicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + viewPager);
        super.onCreate(savedInstanceState);
        urlCache = ChineseChat.database().cacheSelectByUrl(NetworkUtil.levelAndFolders);
        adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int childCount = ll_indicator.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ll_indicator.getChildAt(i).setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: " + viewPager);
        return inflater.inflate(R.layout.fragment_listen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        ll_indicator = (LinearLayout) view.findViewById(R.id.ll_indicator);
        ll_indicator.getChildAt(0).setSelected(true);
        ll_indicator.setVisibility(View.INVISIBLE);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(listener);


        //如果数据是空数据,加载新数据
        if (urlCache == null) {
            //Log.i(TAG, "onViewCreated: 缓存是空的");
            requestLatestData();
        }
        //如果数据是10分钟之前的,也加载新数据
        else if (urlCache.UpdateAt < (System.currentTimeMillis() - 10 * 60 * 1000)) {
            //Log.i(TAG, "onViewCreated: 缓存是旧的");
            requestLatestData();
        }
        //如果数据不为空,而且最近才请求过网络的,那么使用缓存数据
        else {
            //Log.i(TAG, "onViewCreated: 缓存是新的");
            parseJsonData(urlCache.Json);
        }
        Log.i(TAG, "onViewCreated: " + viewPager);
    }

    private void parseJsonData(String json) {
        Gson gson = new Gson();
        Response<ArrayList<Level>> resp = gson.fromJson(json, new TypeToken<Response<ArrayList<Level>>>() {}.getType());
        if (resp.code == 200 && resp.info.size() > 0) {
            pb_loading.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            ll_indicator.setVisibility(View.VISIBLE);

            fragments.clear();

            //未下载
            FragmentLevels fragmentLevelsRemote = new FragmentLevels();
            Bundle argsRemote = new Bundle();
            argsRemote.putParcelableArrayList("levels", resp.info);
            fragmentLevelsRemote.setArguments(argsRemote);
            fragments.add(fragmentLevelsRemote);

            //已下载
            fragments.add(new FragmentHaveDownloaded());

            //通知界面更新数据
            adapter.notifyDataSetChanged();

            //保存等级数据及文件夹数据到数据库,方便在已经下载列表中显示出所属等级,文件夹,以及排序
            for (Level l : resp.info) {
                ChineseChat.database().levelInsertOrReplace(l);
                if (l.Folders != null) {
                    for (Folder f : l.Folders) {
                        ChineseChat.database().folderInsertOrReplace(f);
                    }
                }
            }

        } else {

        }
    }

    private void requestLatestData() {
        HttpUtil.post(NetworkUtil.levelAndFolders, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                //缓存数据
                urlCache = new UrlCache(this.getRequestUrl(), responseInfo.result, System.currentTimeMillis());
                ChineseChat.database().cacheInsertOrUpdate(urlCache);

                //解析数据
                parseJsonData(urlCache.Json);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: msg=" + msg + " error=" + error.getMessage());
                if (urlCache != null) {
                    parseJsonData(urlCache.Json);
                }
            }
        });
    }

}
