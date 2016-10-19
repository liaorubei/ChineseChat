package com.hanwen.chinesechat.fragment;

import android.graphics.Color;
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
import com.hanwen.chinesechat.bean.Chat;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Summary;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.CalendarMy;
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
import java.util.Locale;

/**
 * 教师月份授课记录,要求有开始时间和结束时间
 */
public class FragmentRecord extends Fragment implements SwipeRefreshLayout.OnRefreshListener, XListView.IXListViewListener {

    private static final String TAG = "FragmentRecord";
    private static final String KEY_FROM = "KEY_FROM";
    private static final String KEY_TO = "KEY_TO";
    private List<Chat> list;
    private MyAdapter adapter;
    private int take = 25;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private XListView listview;
    private SimpleDateFormat sdfE = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy", Locale.getDefault());
    private TextView tv_summary;
    private String to;
    private String from;
    private SwipeRefreshLayout srl;

    public static FragmentRecord newInstance(String from, String to) {
        FragmentRecord fragment = new FragmentRecord();
        Bundle args = new Bundle();
        args.putString(KEY_FROM, from);
        args.putString(KEY_TO, to);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        CalendarMy instance = new CalendarMy();
        to = arguments.getString(KEY_TO, instance.toString("yyyy-MM-dd HH:ss:mm"));
        from = arguments.getString(KEY_FROM, instance.add(Calendar.YEAR, 2015).toString("yyyy-MM-dd HH:ss:mm"));
        Log.i(TAG, "onCreate: from:" + from + " to:" + to);

        list = new ArrayList<>();
        adapter = new MyAdapter(list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tv_summary = (TextView) view.findViewById(R.id.tv_summary);
        tv_summary.setVisibility(ChineseChat.isStudent() ? View.GONE : View.VISIBLE);//学生端不用显示这个统计数据信息,但是教师端要求显示

        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeColors(Color.parseColor("#00a478"));
        srl.setOnRefreshListener(this);

        listview = (XListView) view.findViewById(R.id.listView);
        listview.setAdapter(adapter);
        listview.setPullupEnable(false);
        listview.setPullDownEnable(false);
        listview.setXListViewListener(this);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        refresh(true);
    }

    private void refresh(final boolean refresh) {
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("userId", ChineseChat.CurrentUser.Id);
        parameters.add("skip", refresh ? 0 : list.size());
        parameters.add("take", take);
        parameters.add("from", from);
        parameters.add("to", to);

        HttpUtil.post(NetworkUtil.CallLogGetListByUserId, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<Summary> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Summary>>() {}.getType());

                listview.setPullupEnable(true);//要把可用设在前面,不然状态会不正常
                if (resp.code == 200) {
                    if (refresh) {
                        list.clear();
                    }
                    tv_summary.setText(String.format("当月授课情况:%1$d分钟（%2$d次）", resp.info.duration, resp.info.count));
                    List<Chat> logs = resp.info.list;
                    for (Chat callLog : logs) {
                        list.add(callLog);
                    }
                    listview.stopLoadMore(logs.size() < take ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);
                } else {
                    listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
                }
                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                listview.setPullupEnable(false);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
                srl.setRefreshing(false);
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

    private class MyAdapter extends BaseAdapter<Chat> {
        public MyAdapter(List<Chat> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Chat item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_record, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            //region 主题
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
            //endregion
            holder.ll_teacher.setVisibility(ChineseChat.isStudent() ? View.GONE : View.VISIBLE);
            holder.tv_student.setText(getString(R.string.ActivityHistory_student, item.Student));
            holder.tv_duration.setText(getString(item.Duration > 1 ? R.string.ActivityHistory_durations : R.string.ActivityHistory_duration, item.Duration));
            holder.tv_teacher.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.GONE);
            holder.tv_teacher.setText(getString(R.string.ActivityHistory_teacher, item.Teacher));
            holder.ll_student.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.GONE);
            holder.tv_cost.setText(getString(item.Coins > 1 ? R.string.ActivityHistory_coins : R.string.ActivityHistory_coin, item.Coins));
            holder.tv_time.setText(getString(item.Duration > 1 ? R.string.ActivityHistory_durations : R.string.ActivityHistory_duration, item.Duration));

            holder.tv_date.setText(sdfE.format(item.Start));

            return convertView;
        }
    }

    private class ViewHolder {
        public TextView tv_theme;

        public View ll_teacher;
        public TextView tv_student;
        public TextView tv_duration;

        public TextView tv_teacher;

        public View ll_student;
        public TextView tv_cost;
        public TextView tv_time;

        public TextView tv_date;

        public ViewHolder(View convertView) {
            convertView.setTag(this);
            this.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
            this.ll_teacher = convertView.findViewById(R.id.ll_teacher);
            this.tv_student = (TextView) convertView.findViewById(R.id.tv_student);
            this.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);
            this.tv_teacher = (TextView) convertView.findViewById(R.id.tv_teacher);
            this.ll_student = convertView.findViewById(R.id.ll_student);
            this.tv_cost = (TextView) convertView.findViewById(R.id.tv_cost);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
        }
    }

    public static class ParamsPage {
        public String from;
        public String to;
        public String text;
    }
}
