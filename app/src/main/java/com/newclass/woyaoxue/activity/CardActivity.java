package com.newclass.woyaoxue.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.NimSysNotice;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.Theme;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.view.ContentView;
import com.newclass.woyaoxue.view.ContentViewCard;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class CardActivity extends Activity {

    private static final String KEY_SOURCE_ACCID = "KEY_SOURCE_ACCID";
    private static final String KEY_TARGET_ACCID = "KEY_TARGET_ACCID";
    private static final String TAG = "CardActivity";
    private List<Theme> list = new ArrayList<>();
    private BaseAdapter<Theme> adapter;
    private Gson gson = new Gson();
    private String source_accid;
    private String target_accid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        source_accid = intent.getStringExtra(KEY_SOURCE_ACCID);
        target_accid = intent.getStringExtra(KEY_TARGET_ACCID);


        Log.i(TAG, "initData: ");
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("skip", "" + list.size());
        parameters.add("take", "15");
        HttpUtil.post(NetworkUtil.ThemeSelect, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<Theme>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Theme>>>() {
                }.getType());

                if (resp.code == 200) {
                    for (Theme theme : resp.info) {
                        list.add(theme);
                    }
                    adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    private void initView() {
        GridView gridView = (GridView) findViewById(R.id.gridView);
        adapter = new MyAdapter(list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public static void start(Context context, String source, String target) {
        Intent intent = new Intent(context, CardActivity.class);
        intent.putExtra(KEY_SOURCE_ACCID, source);
        intent.putExtra(KEY_TARGET_ACCID, target);
        context.startActivity(intent);
    }


    private class MyAdapter extends BaseAdapter<Theme> {
        public MyAdapter(List<Theme> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Theme item = getItem(position);
            View inflate = View.inflate(CardActivity.this, R.layout.griditem_card, null);
            ImageView iv_card = (ImageView) inflate.findViewById(R.id.iv_card);
            iv_card.setVisibility(View.VISIBLE);
            iv_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.INVISIBLE);
                    // 构造自定义通知，指定接收者
                    CustomNotification notification = new CustomNotification();
                    notification.setSessionId(target_accid);
                    notification.setSessionType(SessionTypeEnum.P2P);

                    NimSysNotice<Theme> i = new NimSysNotice<Theme>();
                    i.info = item;
                    notification.setContent(gson.toJson(i));
                    // 发送自定义通知
                    NIMClient.getService(MsgService.class).sendCustomNotification(notification);
                }
            });
            TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
            TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
            tv_theme.setText(item.Name);
            tv_name.setText(item.Name);
            return inflate;
        }
    }
}
