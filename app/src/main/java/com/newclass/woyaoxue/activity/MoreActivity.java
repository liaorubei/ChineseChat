package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Level;
import com.voc.woyaoxue.R;

public class MoreActivity extends Activity
{
	private ListView listview;
	private List<Level> list;
	private MyAdapter myAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		listview = (ListView) findViewById(R.id.listview);

		String json = getIntent().getStringExtra("hideLevels");
		List<Level> fromJson = new Gson().fromJson(json, new TypeToken<List<Level>>()
		{}.getType());
		list = new ArrayList<Level>();
		if (fromJson != null)
		{
			list.addAll(fromJson);
		}

		myAdapter = new MyAdapter(list);
		listview.setAdapter(myAdapter);
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Toast.makeText(MoreActivity.this, "你点击了", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MyAdapter extends BaseAdapter<Level>
	{

		public MyAdapter(List<Level> list)
		{
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = new TextView(MoreActivity.this);
				convertView.setPadding(25, 25, 25, 25);
			}
			((TextView) convertView).setText(list.get(position).Name);
			return convertView;
		}
	}

}
