package com.newclass.woyaoxue.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Answer;
import com.newclass.woyaoxue.bean.Answer.People;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContactActivity extends Activity implements OnClickListener {
    private ListView listview;
    private List<People> list;
    private MyAdapter adapter;
    private String accid;

    private Button bt_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        accid = getIntent().getStringExtra("accid");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        initView();
        listview = (ListView) findViewById(R.id.listview);
        list = new ArrayList<People>();
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.get(position);
                Intent intent = new Intent(ContactActivity.this, null);
                intent.putExtra("target", list.get(position).AccId);
                startActivity(intent);
            }
        });

        new HttpUtils().send(HttpMethod.POST, NetworkUtil.userSelect, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Answer answer = new Gson().fromJson(responseInfo.result, Answer.class);

                if (200 == answer.code) {
                    List<People> others = answer.info.others;
                    for (People people : others) {
                        if (!people.AccId.equals(accid)) {
                            list.add(people);
                        } else {
                            Log.i("logi", "accid=" + accid);
                        }
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
        bt_call = (Button) findViewById(R.id.bt_call);

        bt_call.setOnClickListener(this);
    }

    private class MyAdapter extends BaseAdapter<People> {

        public MyAdapter(List<People> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            People item = getItem(position);
            TextView textView = (TextView) View.inflate(ContactActivity.this, R.layout.listitem_listactivity, null);
            textView.setText(item.NickName);
            return textView;
        }
    }

    private void call(String teacherAccid) {


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_call:

                String targetAccid = "bf09f7dd02e549f4a16af0cf8e9a5701";
                call(targetAccid);

                // getCallTarget();

                break;

            default:
                break;
        }
    }

    private void getCallTarget() {
        RequestParams para = new RequestParams();
        new HttpUtils().send(HttpMethod.POST, NetworkUtil.studentCall, para, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String teacherAccid = "";
                call(teacherAccid);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                CommonUtil.toast(ContactActivity.this, "网络异常,请重试");
            }
        });
    }
}
