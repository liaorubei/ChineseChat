package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;

import com.newclass.woyaoxue.view.LazyViewPager;
import com.voc.woyaoxue.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.LinearLayout;

public class ThemeActivity extends FragmentActivity
{
	private LinearLayout ll_indicator;
	private LazyViewPager viewPager;
	private List<Fragment> fragments;
	private FragmentPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme);

		initView();
		initData();
	}

	private void initData()
	{
		//get

	}

	private void initView()
	{
		ll_indicator = (LinearLayout) findViewById(R.id.ll_indicator);
		viewPager = (LazyViewPager) findViewById(R.id.viewpager);
		fragments = new ArrayList<Fragment>();
		adapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter
	{

		public MyPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int location)
		{
			return fragments.get(location);
		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}
	}
}
