package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.util.Log;

import com.voc.woyaoxue.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class MoneyActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MoneyActivity";
    private RelativeLayout rl_ten;
    private ListView listview;
    private List<Pay> list;
    private BaseAdapter<Pay> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);

        initView();
        initData();
    }

    private void initData() {
        list = new ArrayList<>();
        list.add(new Pay("1000学币", new BigDecimal(10), new BigDecimal(65)));
        list.add(new Pay("2000学币", new BigDecimal(20), new BigDecimal(130)));
        list.add(new Pay("3000学币", new BigDecimal(30), new BigDecimal(195)));
        list.add(new Pay("5000学币", new BigDecimal(50), new BigDecimal(325)));
        list.add(new Pay("10000学币", new BigDecimal(100), new BigDecimal(650)));
        adapter = new MyAdapter(list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pay pay = list.get(position);
                PaymentActivity.start(MoneyActivity.this, pay.subject, pay.usd, pay.cny);
            }
        });
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        rl_ten = (RelativeLayout) findViewById(R.id.rl_ten);

        rl_ten.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_ten:
                startActivity(new Intent(MoneyActivity.this, PaymentActivity.class));
                break;

        }
    }

    private class MyAdapter extends BaseAdapter<Pay> {
        public MyAdapter(List<Pay> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Pay item = getItem(position);
            View inflate = View.inflate(MoneyActivity.this, R.layout.listitem_money, null);
            TextView tv_subject = (TextView) inflate.findViewById(R.id.tv_subject);
            TextView tv_price = (TextView) inflate.findViewById(R.id.tv_price);

            tv_subject.setText(item.subject);
            tv_price.setText("CNY:" + item.cny + "/USD:" + item.usd);
            return inflate;
        }
    }


    private class Pay {
        String subject;
        BigDecimal usd;
        BigDecimal cny;

        public Pay(String subject, BigDecimal us, BigDecimal cn) {
            this.subject = subject;
            this.usd = us;
            this.cny = cn;
        }
    }

}

