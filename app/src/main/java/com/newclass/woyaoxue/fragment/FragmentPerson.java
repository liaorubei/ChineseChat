package com.newclass.woyaoxue.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.activity.ActivityHistory;
import com.newclass.woyaoxue.activity.ActivityPayment;
import com.newclass.woyaoxue.activity.ActivityPerson;
import com.newclass.woyaoxue.activity.ActivitySetting;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private ImageView iv_avatar;
    private TextView tv_nickname;
    private ImageView iv_gender;
    private TextView tv_coins;
    private RelativeLayout rl_payment;
    private RelativeLayout rl_setting;
    private RelativeLayout rl_history;
    private String username;
    private String accid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_person, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        //头像，昵称,性别，学币
        iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
        tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
        iv_gender = (ImageView) view.findViewById(R.id.iv_gender);
        tv_coins = (TextView) view.findViewById(R.id.tv_coins);

        //充值，设置，学习记录菜单
        rl_payment = (RelativeLayout) view.findViewById(R.id.rl_payment);
        rl_setting = (RelativeLayout) view.findViewById(R.id.rl_setting);
        rl_history = (RelativeLayout) view.findViewById(R.id.rl_history);

        //点击事件
        view.findViewById(R.id.ll_person).setOnClickListener(this);
        rl_payment.setOnClickListener(this);
        rl_setting.setOnClickListener(this);
        rl_history.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        if (NIMClient.getStatus() == StatusCode.LOGINED) {
            //显示头像，昵称，性别，学币
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            username = sharedPreferences.getString("username", "");
            accid = sharedPreferences.getString("accid", "");
            String avatar = sharedPreferences.getString("avatar", "");
            String nickname = sharedPreferences.getString("nickname", "");
            int gender = sharedPreferences.getInt("gender", -1);
            int coins = sharedPreferences.getInt("coins", 0);

            Log.i(TAG, "onResume: Avatar=" + avatar + " nickname=" + nickname + " gender=" + gender + " coins=" + coins);

            CommonUtil.showIcon(getActivity(), iv_avatar, avatar);
            tv_nickname.setText(nickname);
            iv_gender.setImageResource(gender == 0 ? R.drawable.gender_female : R.drawable.gender_male);
            iv_gender.setVisibility(gender > -1 ? View.VISIBLE : View.INVISIBLE);
            tv_coins.setText("" + coins);
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
            case R.id.rl_payment:
                startActivity(new Intent(getActivity(), ActivityPayment.class));
                break;

            case R.id.rl_history:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                ActivityHistory.start(getActivity(), username);
                break;
            case R.id.rl_setting:
                startActivity(new Intent(getActivity(), ActivitySetting.class));
                break;
        }
    }
}
