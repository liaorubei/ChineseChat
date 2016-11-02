package com.hanwen.chinesechat.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.view.FlowLayout;

import java.util.Random;

/**
 * 话题选择界面，流式与随机方式
 */
public class FragmentTopics extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FragmentTopics() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentTopics.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTopics newInstance(String param1, String param2) {
        FragmentTopics fragment = new FragmentTopics();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup fl_content = (ViewGroup) view.findViewById(R.id.fl_content);
        FlowLayout flowLayout = new FlowLayout(getContext());

        for (int i = 0; i < 20; i++) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView textView = new TextView(getContext());
            textView.setText("中文 中文 中文 中文 中文 中文  中文 中文 中文  中文 中文 中文  中文 中文 中文 " + i);
            textView.setSingleLine();
            textView.setLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Color.WHITE);
            //设置文字在背景内部剧中
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout.addView(textView);


            TextView textViewEn = new TextView(getContext());
            textViewEn.setText("English English English English English English English English English " + i);
            textViewEn.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewEn.setLines(1);
            textViewEn.setEllipsize(TextUtils.TruncateAt.END);
            linearLayout.addView(textViewEn);

            linearLayout.setBackgroundResource(R.drawable.shape_rectangle_radius05_green);
            flowLayout.addView(linearLayout);
        }
        fl_content.addView(flowLayout);

    }
}
