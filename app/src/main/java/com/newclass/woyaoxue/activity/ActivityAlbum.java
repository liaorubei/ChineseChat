package com.newclass.woyaoxue.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newclass.woyaoxue.fragment.ImageDetailFragment;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityAlbum extends FragmentActivity {

    private ViewPager viewpager;
    private ImagePagerAdapter adapter;
    private TextView tv_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        initData();
    }

    private void initData() {
        String[] paths = getIntent().getStringArrayExtra("photos");
        //左下角角标
        tv_count.setText("1/" + paths.length);

        adapter = new ImagePagerAdapter(getSupportFragmentManager(), paths);
        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tv_count.setText((position + 1) + "/" + adapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        tv_count = (TextView) findViewById(R.id.tv_count);
    }

    public static void start(Context context, String[] photos1) {
        Intent intent = new Intent(context, ActivityAlbum.class);
        intent.putExtra("photos", photos1);
        context.startActivity(intent);
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public String[] fileList;

        public ImagePagerAdapter(FragmentManager fm, String[] fileList) {
            super(fm);
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList == null ? 0 : fileList.length;
        }

        @Override
        public Fragment getItem(int position) {
            String url = fileList[position];
            return ImageDetailFragment.newInstance(url);
        }
    }

}
