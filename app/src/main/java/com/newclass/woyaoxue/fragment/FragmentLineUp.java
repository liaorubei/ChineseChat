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
 * Created by liaorubei on 2016/1/14.
 */
public class FragmentLineUp extends android.support.v4.app.Fragment {

    private ContentViewLineUp l;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
}
