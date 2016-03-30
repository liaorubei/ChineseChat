package com.newclass.woyaoxue.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.MyApplication;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.activity.ActivityHistory;
import com.newclass.woyaoxue.activity.ActivityMoney;
import com.newclass.woyaoxue.activity.ActivityPerson;
import com.newclass.woyaoxue.activity.ActivitySetting;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private static final int REQUESTCODE_TOPUP = 1;
    private View bt_histroy, bt_setting, rl_topup;
    private ImageView iv_avater, iv_gender;
    private TextView tv_nickname, tv_coins, tv_score;

    private View ll_person;
    private User user;
    private boolean NEED_TO_REFRESH = true;//是否需要刷新成最新数据

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");

        if (NEED_TO_REFRESH && NIMClient.getStatus() == StatusCode.LOGINED) {
            NEED_TO_REFRESH = false;

            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("username", getActivity().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", ""));
            HttpUtil.post(NetworkUtil.nimuserGetByUsername, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Response<User> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                    }.getType());

                    if (resp.code == 200) {
                        tv_coins.setText(resp.info.Coins + "");
                        tv_score.setText(resp.info.Score + "");
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: " + msg);
                }
            });
        }
        super.onResume();
        initData();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View inflate = inflater.inflate(R.layout.fragment_person, null);
        ll_person = inflate.findViewById(R.id.ll_person);
        tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        iv_avater = (ImageView) inflate.findViewById(R.id.iv_avater);
        iv_gender = (ImageView) inflate.findViewById(R.id.iv_gender);
        tv_coins = (TextView) inflate.findViewById(R.id.tv_coins);
        tv_score = (TextView) inflate.findViewById(R.id.tv_score);

        rl_topup = inflate.findViewById(R.id.rl_topup);
        bt_histroy = inflate.findViewById(R.id.rl_history);
        bt_setting = inflate.findViewById(R.id.rl_setting);

        ll_person.setOnClickListener(this);
        rl_topup.setOnClickListener(this);
        bt_histroy.setOnClickListener(this);
        bt_setting.setOnClickListener(this);
        return inflate;
    }

    private void initData() {
        //如果是学生端,显示充值界面和学习记录
        rl_topup.setVisibility(MyApplication.isStudent() ? View.VISIBLE : View.GONE);
        bt_histroy.setVisibility(MyApplication.isStudent() ? View.VISIBLE : View.GONE);

        if (NIMClient.getStatus() == StatusCode.LOGINED) {
            SharedPreferences sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            user = new User();
            String nickname = sp.getString("nickname", "");
            String username = sp.getString("username", "");
            String avater = sp.getString("avater", "");
            int gender = sp.getInt("gender", -1);

            user.NickName = nickname;
            user.UserName = username;
            user.Gender = gender;
            user.Accid = sp.getString("accid", "");

            tv_nickname.setText(nickname);
            iv_gender.setVisibility(gender == -1 ? View.INVISIBLE : View.VISIBLE);
            iv_gender.setImageResource(gender == 0 ? R.drawable.gender_female : R.drawable.gender_male);

            BitmapUtils bitmapUtils = new BitmapUtils(getActivity());
            final File avaterPNG = new File(FolderUtil.rootDir(getActivity()), avater);

            if (avaterPNG.exists() && avaterPNG.isFile()) {
                //加载本地图片
                bitmapUtils.display(iv_avater, avaterPNG.getAbsolutePath());
            } else {
                if (!avaterPNG.getParentFile().exists()) {
                    avaterPNG.getParentFile().mkdirs();
                }

                // 加载网络图片
                bitmapUtils.display(iv_avater, NetworkUtil.getFullPath(avater), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                        try {
                            OutputStream stream = new FileOutputStream(avaterPNG);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            stream.close();
                        } catch (Exception e) {
                            Log.i(TAG, "onLoadCompleted: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                        iv_avater.setImageResource(R.drawable.ic_launcher_student);
                    }
                });
            }

        } else {
            tv_nickname.setText("未登录");
            iv_gender.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_TOPUP:
                NEED_TO_REFRESH = resultCode == ActivityMoney.RESULTCODE_SUCCESS;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_person:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                startActivity(new Intent(getActivity(), ActivityPerson.class));
                break;
            case R.id.rl_topup:
                startActivityForResult(new Intent(getActivity(), ActivityMoney.class), REQUESTCODE_TOPUP);
                break;

            case R.id.rl_history:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                ActivityHistory.start(getActivity(), user.Accid);
                break;
            case R.id.rl_setting:
                startActivity(new Intent(getActivity(), ActivitySetting.class));
                break;
        }
    }
}
