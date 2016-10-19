package com.hanwen.chinesechat.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.HsLevel;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.view.RotateAnimation;

import java.util.ArrayList;
import java.util.List;

public class FragmentThemes extends Fragment {
    private static final String TAG = "FragmentThemes";
    public static final int RESULTCODE_CHOOSE = 1;
    private Gson gson = new Gson();
    private MyAdapter adapter;
    private List<Theme> dataSet;
    private HsLevel hsLevel;

    private OnFragmentInteractionListener fragmentInteractionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hsLevel = gson.fromJson(getArguments().getString("HsLevel"), new TypeToken<HsLevel>() {
        }.getType());
        dataSet = new ArrayList<>();
        for (Theme theme : hsLevel.Theme) {
            dataSet.add(theme);
        }
        adapter = new MyAdapter(dataSet);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_themes, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: " + view);
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        //gridView.setSelection(position);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                Theme theme = dataSet.get(position);
                Theme currentTheme = fragmentInteractionListener.getCurrentTheme();
                if (theme.Id == currentTheme.Id) {
                    return;
                }
                onButtonPressed(theme, FragmentThemes.this);

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = parent.getChildAt(i);

                    childAt.clearAnimation();
                    if (childAt.findViewById(R.id.iv_card).getVisibility() == View.INVISIBLE) {
                        RotateAnimation animation = new RotateAnimation(item.getWidth() / 2, item.getHeight() / 2);
                        animation.setDuration(500);
                        animation.setInterpolatedTimeListener(new RotateAnimation.InterpolatedTimeListener() {
                            @Override
                            public void interpolatedTime(float interpolatedTime, View view) {
                                if (interpolatedTime > 0.5) {
                                    view.findViewById(R.id.iv_card).setVisibility(View.VISIBLE);
                                }
                            }
                        }, childAt);
                        childAt.startAnimation(animation);
                    }
                }

                RotateAnimation animation = new RotateAnimation(item.getWidth() / 2, item.getHeight() / 2);
                animation.setDuration(500);
                animation.setInterpolatedTimeListener(new RotateAnimation.InterpolatedTimeListener() {
                    @Override
                    public void interpolatedTime(float interpolatedTime, View view) {
                        if (interpolatedTime > 0.5) {
                            view.findViewById(R.id.iv_card).setVisibility(View.INVISIBLE);
                        }
                    }
                }, item);
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.85f, 1f, 0.85f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                //scaleAnimation.setDuration(2500);
                AnimationSet set = new AnimationSet(false);
                //set.addAnimation(scaleAnimation);
                set.addAnimation(animation);
                item.startAnimation(set);
            }
        });

    }

    private class MyAdapter extends BaseAdapter<Theme> {
        public MyAdapter(List<Theme> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Theme item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.griditem_card, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            convertView.clearAnimation();//清除动画效果,否则在复用布局的时候,动画会跟着走;因为在选择主题时添加了一个3D旋转的动画
            holder.tv_theme.setText(item.Name);
            holder.tv_number.setText(position + "");
            holder.iv_card.setVisibility(item.Id == fragmentInteractionListener.getCurrentTheme().Id ? View.INVISIBLE : View.VISIBLE);
            return convertView;
        }
    }

    private class ViewHolder {
        private TextView tv_theme;
        private View iv_card;
        private TextView tv_number;

        public ViewHolder(View convertView) {
            convertView.setTag(this);
            this.iv_card = convertView.findViewById(R.id.iv_card);
            this.iv_card.setBackgroundColor(hsLevel.Color);//添加背景颜色,用以区分不同级别的主题
            this.tv_theme = (TextView) convertView.findViewById(R.id.tv_theme);
            this.tv_number = (TextView) convertView.findViewById(R.id.tv_number);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            fragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public void onButtonPressed(Theme theme, FragmentThemes fragmentThemes) {
        if (fragmentInteractionListener != null) {
            fragmentInteractionListener.onFragmentInteraction(theme, fragmentThemes);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Theme theme, Fragment fragmentThemes);

        Theme getCurrentTheme();
    }
}
