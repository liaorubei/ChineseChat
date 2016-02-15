package com.newclass.woyaoxue.fragment;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.newclass.woyaoxue.activity.DocsActivity;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.UrlCache;
import com.newclass.woyaoxue.database.Database;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentView.ViewState;
import com.voc.woyaoxue.R;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class FolderFragment extends Fragment {
    private static final String TAG = "FolderFragment";
    private MyAdapter adapter;
    private ContentView contentView;
    private Database database;
    private List<Folder> list;
    private ListView listview;
    private int mLevelId = -1;

/*    public FolderFragment() {
    }

    public FolderFragment(int levelId) {
        this.mLevelId = levelId;
    }*/

    private void loadData() {
        String url = NetworkUtil.getFolders(mLevelId);
        final UrlCache cache = database.cacheSelectByUrl(url);
        long expire = 1000000L;// 600000L;
        if (cache == null || (System.currentTimeMillis() - cache.UpdateAt > expire)) {
            Log.i("请求网络:" + url);
            new HttpUtils().send(HttpMethod.GET, url, new RequestCallBack<String>() {

                @Override
                public void onFailure(HttpException error, String msg) {
                    if (cache != null) {
                        subShowData(cache.Json);
                    } else {
                        contentView.showView(ViewState.FAILURE);
                    }
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    // 填充数据
                    subShowData(responseInfo.result);

                    // 缓存文件夹
                    List<Folder> folders = new Gson().fromJson(responseInfo.result, new TypeToken<List<Folder>>() {
                    }.getType());
                    for (Folder folder : folders) {
                        if (!database.folderExists(folder.Id)) {
                            database.folderInsert(folder);
                        }
                    }

                    // 缓存数据
                    UrlCache urlCache = new UrlCache(this.getRequestUrl(), responseInfo.result, System.currentTimeMillis());
                    database.cacheInsertOrUpdate(urlCache);
                }
            });
        } else {
            Log.i("使用缓存:" + url);
            subShowData(cache.Json);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.i(TAG, "onCreateView: ");
        if (bundle != null) {
            this.mLevelId = bundle.getInt("LevelId");
        }
        if (database == null) {
            database = new Database(getActivity());
        }
        if (contentView == null) {
            contentView = new ContentView(getActivity()) {

                @Override
                public View onCreateSuccessView() {
                    View view = View.inflate(getActivity(), R.layout.fragment_folder, null);
                    listview = (ListView) view.findViewById(R.id.listview);
                    list = new ArrayList<Folder>();
                    adapter = new MyAdapter(list);
                    listview.setAdapter(adapter);
                    listview.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Folder folder = list.get(position);

                            Intent intent = new Intent(getActivity(), DocsActivity.class);
                            intent.putExtra("FolderId", folder.Id);
                            intent.putExtra("FolderName", folder.Name);
                            startActivity(intent);
                        }
                    });
                    return view;
                }

                @Override
                public void initData() {

                }
            };
        }

        return contentView;

    }

    @Override
    public void onDestroy() {
        if (database != null) {
         //   database.closeConnection();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {

        super.onResume();
        loadData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("LevelId", this.mLevelId);
    }

    public void setLevelId(int id) {
        this.mLevelId = id;
    }

    private void subShowData(String json) {
        List<Folder> folders = new Gson().fromJson(json, new TypeToken<List<Folder>>() {
        }.getType());
        if (folders.size() > 0) {
            list.clear();// 因为是在onCreateView中显示数据,有可能会显示多次,然后数据会叠加重复,所以要清除之前的数据
            list.addAll(folders);
            contentView.showView(ViewState.SUCCESS);
        } else {
            contentView.showView(ViewState.EMPTY);
        }
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends BaseAdapter<Folder> {

        public MyAdapter(List<Folder> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Folder item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_folder, null);
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
    }

    private class ViewHolder {
        public TextView tv_counts;
        public TextView tv_folder;
    }
}
