package com.newclass.woyaoxue.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentView.ViewState;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
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
 * 我的下载--文档列表显示界面
 * 
 * @author liaorubei
 *
 */
public class ActivityDocument extends Activity implements OnClickListener {
	private static final String TAG = "ActivityDocument";
	private List<ViewHelper> list;
	private int folderId;
	private ContentView contentView;
	private MyAdapter adapter;

	private Database database;
	private ListView listview;

	private int levelId;

	private View ll_ctrl;
	private CheckBox cb_select;
	private CheckBox cb_Invert;
	private TextView tv_delete;
	private TextView tv_cancel;
	protected TextView tv_folder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentView = new ContentView(this) {

			@Override
			public View onCreateSuccessView() {
				View view = View.inflate(ActivityDocument.this, R.layout.activity_downdocs, null);
				tv_folder = (TextView) view.findViewById(R.id.tv_folder);
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

		// 取得传递过来的数据
		Intent intent = getIntent();
		levelId = intent.getIntExtra("LevelId", 0);
		folderId = intent.getIntExtra("FolderId", 16);
		String folderName = intent.getStringExtra("FolderName");
		tv_folder.setText(folderName);

		list = new ArrayList<ViewHelper>();
		adapter = new MyAdapter(list);
		listview.setAdapter(adapter);
		// 其他设置
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent play = new Intent(ActivityDocument.this, PlayActivity.class);
				play.putExtra("Id", list.get(position).document.Id);
				startActivity(play);
			}
		});

		getActionBar().setDisplayHomeAsUpEnabled(true);

		loadMore();

		cb_select.setOnClickListener(this);
		cb_Invert.setOnClickListener(this);
		tv_delete.setOnClickListener(this);
		tv_cancel.setOnClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case 1:
			ll_ctrl.setVisibility(View.VISIBLE);

			for (ViewHelper i : list) {
				i.isShow = true;
			}
			adapter.notifyDataSetChanged();

			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//MenuInflater menuInflater = getMenuInflater();
		//menuInflater.inflate(R.menu.menu_downdocs, menu);
		menu.add(Menu.NONE,1,1,"删除").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.closeConnection();
	}

	private void loadMore() {
		new AsyncTask<Integer, Integer, List<Document>>() {

			@Override
			protected List<Document> doInBackground(Integer... params) {
				return database.docsSelectListByFolderId(params[0]);
			}

			protected void onPostExecute(java.util.List<Document> result) {
				for (Document document : result) {
					list.add(new ViewHelper(document, false, false));
				}
				adapter.notifyDataSetChanged();
				contentView.showView(result.size() > 0 ? ViewState.SUCCESS : ViewState.EMPTY);
			}
		}.execute(folderId);
	}

	private class MyAdapter extends BaseAdapter<ViewHelper> {

		public MyAdapter(List<ViewHelper> list) {
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHelper item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(ActivityDocument.this, R.layout.listitem_downdocs, null);
				ViewHolder holder = new ViewHolder();
				holder.tv_title_one = (TextView) convertView.findViewById(R.id.tv_title_one);
				holder.tv_title_two = (TextView) convertView.findViewById(R.id.tv_title_two);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

				holder.cb_delete = (CheckBox) convertView.findViewById(R.id.cb_delete);
				convertView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.tv_title_one.setText(item.document.Title);
			holder.tv_title_two.setText(item.document.TitleTwo);
			holder.tv_date.setText(item.document.DateString);
			holder.tv_size.setText(Formatter.formatFileSize(ActivityDocument.this, item.document.Length));
			holder.tv_time.setText(item.document.LengthString);

			holder.cb_delete.setChecked(item.isChecked);
			holder.cb_delete.setVisibility(item.isShow ? View.VISIBLE : View.GONE);
			holder.cb_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					item.isChecked = ((CheckBox) v).isChecked();
				}
			});

			return convertView;
		}

	}

	private class ViewHolder {
		public TextView tv_title_one;
		public TextView tv_title_two;
		public TextView tv_date;
		public TextView tv_size;
		public TextView tv_time;
		public CheckBox cb_delete;
	}

	private class ViewHelper {
		public ViewHelper(Document doc, boolean check, boolean show) {
			this.document = doc;
			this.isChecked = check;
			this.isShow = show;
		}

		public boolean isShow;
		public boolean isChecked;
		public Document document;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cb_select:
			cb_Invert.setChecked(false);
			for (ViewHelper i : list) {
				i.isChecked = cb_select.isChecked();
				i.isShow = true;
			}
			adapter.notifyDataSetChanged();

			break;

		case R.id.cb_Invert:
			cb_Invert.setChecked(true);
			cb_select.setChecked(false);
			for (ViewHelper i : list) {
				i.isChecked = !i.isChecked;
				i.isShow = true;
			}
			adapter.notifyDataSetChanged();

			break;

		case R.id.tv_delete:
			List<ViewHelper> removeList = new ArrayList<ActivityDocument.ViewHelper>();// 被删除的集合

			for (ViewHelper viewHelper : list) {
				if (viewHelper.isChecked) {
					removeList.add(viewHelper);// 把被删除的对象收集到一个集合中
				}
			}

			list.removeAll(removeList);

			// 清除数据库及文件夹里面的数据
			for (ViewHelper viewHelper : removeList) {
				// 从数据库移除
				database.docsDeleteById(viewHelper.document.Id);

				// 从文件夹移除
				File file = new File(FolderUtil.rootDir(ActivityDocument.this), viewHelper.document.SoundPath);
				if (file.isFile() && file.exists()) {
					file.delete();
				}
				Log.i("" + viewHelper.document.Title + " " + file.getAbsolutePath() + " 被移除了");
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
				i.isShow = false;
			}
			adapter.notifyDataSetChanged();
			break;
		}
	}

}
