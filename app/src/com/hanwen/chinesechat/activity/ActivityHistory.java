package com.hanwen.chinesechat.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.fragment.FragmentHistory;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.XListView;
import com.hanwen.chinesechat.view.XListViewFooter;
import com.hanwen.chinesechat.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

//聊天记录,授课记录
public class ActivityHistory extends FragmentActivity {
    protected static final String TAG = "HistoryActivity";
    private XListView listview;
    private List<CallLog> list;
    private BaseAdapter<CallLog> adapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy");
    private SimpleDateFormat sdfm = new SimpleDateFormat("MM");
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private SwipeRefreshLayout srl;
    private int take = 50;
    private LinearLayout ll_month;
    private List<Calendar> listCalendar;
    private ViewPager viewpager;
    private View ll_teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listCalendar = new ArrayList<>();

        for (Calendar c : listCalendar) {
            Log.i(TAG, "onCreate: " + sdfm.format(c.getTime()));
        }

        initView();
        if (ChineseChat.isStudent()) {
            srl.setVisibility(View.VISIBLE);
            ll_teacher.setVisibility(View.INVISIBLE);
            refresh(true);
        } else {
            srl.setVisibility(View.INVISIBLE);
            ll_teacher.setVisibility(View.VISIBLE);

            for (int i = 0; i < 6; i++) {
                Calendar instance = Calendar.getInstance();
                instance.set(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH), 1, 0, 0, 0);
                instance.add(Calendar.MONTH, -(5 - i));
                listCalendar.add(instance);
            }
            viewpager.getAdapter().notifyDataSetChanged();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
            params.gravity = Gravity.CENTER_VERTICAL;
            for (int i = 0; i < listCalendar.size(); i++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setGravity(Gravity.CENTER);
                textView.setText(sdfm.format(listCalendar.get(i).getTime()) + "月份");
                textView.setTextColor(getResources().getColorStateList(R.color.selector_text_normal));
                textView.setBackgroundResource(R.drawable.selector_hslevels);
                ll_month.addView(textView, params);

                final int item = i;
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewpager.setCurrentItem(item);
                    }
                });
            }

            viewpager.setCurrentItem(5);
        }
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

        ll_teacher = findViewById(R.id.ll_teacher);

        ll_month = (LinearLayout) findViewById(R.id.ll_month);


        viewpager = (ViewPager) findViewById(R.id.viewpager);
        viewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return FragmentHistory.newInstance(listCalendar.get(position));
            }

            @Override
            public int getCount() {
                return listCalendar.size();
            }
        });
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int childCount = ll_month.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = ll_month.getChildAt(i);
                    childAt.setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


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
                convertView = View.inflate(getApplication(), R.layout.listitem_history_student, null);
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
            holder.tv_coins.setText(getString(item.Coins > 1 ? R.string.ActivityHistory_coins : R.string.ActivityHistory_coin, item.Coins));
            holder.tv_date.setText(sdf.format(item.Start));
            holder.tv_time.setText(getString(item.Duration > 1 ? R.string.ActivityHistory_durations : R.string.ActivityHistory_duration, item.Duration));
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
