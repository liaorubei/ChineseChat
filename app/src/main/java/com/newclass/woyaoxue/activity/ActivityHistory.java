package com.newclass.woyaoxue.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.CallLog;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

//聊天记录
public class ActivityHistory extends Activity {
    protected static final String TAG = "HistoryActivity";
    private static final String KEY_USERNAME = "KEY_USERNAME";
    private ListView listview;
    private List<CallLog> list;
    private BaseAdapter<CallLog> adapter;
    private String username;
    private SimpleDateFormat sdf;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
        initData();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;

            default:
                break;
        }

        return true;
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listview = (ListView) findViewById(R.id.listview);
        list = new ArrayList<CallLog>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
    }

    public static void start(Context context, String accid) {
        Intent intent = new Intent(context, ActivityHistory.class);
        intent.putExtra(KEY_USERNAME, accid);
        context.startActivity(intent);
    }

    private void initData() {
        Intent intent = getIntent();
        username = intent.getStringExtra(KEY_USERNAME);

        Parameters parameters = new Parameters();
        parameters.add("username", username);
        parameters.add("skip", 0 + "");
        parameters.add("take", 50 + "");

        HttpUtil.post(NetworkUtil.GetStudentCallLogByUsername, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<CallLog>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<CallLog>>>() {
                }.getType());
                if (resp.code == 200) {
                    list.clear();
                    List<CallLog> logs = resp.info;
                    for (CallLog callLog : logs) {
                        list.add(callLog);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "获取记录失败:" + msg);
            }
        });
    }

    private class MyAdapter extends BaseAdapter<CallLog> {

        public MyAdapter(List<CallLog> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CallLog item = getItem(position);
            View inflate = View.inflate(getApplication(), R.layout.listitem_history, null);
            TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
            TextView tv_teacher = (TextView) inflate.findViewById(R.id.tv_teacher);
            TextView tv_coins = (TextView) inflate.findViewById(R.id.tv_coins);
            TextView tv_date = (TextView) inflate.findViewById(R.id.tv_date);
            TextView tv_time = (TextView) inflate.findViewById(R.id.tv_time);

            tv_theme.setText(getString(R.string.ActivityHistory_theme) + "未选择");
            if (item.Themes != null && item.Themes.size() > 0) {
                tv_theme.setText(getString(R.string.ActivityHistory_theme) + item.Themes.get(0).Name);
            }
            tv_teacher.setText(getString(R.string.ActivityHistory_teacher) + item.Teacher.Nickname);
            tv_coins.setText(getString(R.string.ActivityHistory_coins) + item.Coins);
            tv_date.setText(getString(R.string.ActivityHistory_date) + sdf.format(item.Start));
            tv_time.setText(getString(R.string.ActivityHistory_time) + CommonUtil.millisecondsFormat(item.Finish.getTime() - item.Start.getTime()));
            return inflate;
        }
    }
}
