package com.newclass.woyaoxue.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentView.ViewState;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 我的下载界面,显示所有已经下载的文件,方面再次打开和管理
 * 
 * @author liaorubei
 *
 */
public class DownFolderActivity extends Activity implements OnClickListener {

	private MyAdapter adapter;
	private ContentView contentView;
	private List<ViewHelper> list;
	private ListView listview;
	protected CheckBox cb_Invert;
	protected CheckBox cb_select;
	protected View ll_ctrl;
	protected TextView tv_cancel;
	protected TextView tv_delete;
	private Database database;

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.cb_select:
			cb_Invert.setChecked(false);
			for (ViewHelper i : list) {
				i.isChecked = cb_select.isChecked();
				i.isVisible = true;
			}
			adapter.notifyDataSetChanged();

			break;

		case R.id.cb_Invert:
			cb_Invert.setChecked(true);
			cb_select.setChecked(false);
			for (ViewHelper i : list) {
				i.isChecked = !i.isChecked;
				i.isVisible = true;
			}
			adapter.notifyDataSetChanged();

			break;

		case R.id.tv_delete:
			List<ViewHelper> removeList = new ArrayList<ViewHelper>();// 被删除的集合

			for (ViewHelper viewHelper : list) {
				if (viewHelper.isChecked) {
					removeList.add(viewHelper);// 把被删除的对象收集到一个集合中
				}
			}

			list.removeAll(removeList);

			// 清除数据库及文件夹里面的数据
			for (ViewHelper viewHelper : removeList) {
				// 从数据库移除
				List<Document> docs = database.docsSelectListByFolderId(viewHelper.folder.Id);
				for (Document doc : docs) {
					database.docsDeleteById(doc.Id);
					File file = new File(FolderUtil.rootDir(DownFolderActivity.this), doc.SoundPath);
					if (file.exists() && file.isFile()) {
						file.delete();
					}
				}
			}

			adapter.notifyDataSetChanged();
			break;

		case R.id.tv_cancel:
			// 按钮重置
			cb_select.setChecked(false);
			cb_Invert.setChecked(false);
			ll_ctrl.setVisibility(View.GONE);

			for (ViewHelper i : list) {
				i.isChecked = false;
				i.isVisible = false;
			}
			adapter.notifyDataSetChanged();
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_downfolder, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;

		case R.id.menu_delete:
			ll_ctrl.setVisibility(View.VISIBLE);
			for (ViewHelper i : list) {
				i.isVisible = true;
				i.isChecked = false;
			}
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("FolderDownActivity.onCreate");
		super.onCreate(savedInstanceState);

		contentView = new ContentView(this) {

			@Override
			public View onCreateSuccessView() {
				View view = View.inflate(DownFolderActivity.this, R.layout.activity_down, null);
				listview = (ListView) view.findViewById(R.id.listview);

				ll_ctrl = view.findViewById(R.id.ll_ctrl);
				cb_select = (CheckBox) view.findViewById(R.id.cb_select);
				cb_Invert = (CheckBox) view.findViewById(R.id.cb_Invert);
				tv_delete = (TextView) view.findViewById(R.id.tv_delete);
				tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
				return view;
			}

			@Override
			public void initData()
			{
				// TODO Auto-generated method stub
				
			}
		};

		setContentView(contentView);

		database = new Database(this);
		List<Folder> folders = database.folderSelectListWithDocsCount();

		list = new ArrayList<ViewHelper>();
		for (Folder folder : folders) {
			if (folder.DocsCount > 0) {
				list.add(new ViewHelper(folder, false, false));
			}
		}

		contentView.showView(ViewState.SUCCESS);

		adapter = new MyAdapter(list);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i("点击了");
				Intent intent = new Intent(DownFolderActivity.this, DownDocsActivity.class);
				intent.putExtra("FolderId", list.get(position).folder.Id);
				intent.putExtra("FolderName", list.get(position).folder.Name);
				startActivity(intent);

			}
		});

		// 返回首页按钮
		getActionBar().setDisplayHomeAsUpEnabled(true);

		cb_select.setOnClickListener(this);
		cb_Invert.setOnClickListener(this);
		tv_delete.setOnClickListener(this);
		tv_cancel.setOnClickListener(this);

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		database.closeConnection();
	}

	private class MyAdapter extends BaseAdapter<ViewHelper> {

		public MyAdapter(List<ViewHelper> list) {
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHelper item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(DownFolderActivity.this, R.layout.listitem_folder, null);
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

	private class ViewHelper {
		public Folder folder;
		public boolean isChecked;
		public boolean isVisible;

		public ViewHelper(Folder pFolder, boolean checked, boolean visible) {
			this.folder = pFolder;
			this.isChecked = checked;
			this.isVisible = visible;
		}
	}

	private class ViewHolder {
		public CheckBox cb_delete;
		public TextView tv_counts;
		public TextView tv_folder;
	}

}
