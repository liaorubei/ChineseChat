package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.UrlCache;
import com.hanwen.chinesechat.service.DownloadService;
import com.hanwen.chinesechat.service.DownloadService.MyBinder;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.CircularProgressBar;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

//文档显示界面,非下载
public class ActivityDocsTodo extends Activity implements OnClickListener {
    public static final String TAG = "ActivityDocsTodo";
    private List<Document> list;
    private List<ViewHelper> data;
    private List<Document> down;
    private MyAdapter adapter;
    private ServiceConnection conn;
    private MyBinder myBinder;

    private TextView tv_folder;
    private int take = 50;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Folder folder;
    private ListView listview;
    private ImageView iv_menu;
    private ImageView iv_delete;
    private ImageView cb_check_all;
    private boolean showDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docstodo);

        down = new ArrayList<>();

        // 取得传递过来的数据
        Intent intent = getIntent();

        folder = new Folder();
        folder.Id = intent.getIntExtra("id", 0);
        folder.Name = intent.getStringExtra("name");
        folder.TargetId = intent.getIntExtra("targetId", 0);
        showDate = intent.getBooleanExtra("showDate", false);

        initView();

        initData();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 移除观察者,让下载服务后台自行下载
        myBinder.getDownloadManager().deleteObserver(adapter);

        // 移除服务绑定,避免内存泄漏
        unbindService(conn);
    }

    public static void start(Context context, Folder folder, boolean showDate) {
        //当传递Parcelable类型数据时，最好拆开来传递，而不是直接使用Intent，因为在某类华为和三星手机中Activity在直接传递Parcelable时会出现某些“迷之异常”
        Intent intent = new Intent(context, ActivityDocsTodo.class);
        intent.putExtra("id", folder.Id);
        intent.putExtra("name", folder.Name);
        intent.putExtra("targetId", folder.TargetId);
        intent.putExtra("showDate", showDate);
        context.startActivity(intent);
    }

    private void initData() {
        tv_folder.setText(folder.Name);

        //防异常处理
        int id = -1;
        if (ChineseChat.CurrentUser != null) {
            id = ChineseChat.CurrentUser.Id;
        }

        //请求课文列表时，如果有TargetId，则优先显示，因为其本身只是一个指向作用，有一定可能其本身没有内容
        String url = NetworkUtil.documentGetListByFolderId + String.format("?folderId=%1$d&userId=%2$d", folder.TargetId > 0 ? folder.TargetId : folder.Id, id);
        Log.i(TAG, "initData: " + url);

        UrlCache cache = null;// ChineseChat.database().cacheSelectByUrl(url);
        if (cache == null) {
            HttpUtil.post(url, null, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + this.getRequestUrl());

                    lastPost(responseInfo.result);

                    UrlCache urlCache = new UrlCache();
                    urlCache.Url = this.getRequestUrl();
                    urlCache.Json = responseInfo.result;
                    urlCache.UpdateAt = System.currentTimeMillis();
                    ChineseChat.database().cacheInsertOrUpdate(urlCache);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: error=" + error.getMessage() + " msg=" + msg);
                    CommonUtil.toast(getString(R.string.network_error));
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
                        ChineseChat.database().cacheInsertOrUpdate(urlCache);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        CommonUtil.toast(getString(R.string.network_error));
                    }
                });
            } else {
                lastPost(cache.Json);
            }
        }
    }

    private void lastPost(String json) {
        Response<List<Document>> docs = gson.fromJson(json, new TypeToken<Response<List<Document>>>() {}.getType());
        if (docs.code == 200) {
            list.clear();
            for (Document d : docs.info) {
                list.add(d);
                data.add(new ViewHelper());
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        //标题
        findViewById(R.id.iv_home).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);

        iv_delete = (ImageView) findViewById(R.id.iv_delete);
        iv_delete.setOnClickListener(this);

        tv_folder = (TextView) findViewById(R.id.tv_folder);
        cb_check_all = (ImageView) findViewById(R.id.cb_check_all);
        cb_check_all.setOnClickListener(this);

        listview = (ListView) findViewById(android.R.id.list);

        //初始化变量
        list = new ArrayList<>();
        data = new ArrayList<>();
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
    }

    private void download(Document doc) {
        if (TextUtils.isEmpty(doc.SoundPath)) {
            CommonUtil.toast(R.string.ActivityDocsTodo_soundPath_error);
            return;
        }
        DownloadInfo info = new DownloadInfo();
        info.Id = doc.Id;
        info.Title = doc.Title;
        info.SoundPath = doc.SoundPath;
        myBinder.getDownloadManager().enqueue(info);

        Log.i(TAG, "download: " + doc);
        doc.FolderId = folder.Id;
        ChineseChat.database().docsInsert(doc);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                if (iv_menu.isSelected()) {
                    for (ViewHelper helper : data) {
                        helper.checkBoxShow = false;
                        helper.isSelected = false;
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    for (ViewHelper helper : data) {
                        helper.checkBoxShow = true;
                        helper.isSelected = false;
                    }
                    adapter.notifyDataSetChanged();
                }

                iv_menu.setSelected(!iv_menu.isSelected());
                iv_delete.setSelected(false);
                cb_check_all.setSelected(false);
                iv_delete.setVisibility(iv_menu.isSelected() ? View.VISIBLE : View.INVISIBLE);
                cb_check_all.setVisibility(iv_menu.isSelected() ? View.VISIBLE : View.INVISIBLE);
                break;
            case R.id.cb_check_all:
                cb_check_all.setSelected(!cb_check_all.isSelected());
                for (ViewHelper helper : data) {
                    helper.isSelected = cb_check_all.isSelected();
                }
                adapter.notifyDataSetChanged();
                initState();
                break;
            case R.id.iv_delete:
                if (iv_delete.isSelected()) {
                    for (Document d : down) {
                        download(d);
                    }

                    for (ViewHelper h : data) {
                        h.isSelected = false;
                    }
                    iv_delete.setSelected(false);
                    cb_check_all.setSelected(false);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private class MyAdapter extends BaseAdapter<Document> implements Observer {

        public MyAdapter(List<Document> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Document item = getItem(position);
            final ViewHelper dddd = data.get(position);
            if (convertView == null) {
                convertView = View.inflate(ActivityDocsTodo.this, R.layout.listitem_docs, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.tv_title_one.setText(item.TitleCn);
            holder.tv_title_two.setText(item.TitleEn);
            holder.tv_title_two.setVisibility(TextUtils.isEmpty(item.TitleEn) ? View.GONE : View.VISIBLE);
            holder.tv_title_sub_cn.setText(item.TitleSubCn);
            holder.tv_title_sub_cn.setVisibility(item.Category == 2 ? View.VISIBLE : View.GONE);
            if (item.Date != null) {
                holder.tv_date.setText(sdf.format(item.Date));
            }
            holder.tv_date.setVisibility(showDate ? View.VISIBLE : View.GONE);
            holder.tv_size.setText(FileUtil.formatFileSize(item.Length, FileUtil.SizeUnit.MB));
            holder.tv_time.setText(item.LengthString);

            DownloadInfo ssss = ChineseChat.database().docsSelectById(item.Id);
            if (ssss != null) {
                holder.rl_ctrl.setVisibility(View.VISIBLE);
                if (ssss.IsDownload == 1) {
                    holder.iv_over.setVisibility(View.VISIBLE);
                    holder.iv_down.setVisibility(View.INVISIBLE);
                    holder.pb_down.setVisibility(View.INVISIBLE);
                } else {
                    holder.iv_over.setVisibility(View.INVISIBLE);
                    holder.iv_down.setVisibility(View.INVISIBLE);

                    holder.pb_down.setVisibility(View.VISIBLE);
                    DownloadInfo info = myBinder.getDownloadManager().get(item.Id);
                    holder.pb_down.setMax((int) info.Total);
                    holder.pb_down.setProgress((int) info.Current);
                }
            } else {
                holder.pb_down.setVisibility(View.INVISIBLE);
                holder.iv_over.setVisibility(View.INVISIBLE);
                if (dddd.checkBoxShow) {
                    holder.iv_down.setVisibility(View.VISIBLE);
                    holder.rl_ctrl.setVisibility(View.VISIBLE);
                } else {
                    holder.rl_ctrl.setVisibility(View.GONE);
                }
                holder.iv_down.setSelected(dddd.isSelected);
            }

            holder.iv_down.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    dddd.isSelected = v.isSelected();
                    initState();
                }
            });



            /*
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
            });*/

            return convertView;
        }

        @Override
        public void update(Observable observable, Object data) {
            Log.i("update");
            notifyDataSetChanged();
        }
    }

    private void initState() {
        //选中,并且没有下载过的数量
        int check = 0;
        down.clear();
        for (int i = 0; i < list.size(); i++) {
            Document document = list.get(i);
            ViewHelper helper = data.get(i);
            if (ChineseChat.database().docsSelectById(document.Id) == null && helper.isSelected) {
                down.add(document);
            }

            if (ChineseChat.database().docsSelectById(document.Id) == null) {
                check++;
            }
        }

        iv_delete.setSelected(down.size() > 0);
        iv_delete.setVisibility(down.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        cb_check_all.setSelected(down.size() == check);
    }

    private class ViewHolder {
        public TextView tv_title_one;
        public TextView tv_title_two;
        public TextView tv_title_sub_cn;
        public TextView tv_date;
        public TextView tv_size;
        public TextView tv_time;
        public View rl_ctrl;
        public View iv_over;
        public View iv_down;
        public CircularProgressBar pb_down;

        public ViewHolder(View convertView) {
            this.tv_title_one = (TextView) convertView.findViewById(R.id.tv_title_one);
            this.tv_title_two = (TextView) convertView.findViewById(R.id.tv_title_two);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            this.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

            this.rl_ctrl = convertView.findViewById(R.id.rl_ctrl);
            this.iv_over = convertView.findViewById(R.id.iv_over);
            this.iv_down = convertView.findViewById(R.id.iv_down);
            this.pb_down = (CircularProgressBar) convertView.findViewById(R.id.pb_down);
            this.tv_title_sub_cn = (TextView) convertView.findViewById(R.id.tv_title_sub_cn);
        }
    }

    private class ViewHelper {

        public boolean checkBoxShow;
        public boolean isSelected;
    }
}
