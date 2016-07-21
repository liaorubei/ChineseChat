package com.hanwen.chinesechat.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.activity.ActivityHistory;
import com.hanwen.chinesechat.activity.ActivityPayment;
import com.hanwen.chinesechat.activity.ActivityPerson;
import com.hanwen.chinesechat.activity.ActivitySetting;
import com.hanwen.chinesechat.R;

public class FragmentPerson extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentPerson";
    private ImageView iv_avatar;
    private ImageView iv_gender;

    private AlertDialog dialogLogin;

    private TextView tv_nickname;
    private TextView tv_balance;
    private TextView tv_history;
    private View rl_payment;
    private SwipeRefreshLayout srl;

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
        tv_balance = (TextView) view.findViewById(R.id.tv_balance);

        tv_history = (TextView) view.findViewById(R.id.tv_history);

        view.findViewById(R.id.ll_person).setOnClickListener(this);
        rl_payment = view.findViewById(R.id.rl_payment);
        rl_payment.setOnClickListener(this);
        view.findViewById(R.id.rl_setting).setOnClickListener(this);
        view.findViewById(R.id.rl_history).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //要求,如果性别不确定,那么不显示性别
        //如果是学生端,显示学币数;如果教师端,则显示课时数,如果没有登录,那么学币或者课时都不显示
        //如果是学生端,显示学习记录;如果是教师端,显示授课记录
        //如果是学生端,显示充值,如果是教师端,不显示
        User user = ChineseChat.CurrentUser;
        Log.i(TAG, "onResume: " + user);
        boolean logined = NIMClient.getStatus() == StatusCode.LOGINED;
        boolean student = ChineseChat.isStudent();
        if (TextUtils.isEmpty(user.Avatar)) {
            iv_avatar.setImageResource(R.drawable.ic_launcher_student);
        } else {
            CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(user.Avatar));
        }
        tv_nickname.setText(logined ? user.Nickname : getString(R.string.FragmentPerson_unlogin));
        iv_gender.setVisibility(user.Gender == -1 ? View.GONE : View.VISIBLE);
        iv_gender.setImageResource(user.Gender == 0 ? R.drawable.gender_female : R.drawable.gender_male);
        tv_balance.setVisibility(logined ? View.VISIBLE : View.GONE);
        tv_balance.setText(student ? getString(R.string.FragmentPerson_balance, user.Coins) : getString(R.string.FragmentPerson_hours, user.Summary.duration, user.Summary.count));
        rl_payment.setVisibility(student ? View.VISIBLE : View.GONE);
        tv_history.setText(student ? R.string.ActivityHistory_title_student : R.string.ActivityHistory_title_teacher);
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
