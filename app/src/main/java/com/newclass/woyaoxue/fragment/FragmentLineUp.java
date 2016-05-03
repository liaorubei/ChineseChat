package com.newclass.woyaoxue.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.activity.ActivityProfile;
import com.newclass.woyaoxue.activity.ActivitySignIn;
import com.newclass.woyaoxue.adapter.AdapterTeacher;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.service.TeacherAutoRefreshService;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师端教师排队界面
 * Created by liaorubei on 2016/1/14.
 */
public class FragmentLineUp extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = "FragmentLineUp";
    private static final int REFRESH_DATA = 1;
    private SwipeRefreshLayout srl;
    private static Gson gson = new Gson();
    private List<User> list;
    private AdapterTeacher adapter;
    private int offset = 10;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    time += offset;
                    if (time >= 60) {
                        refresh();
                        time = 0;
                    }
                    Log.i(TAG, "handleMessage: time=" + time);
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);//10秒回调一次，一分钟刷新一次
                    break;
            }
        }
    };
    private int time = 0;

    private void refresh() {
        srl.setRefreshing(true);
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("skip", 0);
        params.add("take", "50");
        HttpUtil.post(NetworkUtil.getTeacher, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);

                Response<List<User>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<User>>>() {
                }.getType());
                list.clear();

                if (resp.code == 200) {
                    List<User> users = resp.info;
                    for (User user : users) {
                        list.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
                CommonUtil.toast(getString(R.string.network_error));
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contentview_lineup, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(this);

        final ListView listview = (ListView) view.findViewById(R.id.listview);
        list = new ArrayList<User>();
        adapter = new AdapterTeacher(list, getActivity());
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityProfile.start(getActivity(), list.get(position));
            }
        });

        view.findViewById(R.id.iv_enqueue).setOnClickListener(this);
        view.findViewById(R.id.iv_dequeue).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        time = 60;
        handler.sendEmptyMessage(REFRESH_DATA);
        Intent service = new Intent(getActivity(), TeacherAutoRefreshService.class);
        getActivity().startService(service);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
        time = 0;
        handler.removeCallbacksAndMessages(null);
    }

    @Override//srl刷新事件
    public void onRefresh() {
        refresh();
    }

    @Override//view点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_enqueue: {
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    getActivity().startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("id", ChineseChat.CurrentUser.Id);
                Log.i(TAG, "onClick: " + ChineseChat.CurrentUser.Id);
                HttpUtil.post(NetworkUtil.teacherEnqueue, parameters, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        Response resp = gson.fromJson(responseInfo.result, new TypeToken<Response>() {
                        }.getType());
                        if (resp.code == 200) {
                            refresh();
                        }
                        CommonUtil.toast(R.string.FragmentLineUp_enqueue_success);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(R.string.FragmentLineUp_enqueue_failure);
                    }
                });
            }
            break;
            case R.id.iv_dequeue: {
                if (NIMClient.getStatus() != StatusCode.LOGINED) {
                    getActivity().startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    return;
                }
                HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                parameters.add("id", ChineseChat.CurrentUser.Id);
                HttpUtil.post(NetworkUtil.teacherDequeue, parameters, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        CommonUtil.toast(R.string.FragmentLineUp_dequeue_success);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(R.string.FragmentLineUp_dequeue_failure);
                    }
                });
            }
            break;
        }
    }
}
