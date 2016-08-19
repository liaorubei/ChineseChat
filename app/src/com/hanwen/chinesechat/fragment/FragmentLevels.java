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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLevels extends Fragment {

    private static final String TAG = "FragmentLevels";
    private ArrayList<Level> levels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levels = getArguments().getParcelableArrayList("levels");
        List<Level> le = new Gson().fromJson(getArguments().getString("Le"), new TypeToken<List<Level>>() {}.getType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_levels, container, false);

        FragmentTabHost tabhost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        tabhost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        ColorStateList colors = getResources().getColorStateList(R.color.selector_text_normal);

        Collections.sort(levels, new Comparator<Level>() {
            @Override
            public int compare(Level lhs, Level rhs) {
                return Integer.valueOf(rhs.Sort).compareTo(lhs.Sort);
            }
        });

        for (Level l : levels) {
            TextView textView = new TextView(getActivity());
            textView.setText(l.Name);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(colors);

            Bundle args = new Bundle();
            args.putParcelable(FragmentFolder.KEY_LEVEL, l);
            tabhost.addTab(tabhost.newTabSpec(l.Name).setIndicator(textView), FragmentFolder.class, args);
        }
        return view;
    }
}
