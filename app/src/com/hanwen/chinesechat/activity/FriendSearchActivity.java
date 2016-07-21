package com.hanwen.chinesechat.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class FriendSearchActivity extends Activity
{
	private List<User> list;
	private MyAdapter adapter;
	private ListView listview;
	private EditText et_keyword;
	protected boolean load;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friendsearch);

		initView();
		// initData();
	}

	private void initData()
	{
		getData(null);
	}

	private void getData(String keyword)
	{
		Parameters parameters = new Parameters();
		if (!TextUtils.isEmpty(keyword))
		{
			parameters.add("keyWord", keyword);
		}
		parameters.add("skip", 0 + "");
		parameters.add("take", 9 + "");
		HttpUtil.post(NetworkUtil.userSelect, parameters, new RequestCallBack<String>()
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
				load = false;
			}

			@Override
			public void onFailure(HttpException error, String msg)
			{
				CommonUtil.toast("加载失败");
				load = false;
			}
		});
	}

	private void initView()
	{
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		et_keyword.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_SEARCH)
				{
					String keyword = et_keyword.getText().toString().trim();
					if (TextUtils.isEmpty(keyword))
					{
						return false;
					}
					if (!load)
					{
						list.clear();
						adapter.notifyDataSetChanged();
						CommonUtil.toast("加载中...");
						load = true;
						getData(keyword);
					}
				}
				return true;
			}
		});

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
			View inflate = View.inflate(FriendSearchActivity.this, R.layout.listitem_add_friend, null);
			TextView tv_username = (TextView) inflate.findViewById(R.id.tv_username);
			TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
			TextView tv_category = (TextView) inflate.findViewById(R.id.tv_category);
			tv_username.setText(user.Username);
			tv_nickname.setText(user.Nickname);
			tv_category.setText(user.Category == 1 ? "教师" : "学生");

			Button bt_call = (Button) inflate.findViewById(R.id.bt_call);
			bt_call.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// 好友请求申请
					AddFriendData addFriendData = new AddFriendData(user.Accid, VerifyType.VERIFY_REQUEST, "好友申请");
					NIMClient.getService(FriendService.class).addFriend(addFriendData);
				}
			});

			return inflate;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.friend_activity, menu);
		return true;
	}
}
