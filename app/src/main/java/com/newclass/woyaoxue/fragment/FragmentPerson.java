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

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.activity.HistoryActivity;
import com.newclass.woyaoxue.activity.ActivityMoney;
import com.newclass.woyaoxue.activity.ActivityPerson;
import com.newclass.woyaoxue.activity.ActivitySetting;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private View bt_histroy, bt_setting;
    private ImageView iv_avater, iv_gender;
    private TextView tv_nickname;

    private View ll_person;
    private User user;

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_person, null);
        ll_person = inflate.findViewById(R.id.ll_person);
        tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
             iv_avater = (ImageView) inflate.findViewById(R.id.iv_avater);
        iv_gender = (ImageView) inflate.findViewById(R.id.iv_gender);


        bt_histroy = inflate.findViewById(R.id.rl_history);
        bt_setting = inflate.findViewById(R.id.rl_setting);

        inflate.findViewById(R.id.rl_topup).setOnClickListener(this);

        ll_person.setOnClickListener(this);
        bt_histroy.setOnClickListener(this);
        bt_setting.setOnClickListener(this);
        return inflate;
    }

    private void initData() {
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
                        iv_avater.setImageResource(R.drawable.ic_launcher);
                    }
                });
            }

        } else {
            tv_nickname.setText("未登录");
            iv_gender.setVisibility(View.INVISIBLE);
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
                startActivity(new Intent(getActivity(), ActivityMoney.class));
                break;

            case R.id.rl_history:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                HistoryActivity.start(getActivity(), user.Accid);
                break;
            case R.id.rl_setting:
                startActivity(new Intent(getActivity(), ActivitySetting.class));
                break;
        }
    }
}
