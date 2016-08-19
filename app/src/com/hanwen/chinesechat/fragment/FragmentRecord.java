package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.XListView;
import com.hanwen.chinesechat.view.XListViewFooter;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 教师月份授课记录,要求有开始时间和结束时间
 */
public class FragmentRecord extends Fragment implements SwipeRefreshLayout.OnRefreshListener, XListView.IXListViewListener {

    private static final String ARG_CALENDAR = "ARG_CALENDAR";
    private static final String TAG = "FragmentRecord";
    private List<CallLog> list;
    private MyAdapter adapter;
    private int take = 50;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private XListView listview;
    private SwipeRefreshLayout srl;
    private Calendar calendarFrom;
    private SimpleDateFormat sdfE = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy");
    private SimpleDateFormat sdfM = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar calendarTo;

    public static FragmentRecord newInstance(Calendar calendar) {
        FragmentRecord fragment = new FragmentRecord();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CALENDAR, calendar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            calendarFrom = (Calendar) getArguments().getSerializable(ARG_CALENDAR);
            calendarTo = Calendar.getInstance();
            calendarTo.set(calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), 1, 0, 0, 0);
            calendarTo.add(Calendar.MONTH, 1);
        }
        Log.i(TAG, "onCreate: " + sdfE.format(calendarFrom.getTime()));
        Log.i(TAG, "onCreate: " + sdfE.format(calendarTo.getTime()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(this);

        listview = (XListView) view.findViewById(R.id.listView);
        listview.setPullupEnable(false);
        listview.setPullDownEnable(false);
        listview.setXListViewListener(this);

        list = new ArrayList<>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        refresh(true);
    }

    private void refresh(final boolean refresh) {
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("Id", ChineseChat.CurrentUser.Id);
        parameters.add("skip", refresh ? 0 : list.size());
        parameters.add("take", take);
        parameters.add("from", sdfM.format(calendarFrom.getTime()));
        parameters.add("to", sdfM.format(calendarTo.getTime()));

        HttpUtil.post(NetworkUtil.CallLogGetByUserId, parameters, new RequestCallBack<String>() {
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

    @Override
    public void onRefresh() {
        refresh(true);
    }

    @Override
    public void onLoadMore() {
        refresh(false);
    }

    private class MyAdapter extends BaseAdapter<CallLog> {
        public MyAdapter(List<CallLog> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CallLog item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_history_teacher, null);
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
            //holder.tv_coins.setText(getString(R.string.ActivityHistory_coins, item.Coins));
            //holder.tv_coins.setVisibility(View.GONE);
            holder.tv_date.setText(sdfE.format(item.Start));
            holder.tv_time.setText(getString(item.Duration > 1 ? R.string.ActivityHistory_durations : R.string.ActivityHistory_duration, item.Duration));
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView tv_theme;
        public TextView tv_other;
        //public TextView tv_coins;
        public TextView tv_date;
        public TextView tv_time;

        public ViewHolder(View convertView) {
            convertView.setTag(this);
            this.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
            this.tv_other = (TextView) convertView.findViewById(R.id.tv_other);
            //this.tv_coins = (TextView) convertView.findViewById(R.id.tv_coins);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        }
    }
}
