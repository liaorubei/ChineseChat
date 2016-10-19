package com.hanwen.chinesechat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.util.Log;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCourse extends Fragment {

    private static final String TAG = "FragmentCourse";
    private static final String KEY_OPEN_MODE = "param1";
    private static final String KEY_ID = "param2";
    public static final int OPEN_MODE_ROOT = 1;
    public static final int OPEN_MODE_SHOW = 2;
    public static final int OPEN_MODE_NONE = 3;

    private int openMode;
    private int id;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param openMode Parameter 1.
     * @param Id       Parameter 2.
     * @return A new instance of fragment FragmentCourse.
     */
    public static FragmentCourse newInstance(int openMode, int Id) {
        FragmentCourse fragment = new FragmentCourse();
        Bundle args = new Bundle();
        args.putInt(KEY_OPEN_MODE, openMode);
        args.putInt(KEY_ID, Id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            openMode = getArguments().getInt(KEY_OPEN_MODE);
            id = getArguments().getInt(KEY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: ");
        if (openMode != OPEN_MODE_NONE) {
            getChildFragmentManager().beginTransaction().replace(R.id.fl_content, openMode == OPEN_MODE_ROOT ? FragmentCourseNest.newInstance("", 1, "") : FragmentCourseShow.newInstance(id, false)).addToBackStack("FragmentCourseClear").commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }
}
