package com.newclass.woyaoxue.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.newclass.woyaoxue.view.ContentViewChoose;
import com.newclass.woyaoxue.view.ContentViewLineUp;

/**
 * 教师端教师排队界面
 * Created by liaorubei on 2016/1/14.
 */
public class FragmentLineUp extends android.support.v4.app.Fragment {

    private ContentViewLineUp l;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        l = new ContentViewLineUp(getActivity());
        return l;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        l.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return l.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        l.onResume();
    }
}
