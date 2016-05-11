package com.newclass.woyaoxue.activity;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

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
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.XListView;
import com.newclass.woyaoxue.view.XListViewFooter;
import com.voc.woyaoxue.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//授课记录
public class ActivityLessons extends Activity {

    private static final String TAG = "ActivityLessons";
    private AdapterLessons adapterLessons;
    private List<CallLog> list;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private XListView listview;
    private SwipeRefreshLayout srl;
    private int pageSize = 25;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initView();
    }

    private void initView() {
        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setProgressViewOffset(false, 0, 250);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        listview = (XListView) findViewById(R.id.listview);
        list = new ArrayList<>();
        adapterLessons = new AdapterLessons(list);
        listview.setAdapter(adapterLessons);
        listview.setPullDownEnable(false);
        listview.setPullupEnable(false);
        listview.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {
                loadmore();
            }
        });

        refresh();
    }

    private void refresh() {
        srl.setRefreshing(true);
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("username", ChineseChat.CurrentUser.Username);
        params.add("skip", 0);
        params.add("take", pageSize);
        HttpUtil.post(NetworkUtil.GetTeacherCallLogByUsername, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<CallLog>> o = gson.fromJson(responseInfo.result, new TypeToken<Response<List<CallLog>>>() {
                }.getType());

                listview.setPullupEnable(true);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);

                if (o.code == 200) {
                    list.clear();
                    List<CallLog> info = o.info;
                    for (CallLog log : info) {
                        list.add(log);
                    }
                    listview.stopLoadMore(o.info.size() < pageSize ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);
                }
                srl.setRefreshing(false);
                adapterLessons.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                srl.setRefreshing(false);
                listview.setPullupEnable(true);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
            }
        });
    }

    private void loadmore() {
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("username", ChineseChat.CurrentUser.Username);
        params.add("skip", list.size());
        params.add("take", pageSize);
        HttpUtil.post(NetworkUtil.GetTeacherCallLogByUsername, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<CallLog>> o = gson.fromJson(responseInfo.result, new TypeToken<Response<List<CallLog>>>() {
                }.getType());

                if (o.code == 200) {
                    List<CallLog> info = o.info;
                    for (CallLog log : info) {
                        list.add(log);
                    }
                    if (info.size() < pageSize) {
                        listview.stopLoadMore(XListViewFooter.STATE_NOMORE);
                    } else {
                        listview.stopLoadMore(XListViewFooter.STATE_NORMAL);
                    }
                }
                adapterLessons.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
            }
        });
    }

    private class AdapterLessons extends BaseAdapter<CallLog> {
        public AdapterLessons(List<CallLog> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CallLog item = getItem(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem_history, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_theme.setText(getString(R.string.ActivityHistory_theme) + "未选择");
            if (item.Themes.size() > 0) {
                holder.tv_theme.setText(getString(R.string.ActivityHistory_theme) + item.Themes.get(0).Name);
            }
            holder.tv_teacher.setText(getString(R.string.ActivityHistory_student) + item.Student.Nickname);
            holder.tv_coins.setText(getString(R.string.ActivityHistory_coins) + item.Coins);
            holder.tv_date.setText(getString(R.string.ActivityHistory_date) + sdf.format(item.Start));
            holder.tv_time.setText(getString(R.string.ActivityHistory_duration) + CommonUtil.millisecondsFormat(item.Finish.getTime() - item.Start.getTime()));

            return convertView;
        }
    }

    private class ViewHolder {

        public TextView tv_theme;
        public TextView tv_date;
        public TextView tv_teacher;
        public TextView tv_coins;
        public TextView tv_time;

        public ViewHolder(View convertView) {
            this.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
            this.tv_teacher = (TextView) convertView.findViewById(R.id.tv_teacher);
            this.tv_coins = (TextView) convertView.findViewById(R.id.tv_coins);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
        }
    }

}
