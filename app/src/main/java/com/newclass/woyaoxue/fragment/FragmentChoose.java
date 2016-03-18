package com.newclass.woyaoxue.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nimlib.sdk.NIMClient;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.view.ContentViewChoose;

public class FragmentChoose extends Fragment {

    private ContentViewChoose contentView;
    private String TAG = "FragmentChoose";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        contentView = new ContentViewChoose(getActivity());
        return contentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ContentViewChoose.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return contentView.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        contentView.onResume();
        Log.i(TAG, "onResume: ");
        super.onResume();
    }
}
