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
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.CallLog;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.XListView;
import com.newclass.woyaoxue.view.XListViewFooter;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//聊天记录
public class ActivityHistory extends Activity {
    protected static final String TAG = "HistoryActivity";
    private XListView listview;
    private List<CallLog> list;
    private BaseAdapter<CallLog> adapter;
    private SimpleDateFormat sdf;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private SwipeRefreshLayout srl;
    private int take = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sdf = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy");
        refresh(true);
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(ChineseChat.isStudent() ? R.string.ActivityHistory_title_student : R.string.ActivityHistory_title_teacher);

        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        list = new ArrayList<CallLog>();
        adapter = new MyAdapter(list);
        listview = (XListView) findViewById(R.id.listview);
        listview.setPullDownEnable(false);
        listview.setPullupEnable(false);
        listview.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }

            @Override
            public void onLoadMore() {
                refresh(false);
            }
        });
        listview.setAdapter(adapter);
    }

    private void refresh(final boolean refresh) {
        Parameters parameters = new Parameters();
        parameters.add("username", ChineseChat.CurrentUser.Username);
        parameters.add("skip", refresh ? 0 : list.size());
        parameters.add("take", take);
        parameters.add("type", ChineseChat.isStudent() ? 0 : 1);

        HttpUtil.post(NetworkUtil.CallLogGetByUsername, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<CallLog>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<CallLog>>>() {
                }.getType());

                listview.setPullupEnable(true);//要把可用设在前面,不然状态会不正常
                if (resp.code == 200) {
                    if (refresh) {
                        list.clear();
                    }
                    List<CallLog> logs = resp.info;
                    for (CallLog callLog : logs) {
                        list.add(callLog);
                    }
                    listview.stopLoadMore(logs.size() < take ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);
                } else {
                    listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
                }
                srl.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                listview.setPullupEnable(true);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
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
            if (convertView == null) {
                convertView = View.inflate(getApplication(), R.layout.listitem_history, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_theme.setText(getString(R.string.ActivityHistory_theme, getString(R.string.ActivityHistory_theme_unselected)));
            if (item.Themes.size() > 0) {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                for (Theme t : item.Themes) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(" > ");
                    }
                    sb.append(t.Name);
                }
                holder.tv_theme.setText(getString(R.string.ActivityHistory_theme, sb.toString()));
            }
            holder.tv_other.setText(ChineseChat.isStudent() ? getString(R.string.ActivityHistory_teacher, item.Teacher.Nickname) : getString(R.string.ActivityHistory_student, item.Student.Nickname));
            holder.tv_coins.setText(getString(R.string.ActivityHistory_coins, item.Coins));
            holder.tv_date.setText(sdf.format(item.Start));
            holder.tv_time.setText(getString(R.string.ActivityHistory_time, item.Duration));
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView tv_theme;
        public TextView tv_other;
        public TextView tv_coins;
        public TextView tv_date;
        public TextView tv_time;

        public ViewHolder(View convertView) {
            convertView.setTag(this);
            this.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
            this.tv_other = (TextView) convertView.findViewById(R.id.tv_other);
            this.tv_coins = (TextView) convertView.findViewById(R.id.tv_coins);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        }
    }
}
