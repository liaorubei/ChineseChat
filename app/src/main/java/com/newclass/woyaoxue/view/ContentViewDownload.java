package com.newclass.woyaoxue.view;

import java.util.ArrayList;
import java.util.List;

import com.newclass.woyaoxue.activity.DownDocsActivity;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class ContentViewDownload extends ContentView
{

	private TextView tv_cancel;
	private TextView tv_delete;
	private CheckBox cb_Invert;
	private CheckBox cb_select;
	private View ll_ctrl;
	private ListView listview;
	private Database database;
	private ArrayList<ViewHelper> list;
	private MyAdapter adapter;

	public ContentViewDownload(Context context)
	{
		super(context);
		initData();
	}

	public void initData()
	{

		database = new Database(getContext());
		List<Folder> folders = database.folderSelectListWithDocsCount();

		list = new ArrayList<ViewHelper>();
		for (Folder folder : folders)
		{
			if (folder.DocsCount > 0)
			{
				list.add(new ViewHelper(folder, false, false));
			}
		}

	

		adapter = new MyAdapter(list);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Log.i("点击了");
				Intent intent = new Intent(getContext(), DownDocsActivity.class);
				intent.putExtra("FolderId", list.get(position).folder.Id);
				intent.putExtra("FolderName", list.get(position).folder.Name);
				getContext().startActivity(intent);
			}
		});
		showView(ViewState.SUCCESS);
	}

	@Override
	public View onCreateSuccessView()
	{
		View view = View.inflate(getContext(), R.layout.activity_down, null);
		listview = (ListView) view.findViewById(R.id.listview);

		ll_ctrl = view.findViewById(R.id.ll_ctrl);
		cb_select = (CheckBox) view.findViewById(R.id.cb_select);
		cb_Invert = (CheckBox) view.findViewById(R.id.cb_Invert);
		tv_delete = (TextView) view.findViewById(R.id.tv_delete);
		tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
		return view;
	}
	
	private class MyAdapter extends BaseAdapter<ViewHelper> {

		public MyAdapter(List<ViewHelper> list) {
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHelper item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.listitem_folder, null);
				ViewHolder holder = new ViewHolder();
				holder.cb_delete = (CheckBox) convertView.findViewById(R.id.cb_delete);
				holder.tv_folder = (TextView) convertView.findViewById(R.id.tv_folder);
				holder.tv_counts = (TextView) convertView.findViewById(R.id.tv_counts);
				convertView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.cb_delete.setVisibility(item.isVisible ? View.VISIBLE : View.GONE);
			holder.cb_delete.setChecked(item.isChecked);
			holder.cb_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					item.isChecked = ((CheckBox) v).isChecked();
				}
			});
			holder.tv_folder.setText(item.folder.Name);
			holder.tv_counts.setText("课程:" + item.folder.DocsCount);
			return convertView;
		}
	}

	private class ViewHelper
	{
		public Folder folder;
		public boolean isChecked;
		public boolean isVisible;

		public ViewHelper(Folder pFolder, boolean checked, boolean visible)
		{
			this.folder = pFolder;
			this.isChecked = checked;
			this.isVisible = visible;
		}
	}

	private class ViewHolder
	{
		public CheckBox cb_delete;
		public TextView tv_counts;
		public TextView tv_folder;
	}

}
