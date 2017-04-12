package com.hanwen.chinesechat.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityPlay;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.DownloadInfo;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.service.DownloadService;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.CircularProgressBar;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 主页面Listen模块的News界面，新闻显示中文/英文标题，并且要求显示文件大小，时长，发布日期
 */
public class FragmentNews extends Fragment implements OnLoadMoreListener, OnRefreshListener {
    private static final String TAG = "FragmentNews";
    private List<Document> data = new ArrayList<>();
    private Integer take = 25;
    private ListView swipe_target;
    private SwipeToLoadLayout swipe;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ServiceConnection conn;
    private DownloadService.MyBinder binder;
    private BroadcastReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        Intent service = new Intent(getContext(), DownloadService.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (DownloadService.MyBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        getContext().bindService(service, conn, Context.BIND_AUTO_CREATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int documentId = intent.getIntExtra("documentId", 0);
                int current = intent.getIntExtra("current", 0);

                Log.i(TAG, "onReceive: " + intent.getAction() + ",document=" + documentId + ",current=" + current);
                int firstVisiblePosition = swipe_target.getFirstVisiblePosition();
                int lastVisiblePosition = swipe_target.getLastVisiblePosition();
                for (int i = 0; i < data.size(); i++) {
                    Document document = data.get(i);
                    if (document.Id == documentId && firstVisiblePosition <= i && i <= lastVisiblePosition) {
                        View childAt = swipe_target.getChildAt(i - firstVisiblePosition);
                        ViewHolder holder = (ViewHolder) childAt.getTag();
                        holder.iv_down.setVisibility(current == 100 ? View.VISIBLE : View.INVISIBLE);
                        holder.pb_down.setVisibility(current == 100 ? View.INVISIBLE : View.VISIBLE);
                        holder.pb_down.setProgress(current);
                    }
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipe = (SwipeToLoadLayout) view.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);
        swipe.setOnLoadMoreListener(this);
        swipe_target = (ListView) view.findViewById(R.id.swipe_target);
        swipe_target.setAdapter(new BaseAdapter<Document>(data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Document item = getItem(position);

                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.listitem_news, null);
                    new ViewHolder(convertView);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.tv_title_cn.setText(item.TitleCn);
                holder.tv_title_en.setText(item.TitleEn);
                holder.tv_size.setText(FileUtil.formatFileSize(item.Length, FileUtil.SizeUnit.MB));
                holder.tv_time.setText(item.LengthString);
                holder.tv_date.setText(sdf.format(item.AuditDate));

                DownloadInfo downloadInfo = ChineseChat.database().docsSelectById(item.Id);

                holder.iv_down.setVisibility(downloadInfo != null && downloadInfo.IsDownload == 1 ? View.VISIBLE : View.INVISIBLE);
                holder.pb_down.setVisibility(downloadInfo == null ? View.INVISIBLE : (downloadInfo.IsDownload == 1 ? View.INVISIBLE : View.VISIBLE));
                holder.bt_down.setVisibility(downloadInfo == null ? View.VISIBLE : View.INVISIBLE);
                holder.bt_down.setTag(item);
                holder.bt_down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Document tag = (Document) v.getTag();
                        Log.i(TAG, "onClick: " + tag);
                        DownloadInfo info = new DownloadInfo();
                        info.SoundPath = tag.SoundPath;
                        info.Id = tag.Id;
                        info.IsDownload = 0;
                        info.Title = tag.TitleCn;

                        //添加到下载队列
                        binder.getDownloadManager().enqueue(info);
                        //添加到数据库
                        ChineseChat.database().docsInsert(tag);
                        //去掉下载按钮
                        v.setVisibility(View.INVISIBLE);
                    }
                });


                new BitmapUtils(getContext(), getContext().getCacheDir().getAbsolutePath()).display(holder.iv_cover, NetworkUtil.getFullPath(item.Cover), new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                        container.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
                        container.setImageResource(ChineseChat.isStudent() ? R.drawable.ic_launcher_student : R.drawable.ic_launcher_teacher);
                    }
                });
                return convertView;
            }
        });
        swipe_target.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document = (Document) parent.getAdapter().getItem(position);
                Intent intent = new Intent(getContext(), ActivityPlay.class);
                intent.putExtra("Id", document.Id);
                intent.putExtra("mode", "Online");
                startActivity(intent);
            }
        });

        Parameters params = new Parameters();
        params.add("levelId", 6);
        params.add("skip", 0);
        params.add("take", take);
        HttpUtil.post(NetworkUtil.documentGetListByLevelId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<Document>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                List<Document> info = resp.info;

                if (info != null) {
                    for (Document d : info) {
                        data.add(d);
                    }
                    ((BaseAdapter) swipe_target.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("音频下载");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        getContext().unbindService(conn);
    }

    @Override
    public void onLoadMore() {
        Parameters params = new Parameters();
        params.add("levelId", 6);
        params.add("skip", data.size());
        params.add("take", take);
        HttpUtil.post(NetworkUtil.documentGetListByLevelId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                swipe.setLoadingMore(false);
                //Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<Document>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                List<Document> info = resp.info;

                if (info != null) {
                    for (Document d : info) {
                        d.LevelId = 6;
                        data.add(d);
                    }
                }
                ((BaseAdapter) swipe_target.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                swipe.setLoadingMore(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        Parameters params = new Parameters();
        params.add("levelId", 6);
        params.add("skip", 0);
        params.add("take", take);
        HttpUtil.post(NetworkUtil.documentGetListByLevelId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //Log.i(TAG, "onSuccess: " + responseInfo.result);
                swipe.setRefreshing(false);
                Response<List<Document>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                List<Document> info = resp.info;
                data.clear();
                ((BaseAdapter) swipe_target.getAdapter()).notifyDataSetChanged();
                if (info != null) {
                    for (Document d : info) {
                        data.add(d);
                    }
                }
                ((BaseAdapter) swipe_target.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                swipe.setRefreshing(false);
            }
        });
    }

    private class ViewHolder {
        private TextView tv_title_cn;
        private TextView tv_title_en;
        private TextView tv_size;
        private TextView tv_time;
        public TextView tv_date;
        public ImageView iv_cover;
        public View bt_down;
        public CircularProgressBar pb_down;
        public View iv_down;

        public ViewHolder(View convertView) {
            this.tv_title_cn = (TextView) convertView.findViewById(R.id.tv_title_cn);
            this.tv_title_en = (TextView) convertView.findViewById(R.id.tv_title_en);
            this.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            this.iv_cover = (ImageView) convertView.findViewById(R.id.iv_cover);
            this.bt_down = convertView.findViewById(R.id.bt_down);
            this.pb_down = (CircularProgressBar) convertView.findViewById(R.id.pb_down);
            this.iv_down = convertView.findViewById(R.id.iv_down);
            convertView.setTag(this);
        }
    }
}
