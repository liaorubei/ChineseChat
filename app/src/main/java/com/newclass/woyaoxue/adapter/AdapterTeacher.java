package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
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
import com.newclass.woyaoxue.activity.ActivityCall;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * Created by 儒北 on 2016-04-27.
 */
public class AdapterTeacher extends BaseAdapter<User> {
    private static final String TAG = "AdapterTeacher";
    private final Context mContext;

    public AdapterTeacher(List<User> list, Context context) {
        super(list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final User user = getItem(position);
        if (convertView == null) {
            convertView = View.inflate(this.mContext, R.layout.listitem_choose, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        //三种状态，在线，忙线，掉线 {绿，红，灰}
        holder.tv_nickname.setText(user.Nickname + (TextUtils.equals(user.Nickname, ChineseChat.CurrentUser.Nickname) ? " [本人]" : ""));
        holder.tv_nickname.setTextColor(user.IsEnable ? this.mContext.getResources().getColor(R.color.color_app) : Color.RED);

        holder.tv_spoken.setText(user.Spoken);

        if (user.IsOnline) {
            if (user.IsEnable && position < 5) {
                holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.color_app));
                holder.tv_status.setText(R.string.FragmentLineUp_in_the_pool);
                holder.tv_status.setTextColor(this.mContext.getResources().getColor(R.color.color_app));
                holder.iv_status.setBackgroundResource(R.color.color_app);
                holder.iv_status.setImageResource(R.drawable.teacher_online);
                holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.color_app));
            } else {
                holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.teacher_busy));
                holder.tv_status.setText(R.string.FragmentLineUp_in_the_line);
                holder.tv_status.setTextColor(this.mContext.getResources().getColor(R.color.teacher_busy));
                holder.iv_status.setBackgroundResource(R.color.teacher_busy);
                holder.iv_status.setImageResource(R.drawable.teacher_busy);
                holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.teacher_busy));
            }
        } else {
            holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.color_app_normal));
            holder.tv_status.setText(R.string.FragmentChoose_tips_offline);
            holder.tv_status.setTextColor(this.mContext.getResources().getColor(R.color.color_app_normal));
            holder.iv_status.setBackgroundResource(R.color.color_app_normal);
            holder.iv_status.setImageResource(R.drawable.teacher_offline);
            holder.tv_nickname.setTextColor(this.mContext.getResources().getColor(R.color.color_app_normal));
        }


        if (!TextUtils.isEmpty(user.Avatar)) {
            CommonUtil.showBitmap(holder.iv_avatar, NetworkUtil.getFullPath(user.Avatar));
        } else {
            holder.iv_avatar.setImageResource(R.drawable.ic_launcher_student);
        }
        holder.iv_status.setImageResource(user.IsEnable ? R.drawable.teacher_online : R.drawable.teacher_busy);

        //设置点击,可见,可用
        holder.bt_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.GONE);
        holder.bt_call.setBackgroundResource(R.drawable.selector_choose_callb);
        holder.bt_call.setEnabled(user.IsEnable && user.IsOnline);
        holder.bt_call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //如果没有登录,那么要求登录
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    mContext.startActivity(new Intent(mContext, ActivitySignIn.class));
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
                        Response<User> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());
                        if (resp.code == 200) {
                            ActivityCall.start(mContext, user.Id, user.Accid, user.Avatar, user.Username, ActivityCall.CALL_TYPE_AUDIO);
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
        });
        return convertView;
    }

    private class ViewHolder {
        public TextView tv_status;
        public TextView tv_nickname;

        public ImageView iv_avatar, iv_status;
        public TextView tv_spoken;
        public ImageView bt_call;

        public ViewHolder(View convertView) {
            this.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            this.iv_status = (ImageView) convertView.findViewById(R.id.iv_status);
            this.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
            this.tv_spoken = (TextView) convertView.findViewById(R.id.tv_spoken);
            this.bt_call = (ImageView) convertView.findViewById(R.id.bt_call);
            this.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
        }
    }
}
