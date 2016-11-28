package com.hanwen.chinesechat.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FragmentNews";
    private List<Document> data = new ArrayList<>();
    private Integer take = 25;
    private ListView swipe_target;
    private SwipeToLoadLayout swipe;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentNews.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentNews newInstance(String param1, String param2) {
        FragmentNews fragment = new FragmentNews();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        public ViewHolder(View convertView) {
            this.tv_title_cn = (TextView) convertView.findViewById(R.id.tv_title_cn);
            this.tv_title_en = (TextView) convertView.findViewById(R.id.tv_title_en);
            this.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            this.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            this.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            this.iv_cover = (ImageView) convertView.findViewById(R.id.iv_cover);
            convertView.setTag(this);
        }
    }
}
