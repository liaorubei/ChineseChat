package com.newclass.woyaoxue.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import com.newclass.woyaoxue.util.Log;

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
import com.newclass.woyaoxue.adapter.AdapterChoose;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaorubei on 2016/1/13.
 */
public class ContentViewChoose extends ContentView {

    private static final String TAG = "ContentViewChoose";
    private SwipeRefreshLayout srl;
    private ListView listview;
    private List<User> list;
    private AdapterChoose adapterChoose;
    private String take = "15";
    private Gson gson = new Gson();

    public ContentViewChoose(Context context) {
        super(context);
        showView(ViewState.SUCCESS);

    }

    @Override
    public void initData() {
        loadData(true);
    }

    @Override
    public View onCreateSuccessView() {
        View inflate = View.inflate(getContext(), R.layout.contentview_choose, null);
        srl = (SwipeRefreshLayout) inflate.findViewById(R.id.srl);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });

        listview = (ListView) inflate.findViewById(R.id.listview);
        list = new ArrayList<>();
        adapterChoose = new AdapterChoose(list, getContext());
        listview.setAdapter(adapterChoose);

        return inflate;
    }

    public static void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 1, 1, "刷新").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                loadData(true);

                break;
        }
        return true;
    }

    private void loadData(final boolean isRefresh) {

        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("skip", (isRefresh ? 0 : list.size()) + "");
        parameters.add("take", take);
        HttpUtil.post(NetworkUtil.teacherInQueue, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (isRefresh) {
                    list.clear();
                }

                Response<List<User>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<User>>>() {
                }.getType());

                if (resp.code == 200 && resp.info.size() > 0) {
                    for (User user : resp.info) {
                        user.NickName = user.Name;
                        list.add(user);
                    }
                    adapterChoose.notifyDataSetChanged();
                }

                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
                CommonUtil.toast("加载失败");
            }
        });


    }


    public void onResume() {
        initData();
    }
}
