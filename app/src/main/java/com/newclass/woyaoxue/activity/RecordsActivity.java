package com.newclass.woyaoxue.activity;

import android.os.Bundle;
import android.app.Activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Orders;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends Activity {

    private static final String TAG = "RecordsActivity";
    private ListView listview;
    private MyAdapter myAdapter;
    private List<Orders> list;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initData();
    }

    private void initData() {
        list = new ArrayList<>();
        myAdapter = new MyAdapter(list);
        listview.setAdapter(myAdapter);

        HttpUtil.Parameters p = new HttpUtil.Parameters();
        p.add("username", getSharedPreferences("user", MODE_PRIVATE).getString("username", ""));
        p.add("skip", 0 + "");
        p.add("take", 15 + "");
        HttpUtil.post(NetworkUtil.paymentOrderRecords, p, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        Response<List<Orders>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Orders>>>() {
                        }.getType());

                        if (resp.code == 200) {
                            List<Orders> info = resp.info;
                            for (Orders o : info) {
                                list.add(o);
                            }
                            myAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i(TAG, "onFailure: "+msg+" "+this.getRequestUrl());
                    }
                }

        );
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);

    }


    private class MyAdapter extends BaseAdapter<Orders> {
        public MyAdapter(List<Orders> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Orders item = getItem(position);
            View view = View.inflate(RecordsActivity.this, R.layout.listitem_records, null);
            TextView tv_main = (TextView) view.findViewById(R.id.tv_main);
            tv_main.setText("项目:" + item.Main);
            TextView tv_amount = (TextView) view.findViewById(R.id.tv_amount);
            tv_amount.setText("金额:" + item.Amount + " " + item.Currency);

            TextView tv_state = (TextView) view.findViewById(R.id.tv_state);
            tv_state.setText("状态:" + item.TradeStatus);

            TextView tv_createtime = (TextView) view.findViewById(R.id.tv_createtime);
            tv_createtime.setText("时间:" + item.CreateTime);

            return view;
        }
    }

}
