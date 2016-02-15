package com.newclass.woyaoxue.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.HistoryActivity;
import com.newclass.woyaoxue.activity.MoneyActivity;
import com.newclass.woyaoxue.activity.PersonActivity;
import com.newclass.woyaoxue.activity.SettingActivity;
import com.newclass.woyaoxue.activity.SignInActivity;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private Button bt_histroy, bt_setting;
    private ImageView iv_avater, iv_gender;
    private TextView tv_nickname, tv_username;
    private RelativeLayout rl_money;
    private LinearLayout ll_person;
    private User user;

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_person, null);
        ll_person = (LinearLayout) inflate.findViewById(R.id.ll_person);
        tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        tv_username = (TextView) inflate.findViewById(R.id.tv_username);
        iv_avater = (ImageView) inflate.findViewById(R.id.iv_avater);
        iv_gender = (ImageView) inflate.findViewById(R.id.iv_gender);

        rl_money = (RelativeLayout) inflate.findViewById(R.id.rl_money);
        bt_histroy = (Button) inflate.findViewById(R.id.bt_history);
        bt_setting = (Button) inflate.findViewById(R.id.bt_setting);

        ll_person.setOnClickListener(this);
        rl_money.setOnClickListener(this);
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
            tv_username.setText(username);

            iv_gender.setVisibility(gender == -1 ? View.INVISIBLE : View.VISIBLE);
            iv_gender.setImageResource(gender == 0 ? R.drawable.gender_female : R.drawable.gender_male);

            BitmapUtils bitmapUtils = new BitmapUtils(getActivity());

            // 加载网络图片
            bitmapUtils.display(iv_avater, NetworkUtil.getFullPath(avater));
        } else {
            tv_nickname.setText("未登录");
            tv_username.setText("未登录");
            iv_gender.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_person:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                    return;
                }
                startActivity(new Intent(getActivity(), PersonActivity.class));
                break;
            case R.id.rl_money:
                getActivity().startActivity(new Intent(getActivity(), MoneyActivity.class));
                break;
            case R.id.bt_history:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                    return;
                }
                HistoryActivity.start(getActivity(), user.Accid);
                break;
            case R.id.bt_setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }
}
