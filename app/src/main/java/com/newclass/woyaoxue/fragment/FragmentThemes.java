package com.newclass.woyaoxue.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        HsLevel hsLevel = gson.fromJson(getArguments().getString("HsLevel"), new TypeToken<HsLevel>() {
        }.getType());

        View inflate = inflater.inflate(R.layout.fragment_themes, container, false);
        gridView = (GridView) inflate.findViewById(R.id.gridView);
        list = new ArrayList<>();
        for (Theme o : hsLevel.Theme) {
            list.add(new ViewModel(o));
        }
        adapter = new MyAdapter(list);
        gridView.setAdapter(adapter);
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
            TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
            tv_theme.setText(item.Name);
            inflate.findViewById(R.id.iv_card).setVisibility(item.isChecked ? View.INVISIBLE : View.VISIBLE);
            return inflate;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
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

    public class ViewModel extends Theme {
        public boolean isChecked;

        public ViewModel(Theme theme) {
            this.Id=theme.Id;
            this.Name=theme.Name;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(ViewModel theme);
    }
}
