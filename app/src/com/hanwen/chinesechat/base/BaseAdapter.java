package com.hanwen.chinesechat.base;

import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter
{
	private List<T> data;

	public BaseAdapter(List<T> list)
	{
		this.data = list;
	}

	@Override
	public int getCount()
	{
		return this.data.size();
	}

	@Override
	public T getItem(int position)
	{
		return this.data.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}
}
