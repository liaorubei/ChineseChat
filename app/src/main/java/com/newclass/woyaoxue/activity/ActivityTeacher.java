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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityTeacher extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivityTeacher";
    private ImageView iv_avatar;
    private TextView tv_nickname;
    private TextView tv_username;
    private TextView tv_about;
    private ViewPager viewpager;
    private List<ImageView> photos;
    private ImageView iv_call;
    private User user;
    private Gson gson = new Gson();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        initView();
        initData();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        iv_call = (ImageView) findViewById(R.id.iv_call);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_about = (TextView) findViewById(R.id.tv_about);
        viewpager = (ViewPager) findViewById(R.id.viewpager);

        iv_call.setOnClickListener(this);
        //在学生端才可以拨打
        iv_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);
    }

    public static void start(Context context, User user) {
        Intent intent = new Intent(context, ActivityTeacher.class);
        intent.putExtra("user", new Gson().toJson(user));
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        user = new Gson().fromJson(intent.getStringExtra("user"), User.class);

        CommonUtil.showIcon(this, iv_avatar, user.Avatar);
        tv_nickname.setText(user.Nickname);
        tv_username.setText(user.Username);
        tv_about.setText(user.About);
        iv_call.setEnabled(user.IsEnable && user.IsOnline);

        photos = new ArrayList<>();
        adapter = new MyAdapter();
        viewpager.setAdapter(adapter);

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("username", user.Username);
        HttpUtil.post(NetworkUtil.nimuserGetPhotosByUsername, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (o.code == 200) {
                    photos.clear();
                    for (String path : o.info.Photos) {
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setTag(path);
                        photos.add(imageView);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.iv_call: {
                //如果没有登录,那么要求登录
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(this, ActivitySignIn.class));
                    return;
                }

                if (!user.IsOnline) {
                    CommonUtil.toast(R.string.FragmentChoose_offline);
                    return;
                }

                if (!user.IsEnable) {
                    CommonUtil.toast(R.string.FragmentChoose_busy);
                    return;
                }

                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("id", ChineseChat.CurrentUser.Id);
                parameters.add("target", user.Id);
                HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());
                        if (resp.code == 200) {
                            ActivityCall.start(ActivityTeacher.this, user.Id, user.Accid, user.Avatar, user.Nickname, ActivityCall.CALL_TYPE_AUDIO);
                        } else {
                            CommonUtil.toast(resp.desc);
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "onFailure: " + msg);
                    }
                });
            }
            break;
        }
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
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            CommonUtil.showIcon(getApplicationContext(), imageView, (String) imageView.getTag());
            container.addView(imageView, params);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
