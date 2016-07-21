package com.hanwen.chinesechat.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import java.util.Set;

public class ActivityProfile extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivityProfile";
    private ImageView iv_avatar;
    private TextView tv_nickname;
    private TextView tv_location;
    private TextView tv_about, tv_school, tv_language, tv_hobby;
    private LinearLayout ll_album;

    private ImageView iv_call;
    private User user;
    private Gson gson = new Gson();
    private Set<String> photos1;
    private ProgressDialog progressDialog;
    private int padding = 0;
    private int size = 0;
    private boolean IsEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        initData();
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        iv_call = (ImageView) findViewById(R.id.iv_call);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        tv_location = (TextView) findViewById(R.id.tv_location);
        tv_about = (TextView) findViewById(R.id.tv_about);
        tv_school = (TextView) findViewById(R.id.tv_school);
        tv_language = (TextView) findViewById(R.id.tv_language);
        tv_hobby = (TextView) findViewById(R.id.tv_hobby);
        ll_album = (LinearLayout) findViewById(R.id.ll_album);
        //viewpager = (ViewPager) findViewById(R.id.viewpager);

        iv_call.setOnClickListener(this);
        //在学生端才可以拨打
        iv_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.INVISIBLE);
    }

    public static void start(Context context, User user, boolean isEnable) {
        Intent intent = new Intent(context, ActivityProfile.class);
        intent.putExtra("user", new Gson().toJson(user));
        intent.putExtra("IsEnable", isEnable);
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        user = new Gson().fromJson(intent.getStringExtra("user"), User.class);
        IsEnable = intent.getBooleanExtra("IsEnable", false);

        CommonUtil.showIcon(this, iv_avatar, user.Avatar);
        tv_nickname.setText(user.Nickname);
        tv_location.setText(user.Spoken);
        tv_about.setText(user.About);
        tv_school.setText(user.School);
        tv_language.setText(user.Spoken);
        tv_hobby.setText(user.Hobbies);
        iv_call.setEnabled(IsEnable);

        padding = ll_album.getPaddingLeft();
        int width = getResources().getDisplayMetrics().widthPixels;
        size = (width - padding * 5) / 4;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        ImageView imageView = new ImageView(getApplicationContext());
        layoutParams.setMargins(0, 0, 0, 0);//显示参数,添加marginLeft
        ll_album.addView(imageView, layoutParams);//添加图片
        ll_album.setOnClickListener(ActivityProfile.this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.ActivityProfile_loading));
        progressDialog.show();

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("username", user.Username);
        HttpUtil.post(NetworkUtil.nimuserGetPhotosByUsername, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> o = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (o.code == 200) {
                    User info = o.info;
                    tv_location.setText(info.Spoken);
                    tv_school.setText(info.School);
                    tv_language.setText(info.Spoken);
                    tv_hobby.setText(info.Hobbies);

                    if (info.Photos.size() > 0) {
                        ll_album.removeAllViews();
                    }

                    photos1 = info.Photos;
                    for (String s : photos1) {
                        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(size, size);
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setBackgroundResource(R.color.color_app_normal);
                        layout.setMargins(0, 0, padding, 0);//显示参数,添加marginLeft
                        CommonUtil.showBitmap(imageView, NetworkUtil.getFullPath(s));//加载图片
                        ll_album.addView(imageView, layout);//添加图片
                    }

                    int childCount = ll_album.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View childAt = ll_album.getChildAt(i);

                        final int index = i;
                        childAt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityAlbum.start(ActivityProfile.this, photos1.toArray(new String[photos1.size()]), index);
                            }
                        });

                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                progressDialog.dismiss();
                CommonUtil.toast(R.string.network_error);
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
                            ActivityCall.start(ActivityProfile.this, user.Id, user.Accid, user.Avatar, user.Nickname,user.Username, ActivityCall.CALL_TYPE_AUDIO);
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
}
