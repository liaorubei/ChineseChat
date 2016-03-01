package com.newclass.woyaoxue.view;

import android.content.Context;
import android.view.View;
import android.widget.GridView;

import com.google.gson.Gson;
import com.newclass.woyaoxue.adapter.AdapterCard;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Theme;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaorubei on 2016/1/15.
 */
public class ContentViewCard extends ContentView {
    private List<Theme> list = new ArrayList<>();
    private Gson gson=new Gson();
    private BaseAdapter<Theme> adapter;

    public ContentViewCard(Context context) {
        super(context);
    }

    @Override
    public void initData() {

    }

    @Override
    public View onCreateSuccessView() {
        View inflate = View.inflate(getContext(), R.layout.activity_card, null);
        GridView gridView = (GridView) inflate.findViewById(R.id.gridView);
        adapter = new AdapterCard(list, getContext());
        gridView.setAdapter(adapter);

        return inflate;
    }
}
