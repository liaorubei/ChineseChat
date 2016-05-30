package com.newclass.woyaoxue.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.activity.ActivityHistory;
import com.newclass.woyaoxue.activity.ActivityPayment;
import com.newclass.woyaoxue.activity.ActivityPerson;
import com.newclass.woyaoxue.activity.ActivitySetting;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private ImageView iv_avatar;
    private TextView tv_nickname;
    private ImageView iv_gender;
    private TextView tv_coins;
    private View rl_payment, rl_setting, rl_history;
    private View rl_coins;
    private AlertDialog dialogLogin;
    private TextView tv_history;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //头像，昵称,性别，学币
        iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
        tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
        iv_gender = (ImageView) view.findViewById(R.id.iv_gender);
        tv_coins = (TextView) view.findViewById(R.id.tv_coins);
        rl_coins = view.findViewById(R.id.rl_coins);

        //充值，设置，学习记录菜单
        rl_payment = view.findViewById(R.id.rl_payment);
        rl_setting = view.findViewById(R.id.rl_setting);
        rl_history = view.findViewById(R.id.rl_history);
        tv_history = (TextView) view.findViewById(R.id.tv_history);

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
            String avatar = ChineseChat.CurrentUser.Avatar;
            String nickname = ChineseChat.CurrentUser.Nickname;
            int gender = ChineseChat.CurrentUser.Gender;
            int coins = ChineseChat.CurrentUser.Coins;

            Log.i(TAG, "onResume: Avatar=" + avatar + " nickname=" + nickname + " gender=" + gender + " coins=" + coins);

            CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(avatar));
            tv_nickname.setText(nickname);
            iv_gender.setImageResource(gender == 0 ? R.drawable.gender_female : R.drawable.gender_male);
            iv_gender.setVisibility(gender > -1 ? View.VISIBLE : View.INVISIBLE);
            tv_coins.setText(coins + " coins");
        } else {
            iv_avatar.setImageResource(R.drawable.ic_launcher_student);
            tv_nickname.setText(R.string.fragment_person_unlogin);
            iv_gender.setVisibility(View.GONE);
        }

        //如果是教师端，则不显示我的学币，充值和学习记录
        boolean isStudent = ChineseChat.isStudent();
        rl_coins.setVisibility(isStudent ? View.VISIBLE : View.INVISIBLE);
        rl_payment.setVisibility(isStudent ? View.VISIBLE : View.GONE);
        tv_history.setText(isStudent ? R.string.ActivityHistory_title_student : R.string.ActivityHistory_title_teacher);

        //如果没有登录,那么不显示余额
        rl_coins.setVisibility(NIMClient.getStatus() == StatusCode.LOGINED ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_person:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    showLoginDialog();
                    return;
                }
                startActivity(new Intent(getActivity(), ActivityPerson.class));
                break;
            case R.id.rl_payment:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    showLoginDialog();
                    return;
                }
                startActivity(new Intent(getActivity(), ActivityPayment.class));
                break;
            case R.id.rl_setting:
                startActivity(new Intent(getActivity(), ActivitySetting.class));
                break;
            case R.id.rl_history:
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    showLoginDialog();
                    return;
                }
                startActivity(new Intent(getActivity(), ActivityHistory.class));
                break;
        }
    }

    private void showLoginDialog() {
        if (dialogLogin == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.fragment_person_dialog_title);
            builder.setMessage(R.string.fragment_person_dialog_message);
            builder.setPositiveButton(R.string.fragment_person_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.fragment_person_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialogLogin = builder.create();
        }
        dialogLogin.show();
    }
}
