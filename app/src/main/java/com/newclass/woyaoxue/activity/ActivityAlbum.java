package com.newclass.woyaoxue.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newclass.woyaoxue.util.CommonUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityAlbum extends Activity {

    private ViewPager viewpager;
    private MyAdapter adapter;
    private List<ImageView> photos;
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
        photos = new ArrayList<>();
        for (String path : paths) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            CommonUtil.showIcon(getApplicationContext(), imageView, path);
            photos.add(imageView);
        }

        //左下角角标
        tv_count.setText("1/" + paths.length);
        adapter = new MyAdapter();
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
            container.addView(imageView, params);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}