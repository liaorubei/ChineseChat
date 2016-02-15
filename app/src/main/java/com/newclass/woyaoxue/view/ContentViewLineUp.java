package com.newclass.woyaoxue.view;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.activity.SignInActivity;
import com.newclass.woyaoxue.adapter.AdapterLineup;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Rank;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaorubei on 2016/1/14.
 */
public class ContentViewLineUp extends ContentView {

    private static final String TAG = "ContentViewLineUp";
    private View inflate;
    private SwipeRefreshLayout srl;
    private ListView listview;
    private BaseAdapter<User> adapter;
    private List<User> list;

    public ContentViewLineUp(Context context) {
        super(context);
        showView(ViewState.SUCCESS);
        initData();
    }

    @Override
    public void initData() {
        refresh(true);
    }

    @Override
    public View onCreateSuccessView() {
        inflate = View.inflate(getContext(), R.layout.contentview_folder, null);
        srl = (SwipeRefreshLayout) inflate.findViewById(R.id.srl);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        listview = (ListView) inflate.findViewById(R.id.listview);
        list = new ArrayList<User>();
        adapter = new AdapterLineup(list, getContext());
        listview.setAdapter(adapter);

        return inflate;
    }

    public static void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(menu.NONE, 1, 1, "入队").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(menu.NONE, 2, 3, "刷新").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                lineup();
                break;
            case 2:
                refresh(true);
                break;
        }
        return true;
    }

    private void refresh(boolean isRefresh) {

        StatusCode status = NIMClient.getStatus();
        Log.i(TAG, "refresh: " + status);
        if (status != StatusCode.LOGINED) {
            getContext().startActivity(new Intent(getContext(), SignInActivity.class));
            return;
        }

        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("id", getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getInt("id", 0) + "");
        HttpUtil.post(NetworkUtil.teacherRefresh, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                refreshView(responseInfo.result);

                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
            }
        });

    }

    private void lineup() {
        StatusCode status = NIMClient.getStatus();
        if (status != StatusCode.LOGINED) {
            getContext().startActivity(new Intent(getContext(), SignInActivity.class));
            return;
        }

        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("id", getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getInt("id", 0) + "");
        HttpUtil.post(NetworkUtil.teacherEnqueue, parameters, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                refreshView(responseInfo.result);
                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
                CommonUtil.toast("排队失败");
            }
        });
    }

    private Gson gson = new Gson();

    private void refreshView(String json) {
        Response<Rank> resp = gson.fromJson(json, new TypeToken<Response<Rank>>() {
        }.getType());

        if (resp.code == 200 && resp.info != null) {
            List<User> users = resp.info.Data;
            list.clear();
            for (User user : users) {
                list.add(user);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
