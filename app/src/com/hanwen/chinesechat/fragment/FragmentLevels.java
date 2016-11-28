package com.hanwen.chinesechat.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Level;

import java.util.ArrayList;

/**
 * 课本分类
 */
public class FragmentLevels extends Fragment {
    private static final String TAG = "FragmentLevels";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_levels, container, false);
        FragmentTabHost host = (FragmentTabHost) view.findViewById(android.R.id.tabhost);

        host.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        ArrayList<Level> levels = new ArrayList<>();
        levels.add(new Level(8, "TEXTBOOK", 1));
        levels.add(new Level(1, "LEVEL_1", 0));
        levels.add(new Level(2, "LEVEL_2", 0));
        ColorStateList colors = getResources().getColorStateList(R.color.selector_text_normal);
        for (Level level : levels) {
            TextView textView = new TextView(getActivity());
            textView.setText(level.Name);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(colors);
            //参数
            Bundle args = new Bundle();
            args.putParcelable(FragmentFolder.KEY_LEVEL, level);
            host.addTab(host.newTabSpec(level.Name).setIndicator(textView), FragmentFolder.class, args);
        }

        TextView news = new TextView(getContext());
        news.setText("NEWS");
        news.setGravity(Gravity.CENTER);
        news.setTextColor(colors);
        host.addTab(host.newTabSpec("NEWS").setIndicator(news), FragmentNews.class, null);
        return view;
    }
}
