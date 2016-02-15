package com.newclass.woyaoxue.fragment;

import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentViewListen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListenFragment extends Fragment {
    private static final String TAG = "ListenFragment";
    private ContentView contentView;
    private FragmentManager supportManager;

    public ListenFragment() {
        super();
    }

    public ListenFragment(FragmentManager fm) {
        super();
        this.supportManager = fm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        if (contentView == null) {
            contentView = new ContentViewListen(getActivity(), supportManager);
        }
        return contentView;
    }
}
