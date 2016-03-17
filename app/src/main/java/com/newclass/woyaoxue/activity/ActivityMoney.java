package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;


import com.newclass.woyaoxue.base.BaseAdapter;

import com.voc.woyaoxue.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//充值界面
public class ActivityMoney extends Activity implements View.OnClickListener {

    private static final String TAG = "MoneyActivity";
    private LinearLayout ll_paypal, ll_alipay;
    private RadioButton rb_paypal, rb_alipay;
    private ListView listview;
    private List<Pay> list;
    private BaseAdapter<Pay> adapter;
    private Dialog paymentDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);

        initView();
        initData();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initData() {
        //是否显示充值的内容和按钮

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
                if (paymentDialog == null) {
                    paymentDialog = new Dialog(ActivityMoney.this);
                    paymentDialog.setTitle("请选择你的支付方式");
                    View inflate = getLayoutInflater().inflate(R.layout.dialog_payment, null);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    paymentDialog.setContentView(inflate, params);
                    paymentDialog.getWindow().setGravity(Gravity.BOTTOM);
                    paymentDialog.setCanceledOnTouchOutside(false);
                }
                paymentDialog.show();
            }
        });
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        rb_paypal = (RadioButton) findViewById(R.id.rb_paypal);
        rb_alipay = (RadioButton) findViewById(R.id.rb_alipay);
        ll_paypal = (LinearLayout) findViewById(R.id.ll_paypal);
        ll_alipay = (LinearLayout) findViewById(R.id.ll_alipay);
        ll_paypal.setOnClickListener(this);
        ll_alipay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ll_paypal:
                rb_paypal.setChecked(true);
                rb_alipay.setChecked(false);
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_pricelist);
                dialog.setTitle("请选择充值金额");
                dialog.show();
                break;
            case R.id.ll_alipay:
                rb_paypal.setChecked(false);
                rb_alipay.setChecked(true);
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
            View inflate = View.inflate(ActivityMoney.this, R.layout.listitem_records, null);
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

