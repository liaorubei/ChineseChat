package com.hanwen.chinesechat.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.activity.ActivityCall;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.TeacherQueue;
import com.hanwen.chinesechat.service.ServiceQueue;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.activity.ActivityProfile;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.XListView;
import com.hanwen.chinesechat.view.XListViewFooter;
import com.hanwen.chinesechat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师端教师排队界面
 * Created by liaorubei on 2016/1/14.
 */
public class FragmentLineUp extends Fragment implements View.OnClickListener, XListView.IXListViewListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private static final String TAG = "FragmentLineUp";
    private static final int REFRESH_DATA = 1;
    private SwipeRefreshLayout srl;
    private static Gson gson = new Gson();
    private List<User> dataSet;
    private BaseAdapter<User> adapter;
    private int offset = 10;
    private int time = 0;
    private XListView listview;
    private int take = 50;
    private TextView tv_nickname, tv_line;
    private boolean resume = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    time += offset;
                    if (time >= 60) {
                        refresh(true);
                        time = 0;
                    }
                    Log.i(TAG, "handleMessage: time=" + time);
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);//10秒回调一次，一分钟刷新一次
                    break;
            }
        }
    };
    private Intent service;

    private void refresh(final boolean refresh) {
        if (resume) {
            srl.setRefreshing(true);
            resume = false;
        }

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", ChineseChat.CurrentUser.Id);
        params.add("skip", refresh ? 0 : dataSet.size());
        params.add("take", take);
        HttpUtil.post(NetworkUtil.getTeacherOnline, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);

                Response<TeacherQueue> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<TeacherQueue>>() {
                }.getType());
                if (refresh) {
                    dataSet.clear();
                }

                listview.setPullupEnable(true);
                if (resp.code == 200) {
                    List<User> users = resp.info.Teacher;
                    for (User user : users) {
                        dataSet.add(user);
                    }
                    listview.stopLoadMore(users.size() < take ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);

                    if (resp.info.Current != null) {
                        boolean b = resp.info.Current.IsOnline == 1 && resp.info.Current.IsEnable == 1;
                        tv_line.setText(b ? R.string.FragmentLineUp_i_need_dequeue : R.string.FragmentLineUp_i_need_enqueue);
                        tv_line.setSelected(b);
                    }
                } else {
                    listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
                }


                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);


            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
                listview.setPullupEnable(true);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = new Intent(getActivity(), ServiceQueue.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lineup, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        if (!TextUtils.isEmpty(ChineseChat.CurrentUser.Avatar)) {
            CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(ChineseChat.CurrentUser.Avatar));
        }
        tv_line = (TextView) view.findViewById(R.id.tv_line);
        tv_line.setOnClickListener(this);
        tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
        tv_nickname.setText(ChineseChat.CurrentUser.Nickname);

        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(this);

        dataSet = new ArrayList<User>();
        adapter = new MyAdapter(dataSet);
        listview = (XListView) view.findViewById(R.id.listview);
        listview.setAdapter(adapter);
        listview.setPullupEnable(false);
        listview.setPullDownEnable(false);
        listview.setXListViewListener(this);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        resume = true;
        time = 60;
        handler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
        time = 0;
        handler.removeCallbacksAndMessages(null);
    }

    @Override//srl刷新事件,xListView刷新事件
    public void onRefresh() {
        refresh(true);
    }

    @Override//加载更多
    public void onLoadMore() {
        refresh(false);
    }

    @Override//view点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_line: {
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    getActivity().startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }

                if (tv_line.isSelected()) {
                    getActivity().stopService(service);

                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", ChineseChat.CurrentUser.Id);
                    HttpUtil.post(NetworkUtil.teacherDequeue, parameters, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            tv_line.setSelected(false);
                            tv_line.setText(R.string.FragmentLineUp_i_need_enqueue);
                            CommonUtil.toast(R.string.FragmentLineUp_dequeue_success);
                            refresh(true);
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast(R.string.FragmentLineUp_dequeue_failure);
                        }
                    });
                } else {
                    getActivity().startService(service);

                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", ChineseChat.CurrentUser.Id);
                    HttpUtil.post(NetworkUtil.teacherEnqueue, parameters, new RequestCallBack<String>() {

                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Response resp = gson.fromJson(responseInfo.result, new TypeToken<Response>() {
                            }.getType());
                            if (resp.code == 200) {
                                refresh(true);
                                CommonUtil.toast(R.string.FragmentLineUp_enqueue_success);
                                tv_line.setSelected(true);
                                tv_line.setText(R.string.FragmentLineUp_i_need_dequeue);
                            }
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast(R.string.FragmentLineUp_enqueue_failure);
                        }
                    });
                }
            }
            break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - 1;
        if (index < dataSet.size()) {
            ActivityProfile.start(getActivity(), dataSet.get(index), false);
        }
    }

    private class MyAdapter extends BaseAdapter<User> {
        public MyAdapter(List<User> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_choose, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            //三种状态，在线，忙线，掉线 {绿，红，灰}

            holder.tv_nickname.setText(String.format("%1$s%2$s", TextUtils.isEmpty(user.Nickname) ? "" : user.Nickname, TextUtils.equals(user.Username, ChineseChat.CurrentUser.Username) ? "[本人]" : ""));
            holder.tv_nickname.setTextColor(user.IsEnable ? getResources().getColor(R.color.color_app) : Color.RED);
            holder.tv_spoken.setText(user.Spoken);

            if (user.IsOnline) {
                if (user.IsEnable) {
                    holder.tv_nickname.setTextColor(getResources().getColor(R.color.color_app));
                    holder.tv_status.setText(R.string.FragmentChoose_tips_online);
                    holder.tv_status.setTextColor(getResources().getColor(R.color.color_app));
                    holder.iv_status.setBackgroundResource(R.color.color_app);
                    holder.iv_status.setImageResource(R.drawable.teacher_online);
                    holder.tv_nickname.setTextColor(getResources().getColor(R.color.color_app));
                } else {
                    holder.tv_nickname.setTextColor(getResources().getColor(R.color.teacher_busy));
                    holder.tv_status.setText(R.string.FragmentChoose_tips_busy);
                    holder.tv_status.setTextColor(getResources().getColor(R.color.teacher_busy));
                    holder.iv_status.setBackgroundResource(R.color.teacher_busy);
                    holder.iv_status.setImageResource(R.drawable.teacher_busy);
                    holder.tv_nickname.setTextColor(getResources().getColor(R.color.teacher_busy));
                }
            } else {
                holder.tv_nickname.setTextColor(getResources().getColor(R.color.color_app_normal));
                holder.tv_status.setText(R.string.FragmentChoose_tips_offline);
                holder.tv_status.setTextColor(getResources().getColor(R.color.color_app_normal));
                holder.iv_status.setBackgroundResource(R.color.color_app_normal);
                holder.iv_status.setImageResource(R.drawable.teacher_offline);
                holder.tv_nickname.setTextColor(getResources().getColor(R.color.color_app_normal));
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
                        startActivity(new Intent(getActivity(), ActivitySignIn.class));
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
                                ActivityCall.start(getActivity(), user.Id, user.Accid, user.Avatar, user.Username, user.Username, ActivityCall.CALL_TYPE_AUDIO);
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