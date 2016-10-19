package com.hanwen.chinesechat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.util.Log;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ActivityAlbum extends FragmentActivity {

    private static final String KEY_INDEX = "KEY_TAB_INDEX";
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
        tv_count.setVisibility(paths.length > 1 ? View.VISIBLE : View.INVISIBLE);

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

    /**
     * 打开相册界面
     *
     * @param context       上下文
     * @param absolutePaths 照片绝对路径数组
     * @param index         指定相片的索引
     */
    public static void start(Context context, String[] absolutePaths, int index) {
        Intent intent = new Intent(context, ActivityAlbum.class);
        intent.putExtra("photos", absolutePaths);
        intent.putExtra(KEY_INDEX, index);
        context.startActivity(intent);
    }

    private static final String TAG = "ActivityAlbum";

    private class MyAdapter extends PagerAdapter {
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
            final View inflate = getLayoutInflater().inflate(R.layout.zoom_image_view, null);
            final PhotoView zoom = (PhotoView) inflate.findViewById(R.id.imageView);
            final View pb_loading = inflate.findViewById(R.id.pb_loading);

            zoom.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });

            ImageView imageView = photos.get(position);
            new BitmapUtils(ActivityAlbum.this).display(imageView, (String) imageView.getTag(), new BitmapLoadCallBack<ImageView>() {
                @Override
                public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                    zoom.setImageBitmap(bitmap);
                    pb_loading.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                    Log.i(TAG, "onLoadFailed: ");
                }
            });

            container.addView(inflate);
            return inflate;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
