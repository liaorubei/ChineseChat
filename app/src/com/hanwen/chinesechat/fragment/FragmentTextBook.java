package com.hanwen.chinesechat.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.util.Log;

/**
 * A simple {@link Fragment} subclass.
 * <p/>
 * create an instance of this fragment.
 */
public class FragmentTextBook extends Fragment {
    private static final String TAG = "FragmentTextBook";
    private Fragment fragmentFolderDone;
    private Fragment fragmentFolderTodo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentFolderDone = new FragmentFolderDone();
        fragmentFolderTodo = new FragmentLevels();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_text_book, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ViewPager viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        viewpager.setAdapter(new MyAdapter(getChildFragmentManager()));//在Fragment里面管理Fragment应该使用getChildFragmentManager()
    }


    public class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ? fragmentFolderTodo : fragmentFolderDone;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
