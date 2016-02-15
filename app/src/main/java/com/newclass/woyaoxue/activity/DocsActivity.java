package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.DownloadInfo;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.service.DownloadService.MyBinder;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.CircularProgressBar;
import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentView.ViewState;
import com.newclass.woyaoxue.view.XListView;
import com.newclass.woyaoxue.view.XListView.IXListViewListener;
import com.newclass.woyaoxue.view.XListViewFooter;
import com.voc.woyaoxue.R;

public class DocsActivity extends Activity {
	private List<Document> list;
	private int folderId;
	private ContentView contentView;
	private MyAdapter adapter;
	private ServiceConnection conn;
	private MyBinder myBinder;
	private Database database;
	private XListView listview;
	private View cpb_download;
	private TextView tv_folder, tv_down;
	protected int pageSize = 15;
	private int levelId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentView = new ContentView(this) {

			@Override
			public View onCreateSuccessView() {
				View view = View.inflate(DocsActivity.this, R.layout.activity_docs, null);
				tv_folder = (TextView) view.findViewById(R.id.tv_folder);
				tv_down = (TextView) view.findViewById(R.id.tv_down);
				cpb_download = view.findViewById(R.id.cpb_download);
				listview = (XListView) view.findViewById(R.id.listview);
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
		folderId = intent.getIntExtra("FolderId", 0);
		String folderName = intent.getStringExtra("FolderName");
		tv_folder.setText(folderName);

		list = new ArrayList<Document>();
		adapter = new MyAdapter(list);

		conn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i("onServiceConnected");
				myBinder = (MyBinder) service;
				myBinder.getDownloadManager().addObserver(adapter);
			}
		};
		bindService(new Intent(this, DownloadService.class), conn, Service.BIND_AUTO_CREATE);

		listview.setAdapter(adapter);
		// 其他设置
		listview.setPullDownEnable(false);
		listview.setPullupEnable(true);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent play = new Intent(DocsActivity.this, PlayActivity.class);
				play.putExtra("Id", list.get(position - 1).Id);
				startActivity(play);
			}
		});
		listview.setXListViewListener(new IXListViewListener() {

			@Override
			public void onRefresh() {
			}

			@Override
			public void onLoadMore() {
				loadData();
			}
		});

		cpb_download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 添加到下载列表中,添加到数据库中,提示数据适配器更新
				for (Document i : list) {
					if (!database.docsExists(i.Id)) {
						download(i);
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

		getActionBar().setDisplayHomeAsUpEnabled(true);
		loadData();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (database != null) {
			database.closeConnection();
			database = null;
		}
		Log.i("database=" + database);

		// 移除观察者,让下载服务后台自行下载
		myBinder.getDownloadManager().deleteObserver(adapter);

		// 移除服务绑定,避免内存泄漏
		unbindService(conn);
	}

	private void download(Document doc) {
		DownloadInfo info = new DownloadInfo();
		info.Id = doc.Id;
		info.Title = doc.Title;
		info.SoundPath = doc.SoundPath;
		myBinder.getDownloadManager().enqueue(info);

		doc.LevelId = levelId;
		doc.FolderId = folderId;
		database.docsInsert(doc);
	}

	private void loadData() {
		String url = NetworkUtil.getDocs(folderId + "", list.size() + "", pageSize + "");
		final UrlCache cache = database.cacheSelectByUrl(url);

		if (cache == null || (System.currentTimeMillis() - cache.UpdateAt > 600000)) {
			Log.i("使用网络:" + url);
			new HttpUtils().send(HttpMethod.GET, NetworkUtil.getDocs(folderId + "", list.size() + "", pageSize + ""),
					new RequestCallBack<String>() {

						@Override
						public void onSuccess(ResponseInfo<String> responseInfo) {
							showData(responseInfo.result);
							UrlCache urlCache = new UrlCache(this.getRequestUrl(), responseInfo.result,
									System.currentTimeMillis());
							if (database != null) {
								database.cacheInsertOrUpdate(urlCache);
							}
						}

						@Override
						public void onFailure(HttpException error, String msg) {
							if (cache != null) {
								showData(cache.Json);
							} else {
								contentView.showView(ViewState.FAILURE);
							}
						}
					});
		} else {
			Log.i("使用缓存:" + url);
			showData(cache.Json);
		}

	}

	private void showData(String result) {
		List<Document> json = new Gson().fromJson(result, new TypeToken<List<Document>>() {
		}.getType());
		if (json.size() > 0) {
			list.addAll(json);
			adapter.notifyDataSetChanged();
			contentView.showView(ViewState.SUCCESS);
		} else {
			contentView.showView(list.size() > 0 ? ViewState.SUCCESS : ViewState.EMPTY);
		}

		listview.stopLoadMore(json.size() < pageSize ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);
	}

	private class MyAdapter extends BaseAdapter<Document> implements Observer {

		public MyAdapter(List<Document> list) {
			super(list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Document item = getItem(position);
			if (convertView == null) {
				convertView = View.inflate(DocsActivity.this, R.layout.listitem_docs, null);
				ViewHolder holder = new ViewHolder();
				holder.tv_title_one = (TextView) convertView.findViewById(R.id.tv_title_one);
				holder.tv_title_two = (TextView) convertView.findViewById(R.id.tv_title_two);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				
				holder.cpb = (CircularProgressBar) convertView.findViewById(R.id.cpb);
				convertView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.tv_title_one.setText(item.Title);
			holder.tv_title_two.setText(item.TitleTwo);
			holder.tv_date.setText(item.DateString);
			holder.tv_size.setText(Formatter.formatFileSize(DocsActivity.this, item.Length));
			holder.tv_time.setText(item.LengthString);

			DownloadInfo ssss = database.docsSelectById(item.Id);
			if (ssss != null) {
				if (ssss.IsDownload == 1) {
					holder.cpb.setMax(100);
					holder.cpb.setProgress(100);
					holder.cpb.setBackgroundResource(R.drawable.download_finish);
				} else {
					DownloadInfo info = myBinder.getDownloadManager().get(item.Id);
					holder.cpb.setMax((int) info.Total);
					holder.cpb.setProgress((int) info.Current);
					holder.cpb.setBackgroundResource(R.drawable.download_begin);
				}
			} else {
				holder.cpb.setMax(100);
				holder.cpb.setProgress(0);
				holder.cpb.setBackgroundResource(R.drawable.download_notyet);
			}

			holder.cpb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (database.docsExists(item.Id)) {
						Toast.makeText(DocsActivity.this, "请不要重复下载", Toast.LENGTH_SHORT).show();
					} else {
						download(item);
						v.setBackgroundResource(R.drawable.download_begin);
					}
				}
			});

			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			Log.i("update");
			notifyDataSetChanged();
		}
	}

	private class ViewHolder {
		public TextView tv_title_one;
		public TextView tv_title_two;
		public TextView tv_date;
		public TextView tv_size;
		public TextView tv_time;
		public CircularProgressBar cpb;
	}

}
