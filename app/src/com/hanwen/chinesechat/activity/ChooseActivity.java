package com.hanwen.chinesechat.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseActivity extends Activity
{
	private List<User> list;
	private MyAdapter adapter;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);

		initView();
		// initData();

		SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
		int int1 = sp.getInt("id", 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.choose_activity, menu);
		return true;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_refresh_teacher:
			initData();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initData()
	{
		Parameters parameters = new Parameters();
		parameters.add("skip", 0 + "");
		parameters.add("take", 5 + "");
		HttpUtil.post(NetworkUtil.teacherInQueue, parameters, new RequestCallBack<String>()
		{

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo)
			{
				Response<List<User>> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<User>>>()
				{}.getType());

				if (response.code == 200 && response.info != null)
				{
					List<User> users = response.info;
					list.clear();
					for (User user : users)
					{
						list.add(user);
					}
					adapter.notifyDataSetChanged();
				}
				Log.i("logi", "数据更新");
			}

			@Override
			public void onFailure(HttpException error, String msg)
			{}
		});
	}

	private void initView()
	{
		listview = (ListView) findViewById(R.id.listview);
		list = new ArrayList<User>();
		adapter = new MyAdapter(list);
		listview.setAdapter(adapter);
	}

	private class MyAdapter extends BaseAdapter<User>
	{

		public MyAdapter(List<User> list)
		{
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final User user = list.get(position);
			View inflate = View.inflate(ChooseActivity.this, R.layout.listitem_choose, null);
			TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
			TextView tv_username = (TextView) inflate.findViewById(R.id.tv_username);
			TextView tv_category = (TextView) inflate.findViewById(R.id.tv_category);
			tv_nickname.setText(user.Nickname);
			tv_username.setText(user.Username);
			tv_category.setText(user.Category == 1 ? "教师" : "学生");

			Button bt_call = (Button) inflate.findViewById(R.id.bt_call);
			bt_call.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					Parameters parameters = new Parameters();
					parameters.add("id", 1 + "");
					parameters.add("target", user.Id + "");
					HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>()
					{

						@Override
						public void onSuccess(ResponseInfo<String> responseInfo)
						{
							Intent intent = new Intent(getApplication(), ActivityCall.class);
							intent.putExtra(ActivityCall.KEY_TARGET_ACCID, user.Accid);
							intent.putExtra(ActivityCall.KEY_TARGET_NICKNAME, user.Nickname);
							intent.putExtra(ActivityCall.CALL_TYPE_KEY, ActivityCall.CALL_TYPE_AUDIO);
							startActivity(intent);
						}

						@Override
						public void onFailure(HttpException error, String msg)
						{
							// TODO Auto-generated method stub

						}
					});

				}
			});

			return inflate;
		}
	}
}
