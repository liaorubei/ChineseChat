package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

public class ActivityFolder extends Activity {

    private static final String TAG = "ActivityFolder";
    private TextView tv_name;
    private ListView listView;
    private List<Folder> data;
    private boolean showDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        Folder folder = intent.getParcelableExtra("folder");
        showDate = intent.getBooleanExtra("showDate", false);
        tv_name.setText(folder.Name);
        data = new ArrayList<>();
        listView.setAdapter(new BaseAdapter<Folder>(data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Folder item = getItem(position);
                View inflate = getLayoutInflater().inflate(R.layout.listitem_folder, null);
                TextView tv_folder = (TextView) inflate.findViewById(R.id.tv_folder);
                TextView tv_counts = (TextView) inflate.findViewById(R.id.tv_counts);
                tv_folder.setText(item.Name);
                tv_counts.setText(String.format("课程：%1$d", item.DocsCount));
                return inflate;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder item = (Folder) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), ActivityDocsTodo.class);
                i.putExtra("folder", item);
                i.putExtra("showDate", showDate);
                startActivity(i);
            }
        });

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("folderId", folder.Id);
        HttpUtil.post(NetworkUtil.folderGetChildListByParentId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                for (Folder f : resp.info) {
                    data.add(f);
                }
                android.widget.BaseAdapter adapter = (android.widget.BaseAdapter) listView.getAdapter();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.e(TAG, "onFailure: " + msg);
            }
        });
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("Folder List");
        tv_name = (TextView) findViewById(R.id.tv_name);
        listView = (ListView) findViewById(R.id.listView);
    }

}
