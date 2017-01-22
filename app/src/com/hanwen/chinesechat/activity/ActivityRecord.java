package com.hanwen.chinesechat.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.CallLog;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.fragment.FragmentRecord;
import com.hanwen.chinesechat.util.CalendarMy;
import com.hanwen.chinesechat.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 本类是聊天记录类，主要显示学生的学生记录或者教师的授课记录，主要是两个功能，一个是只显示一个tab的学生记录（全部记录），
 * 另一个是显示教师的授课记录，但是教师的记录要求显示半年的记录，并按月份统计，并显示月份汇总数据，
 * 为了方便编码，两个功能都合并在这个类中显示，根据数据接口的参数，特地约定学生的参数格式为 from:2015-MM-dd,to:now
 * 约定教师的参数方式为 from:当月开始，to:当月结束
 */
public class ActivityRecord extends FragmentActivity {
    protected static final String TAG = "HistoryActivity";
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy");
    private LinearLayout ll_month;
    private ViewPager viewpager;
    private List<FragmentRecord.ParamsPage> paramsPageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initView();
        initData();
    }

    private void initData() {
        CalendarMy calendar = new CalendarMy();
        if (ChineseChat.isStudent()) {
            FragmentRecord.ParamsPage param = new FragmentRecord.ParamsPage();
            param.to = calendar.add(Calendar.MONTH, 1).toString();
            param.from = calendar.set(Calendar.YEAR, 2015).toString();
            paramsPageList.add(param);
        } else {
            calendar.set(calendar.getYear(), calendar.getMonth(), 1, 0, 0, 1).add(Calendar.MONTH, -6);
            for (int i = 0; i < 6; i++) {
                FragmentRecord.ParamsPage object = new FragmentRecord.ParamsPage();
                object.from = calendar.toString();
                object.text = calendar.toString("MM月份");
                object.to = calendar.add(Calendar.MONTH, 1).toString();
                paramsPageList.add(object);
            }
        }
        viewpager.getAdapter().notifyDataSetChanged();

        for (FragmentRecord.ParamsPage p : paramsPageList) {
            Log.i(TAG, "initData: from:" + p.from + " to:" + p.to);
        }

        ll_month.setVisibility(ChineseChat.isStudent() ? View.GONE : View.VISIBLE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.gravity = Gravity.CENTER_VERTICAL;
        for (int i = 0; i < paramsPageList.size(); i++) {
            TextView textView = new TextView(getApplicationContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText(paramsPageList.get(i).text);
            textView.setTextColor(getResources().getColorStateList(R.color.selector_text_normal));
            textView.setBackgroundResource(R.drawable.selector_indicator_record);
            ll_month.addView(textView, params);

            final int item = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewpager.setCurrentItem(item);
                }
            });
        }
        viewpager.setCurrentItem(paramsPageList.size() - 1);
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

        ll_month = (LinearLayout) findViewById(R.id.ll_month);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        viewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                FragmentRecord.ParamsPage paramsPage = paramsPageList.get(position);
                return FragmentRecord.newInstance(paramsPage.from, paramsPage.to);
            }

            @Override
            public int getCount() {
                return paramsPageList.size();
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
