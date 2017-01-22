package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

/*
 * 该类为显示课程合集所用，如果有下级文件夹或者合集，或者课本，则点击后显示下级内容（即KidsCount>0），如果没有，则直接打开文档列表或课文列表界面
 * 同时，如果该合集或文件夹有指向Id，则优先显示指向Id所包含的内容，即TargetId>0
 */
public class ActivityFolder extends Activity {

    private static final String TAG = "ActivityFolder";
    private ListView listView;
    private List<Folder> data;
    private boolean showDate;
    private TextView tv_title;

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
        tv_title.setText(folder.Name);
        data = new ArrayList<>();
        listView.setAdapter(new BaseAdapter<Folder>(data) {
            @Override
            public View getView(int position, View inflate, ViewGroup parent) {
                Folder item = getItem(position);
                if (inflate == null) {
                    inflate = getLayoutInflater().inflate(R.layout.listitem_activity_folder, null);
                }
                TextView tv_folder = (TextView) inflate.findViewById(R.id.tv_name);
                TextView tv_counts = (TextView) inflate.findViewById(R.id.tv_name_en);
                tv_folder.setText(item.Name);
                tv_counts.setText(item.NameEn);

                ImageView iv_cover = (ImageView) inflate.findViewById(R.id.iv_cover);
                CommonUtil.showIcon(getApplicationContext(), iv_cover, item.Cover);
                return inflate;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder item = (Folder) parent.getItemAtPosition(position);
                if (item.KidsCount > 0) {
                    Intent kidsIntent = new Intent(getApplicationContext(), ActivityFolder.class);
                    kidsIntent.putExtra("folder", item);
                    kidsIntent.putExtra("showDate", showDate);
                    startActivity(kidsIntent);
                } else {
                    Intent docsIntent = new Intent(getApplicationContext(), ActivityDocsTodo.class);
                    docsIntent.putExtra("folder", item);
                    docsIntent.putExtra("showDate", showDate);
                    startActivity(docsIntent);
                }
            }
        });

        //请求子级文件夹或者课本，如果TargetId>0则优先显示，因为有TargetId的合集只是一个指向作用，自己本身按要求是不添加内容或子集的
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("userId", ChineseChat.CurrentUser.Id);
        params.add("parentId", folder.TargetId > 0 ? folder.TargetId : folder.Id);
        HttpUtil.post(NetworkUtil.folderGetList, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
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
        tv_title = (TextView) findViewById(R.id.tv_title);
        listView = (ListView) findViewById(R.id.listView);
    }
}
