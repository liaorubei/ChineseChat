package com.newclass.woyaoxue.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newclass.woyaoxue.fragment.FragmentChoose;
import com.newclass.woyaoxue.fragment.FriendFragment;
import com.newclass.woyaoxue.fragment.RandomFragment;
import com.newclass.woyaoxue.view.LazyViewPager;
import com.voc.woyaoxue.R;

public class StudentActivity extends FragmentActivity implements OnClickListener
{
	private LazyViewPager viewpager;
	private LinearLayout ll_ctrl;
	private TextView tv_random, tv_choose, tv_groups, tv_friend;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student);

		initView();

		PagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewpager.setAdapter(myPagerAdapter);

		// 自动登录
		SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
		String username = sp.getString("username", "");
		String password = sp.getString("password", "");

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
		{}
		startActivity(new Intent(this, ActivitySignIn.class));
	}

	private void initView()
	{
		viewpager = (LazyViewPager) findViewById(R.id.viewpager);
		tv_random = (TextView) findViewById(R.id.tv_random);
		tv_choose = (TextView) findViewById(R.id.tv_choose);
		tv_groups = (TextView) findViewById(R.id.tv_groups);
		tv_friend = (TextView) findViewById(R.id.tv_friend);

		tv_choose.setOnClickListener(this);
		tv_groups.setOnClickListener(this);
		tv_friend.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.viewpager:

			break;
		case R.id.tv_choose:
		{
			Intent intent = new Intent(StudentActivity.this, ChooseActivity.class);
			startActivity(intent);
		}

			break;


		case R.id.tv_friend:
		{
			Intent intent = new Intent(StudentActivity.this, FriendActivity.class);
			startActivity(intent);
		}
			break;

		default:
			break;
		}

	}

	public class MyPagerAdapter extends FragmentPagerAdapter
	{

		public MyPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
			case 0:
				return new RandomFragment();
			// break;
			case 1:
				return new FragmentChoose();
			// break;

			// break;
			case 3:
				return new FriendFragment();
			// break;
			default:
				break;
			}

			return new RandomFragment();
		}

		@Override
		public int getCount()
		{

			return 1;
		}
	}

}
