package com.newclass.woyaoxue.adapter;

import java.util.List;

import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.voc.woyaoxue.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AdapterFolder extends BaseAdapter<Folder>
{
	private Context mContext;

	public AdapterFolder(List<Folder> list, Context content)
	{
		super(list);
		this.mContext = content;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Folder item = getItem(position);
		if (convertView == null)
		{
			convertView = View.inflate(mContext, R.layout.listitem_folder, null);
			ViewHolder holder = new ViewHolder();
			holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
			holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.tv_folder.setText(item.Name);
		holder.tv_counts.setText("课程:" + item.DocsCount);
		return convertView;
	}

	private class ViewHolder
	{
		public TextView tv_counts;
		public TextView tv_folder;
	}
}
