package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.R;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends Activity {
    private List<User> list;
    private MyAdapter adapter;
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        initView();
        initData();
    }

    private void initData() {
        // 构建通讯录
        // 如果使用网易云信用户关系、用户资料托管，构建通讯录，先获取我所有好友帐号，再根据帐号去获取对应的用户资料，代码示例如下:
        List<String> accounts = NIMClient.getService(FriendService.class).getFriendAccounts(); // 获取所有好友帐号
        List<NimUserInfo> users = NIMClient.getService(UserService.class).getUserInfoList(accounts); // 获取所有好友用户资料

        for (NimUserInfo nim : users) {
            User user = new User();
            user.Accid = nim.getAccount();
            user.Nickname = nim.getName();
            list.add(user);
        }
        adapter.notifyDataSetInvalidated();
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        list = new ArrayList<User>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);

    }

    private class MyAdapter extends BaseAdapter<User> {

        public MyAdapter(List<User> list) {
            super(list);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = list.get(position);
            View inflate = View.inflate(FriendActivity.this, R.layout.listitem_friend, null);
            TextView tv_username = (TextView) inflate.findViewById(R.id.tv_username);
            TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
            tv_username.setText(user.Username);
            tv_nickname.setText(user.Nickname);
            return inflate;
        }
    }

}
