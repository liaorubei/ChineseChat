package com.hanwen.chinesechat.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Question;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.Theme;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionActivity extends Activity
{
	private ListView listview;
	private TextView tv_theme;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question);
		initView();
		initData();

		if( getActionBar()!=null){
			getActionBar().setDisplayHomeAsUpEnabled(true);}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return true;
	}

	private void initView()
	{
		tv_theme = (TextView) findViewById(R.id.tv_theme);
		listview = (ListView) findViewById(R.id.listview);

		list = new ArrayList<Question>();
		adapter = new MyAdapter(list);
		listview.setAdapter(adapter);
	}

	Gson gson = new Gson();
	private List<Question> list;
	private BaseAdapter<Question> adapter;

	private void initData()
	{
		Intent intent = getIntent();
		int themeId = intent.getIntExtra("themeId", 0);
		Parameters parameters = new Parameters();
		parameters.add("id", themeId + "");
		HttpUtil.post(NetworkUtil.themeGetById, parameters, new RequestCallBack<String>()
		{

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo)
			{
				CommonUtil.toast("数据成功");
				Response<Theme> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Theme>>()
				{}.getType());
				if (resp.code == 200)
				{
					//显示主题的名字
					tv_theme.setText(resp.info.Name);
					
					//显示主题对应的问题
					list.clear();
					List<Question> questions = resp.info.Questions;
					for (Question question : questions)
					{
						list.add(question);
					}
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onFailure(HttpException error, String msg)
			{
				

			} 		});

	}

	private class MyAdapter extends BaseAdapter<Question>
	{

		public MyAdapter(List<Question> list)
		{
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Question item = getItem(position);

			TextView textView = new TextView(getApplication());
			textView.setText(item.Name);
			textView.setTextColor(Color.BLACK);
			return textView;
		}
	}
}
