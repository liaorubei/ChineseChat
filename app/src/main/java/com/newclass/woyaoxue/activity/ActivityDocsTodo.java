package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Document;
import com.newclass.woyaoxue.bean.DownloadInfo;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.service.DownloadService;
import com.newclass.woyaoxue.service.DownloadService.MyBinder;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.CircularProgressBar;
import com.voc.woyaoxue.R;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/*
*
* 文档列表页
*
* */
public class ActivityDocsTodo extends Activity {
    private static final String TAG = "DocsActivity";
    private List<Document> list;
    private MyAdapter adapter;
    private ServiceConnection conn;
    private MyBinder myBinder;
    private Database database;
    private View ib_download;
    private TextView tv_folder;
    private int pageSize = 25;
    private Gson gson = new Gson();
    private SwipeRefreshLayout srl;
    private Folder folder;
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docstodo);

        // 取得传递过来的数据
        Intent intent = getIntent();
        folder = gson.fromJson(intent.getStringExtra("folder"), new TypeToken<Folder>() {
        }.getType());
        if (folder == null) {
            folder = new Folder();
            folder.Id = 50;
        }

        initView();

        initData();

        tv_folder.setText(folder.Name);

        database = new Database(this);

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
    }

    private void initData() {

        String url = NetworkUtil.getDocs(folder.Id + "", 0 + "", pageSize + "");

        UrlCache cache = ChineseChat.getDatabase().cacheSelectByUrl(url);
        if (cache == null) {

            HttpUtil.post(url, null, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    lastPost(responseInfo.result);

                    UrlCache urlCache = new UrlCache();
                    urlCache.Url = this.getRequestUrl();
                    urlCache.Json = responseInfo.result;
                    urlCache.UpdateAt = System.currentTimeMillis();
                    ChineseChat.getDatabase().cacheInsertOrUpdate(urlCache);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    CommonUtil.toast(getString(R.string.network_error));
                    srl.setRefreshing(false);
                }
            });
        } else {
            if (cache.UpdateAt < (System.currentTimeMillis() - 10 * 60 * 1000)) {

                HttpUtil.post(url, null, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        lastPost(responseInfo.result);

                        UrlCache urlCache = new UrlCache();
                        urlCache.Url = this.getRequestUrl();
                        urlCache.Json = responseInfo.result;
                        urlCache.UpdateAt = System.currentTimeMillis();
                        ChineseChat.getDatabase().cacheInsertOrUpdate(urlCache);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(getString(R.string.network_error));
                        srl.setRefreshing(false);
                    }
                });
            } else {
                lastPost(cache.Json);
            }

        }
    }

    private void lastPost(String json) {
        List<Document> docs = gson.fromJson(json, new TypeToken<List<Document>>() {
        }.getType());

        list.clear();
        for (Document d : docs) {
            list.add(d);
        }
        adapter.notifyDataSetChanged();
        srl.setRefreshing(false);
    }

    private void initView() {
        //标题
        findViewById(R.id.iv_home).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //查找
        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        listview = (ListView) findViewById(android.R.id.list);
        ib_download = findViewById(R.id.ib_download);
        tv_folder = (TextView) findViewById(R.id.tv_folder);

        //初始化变量
        list = new ArrayList<>();
        adapter = new MyAdapter(list);

        //设置监听或其它
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ActivityDocsTodo.this, ActivityPlay.class);
                intent.putExtra("Id", list.get(position).Id);
                intent.putExtra("mode", "Online");
                startActivity(intent);
            }
        });

        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });

        ib_download.setOnClickListener(new OnClickListener() {
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

        //加载动画
        try {
            Method setRefreshing = SwipeRefreshLayout.class.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
            setRefreshing.setAccessible(true);
            setRefreshing.invoke(srl, true, true);
        } catch (Exception ex) {

        }
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

        // doc.LevelId = levelId;
        doc.FolderId = folder.Id;
        database.docsInsert(doc);
    }

    private class MyAdapter extends BaseAdapter<Document> implements Observer {

        public MyAdapter(List<Document> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Document item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(ActivityDocsTodo.this, R.layout.listitem_docs, null);
                ViewHolder holder = new ViewHolder();
                convertView.setTag(holder);

                holder.tv_title_one = (TextView) convertView.findViewById(R.id.tv_title_one);
                holder.tv_title_two = (TextView) convertView.findViewById(R.id.tv_title_two);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
                holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                holder.iv_down = (ImageView) convertView.findViewById(R.id.iv_down);
                holder.pb_down = (CircularProgressBar) convertView.findViewById(R.id.pb_down);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_title_one.setText(item.Title);
            holder.tv_title_two.setText(item.TitleTwo);
            holder.tv_date.setText(item.DateString);
            holder.tv_size.setText(Formatter.formatFileSize(ActivityDocsTodo.this, item.Length));
            holder.tv_time.setText(item.LengthString);

            DownloadInfo ssss = database.docsSelectById(item.Id);
            if (ssss != null) {
                if (ssss.IsDownload == 1) {
                    holder.pb_down.setMax(100);
                    holder.pb_down.setProgress(100);
                    holder.pb_down.setVisibility(View.INVISIBLE);

                    holder.iv_down.setVisibility(View.VISIBLE);
                    holder.iv_down.setImageResource(R.drawable.download_finish);
                } else {
                    DownloadInfo info = myBinder.getDownloadManager().get(item.Id);
                    holder.pb_down.setMax((int) info.Total);
                    holder.pb_down.setProgress((int) info.Current);
                    holder.pb_down.setVisibility(View.VISIBLE);
                    holder.iv_down.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.pb_down.setMax(100);
                holder.pb_down.setProgress(0);
                holder.pb_down.setVisibility(View.INVISIBLE);
                holder.iv_down.setImageResource(R.drawable.download_pressed);
            }

            holder.iv_down.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (database.docsExists(item.Id)) {
                        Toast.makeText(ActivityDocsTodo.this, getString(R.string.ActivityDocsTodo_alreadyDownload), Toast.LENGTH_SHORT).show();
                    } else {
                        download(item);
                        holder.pb_down.setMax(100);
                        holder.pb_down.setProgress(0);
                        holder.pb_down.setVisibility(View.VISIBLE);
                        holder.iv_down.setVisibility(View.INVISIBLE);
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
        public ImageView iv_down;
        public CircularProgressBar pb_down;
    }
}
