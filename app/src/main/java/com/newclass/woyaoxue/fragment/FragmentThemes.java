package com.newclass.woyaoxue.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.HsLevel;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.view.RotateAnimation;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentThemes extends Fragment {
    private static final String TAG = "FragmentThemes";
    public static final int RESULTCODE_CHOOSE = 1;
    private Gson gson = new Gson();
    private MyAdapter adapter;
    private GridView gridView;
    private List<ViewModel> list;
    private HsLevel hsLevel;
    private ViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        int levelId = getArguments().getInt("levelId", 0);
        hsLevel = gson.fromJson(getArguments().getString("HsLevel"), new TypeToken<HsLevel>() {
        }.getType());

        View inflate = inflater.inflate(R.layout.fragment_themes, container, false);
        gridView = (GridView) inflate.findViewById(R.id.gridView);
        list = new ArrayList<>();
        int position = 0;
        for (int i = 0; i < hsLevel.Theme.size(); i++) {
            model = new ViewModel(hsLevel.Theme.get(i), levelId);
            list.add(model);
            position = model.isChecked ? i : 0;
        }
        Log.i(TAG, "onCreateView: " + position);

        adapter = new MyAdapter(list);
        gridView.setAdapter(adapter);
        //gridView.setSelection(position);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).isChecked = i == position;
                }

                //通知并回传到Activity
                onButtonPressed(list.get(position));

                float cX = item.getWidth() / 2.0f;
                float cY = item.getHeight() / 2.0f;
                RotateAnimation rotate = new RotateAnimation(cX, cY);

                rotate.setInterpolatedTimeListener(new RotateAnimation.InterpolatedTimeListener() {
                    @Override
                    public void interpolatedTime(float interpolatedTime, View view) {
                        if (interpolatedTime > 0.5) {
                            view.findViewById(R.id.iv_card).setVisibility(View.INVISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, item);
                rotate.setFillAfter(true);
                item.startAnimation(rotate);
            }
        });
        return inflate;
    }

    private class MyAdapter extends BaseAdapter<ViewModel> {
        public MyAdapter(List<ViewModel> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewModel item = getItem(position);
            View inflate = View.inflate(getActivity(), R.layout.griditem_card, null);
            inflate.findViewById(R.id.iv_card).setBackgroundColor(hsLevel.Color);

            TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
            TextView tv_number = (TextView) inflate.findViewById(R.id.tv_number);

            tv_theme.setText(item.Name);
            tv_number.setText("" + (position + 1));

            inflate.findViewById(R.id.iv_card).setVisibility(item.isChecked ? View.INVISIBLE : View.VISIBLE);
            return inflate;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 1, 1, "选择").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Theme temp = null;
                for (ViewModel model : list) {
                    if (model.isChecked) {
                        temp = model;
                    }
                }

                if (temp == null) {
                    CommonUtil.toast("你还没有选择");
                } else {
                    Intent data = new Intent();
                    data.putExtra("theme", gson.toJson(temp));
                    getActivity().setResult(RESULTCODE_CHOOSE, data);
                    getActivity().finish();
                }
                break;
        }
        return true;
    }

    public static class ViewModel extends Theme {
        public boolean isChecked;

        public ViewModel(Theme theme, int selectLevelId) {
            this.Id = theme.Id;
            this.Name = theme.Name;
            this.isChecked = this.Id == selectLevelId;
        }
    }

    private OnFragmentInteractionListener mListener;

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public void onButtonPressed(ViewModel uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ViewModel theme);
    }
}
