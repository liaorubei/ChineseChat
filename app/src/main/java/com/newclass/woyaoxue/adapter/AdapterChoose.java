package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.CallActivity;
import com.newclass.woyaoxue.activity.SignInActivity;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * Created by liaorubei on 2016/1/13.
 */
public class AdapterChoose extends BaseAdapter<User> {
    private Context mContext;

    public AdapterChoose(List<User> list, Context context) {
        super(list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final User user = getItem(position);
        View inflate = View.inflate(mContext, R.layout.listitem_choose, null);
        TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        TextView tv_username = (TextView) inflate.findViewById(R.id.tv_username);
        TextView tv_category = (TextView) inflate.findViewById(R.id.tv_category);
        tv_nickname.setText(user.Name);
        tv_username.setText(user.Username);
        tv_category.setText(user.Category == 1 ? "教师" : "学生");

        Button bt_call = (Button) inflate.findViewById(R.id.bt_call);
        bt_call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    mContext.startActivity(new Intent(mContext, SignInActivity.class));
                    return;
                }

                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("id", 1 + "");
                parameters.add("target", user.Id + "");
                HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        CallActivity.start(mContext, user.Id, user.Accid, user.NickName, CallActivity.CALL_TYPE_AUDIO);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });

            }
        });

        return inflate;
    }
}
