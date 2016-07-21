package com.hanwen.chinesechat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityAlbum extends FragmentActivity {

    private static final String KEY_INDEX = "KEY_INDEX";
    private ViewPager viewpager;
    private PagerAdapter adapter;
    private TextView tv_count;
    private List<ImageView> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        initData();
    }

    private void initData() {
        String[] paths = getIntent().getStringArrayExtra("photos");
        int index = getIntent().getIntExtra(KEY_INDEX, 0);

        photos = new ArrayList<>();
        for (String path : paths) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setTag(path);
            photos.add(imageView);
        }
        //左下角角标
        tv_count.setText((index + 1) + "/" + paths.length);

        adapter = new MyAdapter();// new ImagePagerAdapter(getSupportFragmentManager(), paths);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(index);
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

    public static void start(Context context, String[] photos1, int index) {
        Intent intent = new Intent(context, ActivityAlbum.class);
        intent.putExtra("photos", photos1);
        intent.putExtra(KEY_INDEX, index);
        context.startActivity(intent);
    }

    private class MyAdapter extends PagerAdapter {
        private ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = photos.get(position);
            CommonUtil.showBitmap(imageView, NetworkUtil.getFullPath((String) imageView.getTag()));
            container.addView(imageView, params);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


}
