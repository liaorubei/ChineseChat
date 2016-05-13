package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Orders;
import com.newclass.woyaoxue.bean.PayResult;
import com.newclass.woyaoxue.bean.Product;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.voc.woyaoxue.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//充值界面
public class ActivityPayment extends Activity implements View.OnClickListener {

    private static final String TAG = "MoneyActivity";
    private LinearLayout ll_paypal, ll_alipay;
    private RadioButton rb_paypal, rb_alipay;
    private ListView orderListView;
    private ListView paymentListView;

    private List<Orders> listOrder;
    private List<Pay> listPay;
    private BaseAdapter<Orders> adapterOrder;
    private BaseAdapter<Pay> adapterPay;
    private Dialog paymentDialog;
    private ProgressDialog progressDialog;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private String orderId;

    private static final int SDK_PAY_FLAG = 1;
    private static final String PAYPAL_CLIENT_ID = "ARWTsXI5z88D8wWRIcy8WqR2WfTSpxeHWqL1LLQh15RwYqsfTJx08plA5Lczhm3NmCzZglArvmQ_6Y8h";
    private static PayPalConfiguration paypalConfig = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PAYPAL_CLIENT_ID);
    private static final int REQUEST_CODE_PAYPAL = 50;
    private Pay pay;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);
        initView();
        initData();
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sdf = new SimpleDateFormat("HH:mm:ss   E,dd/MM/yyyy");
    }

    private void initData() {
        //初始化充值记录
        listOrder = new ArrayList<Orders>();
        adapterOrder = new AdapterOrder(listOrder);
        orderListView.setAdapter(adapterOrder);

        //取得充值记录
        String username = ChineseChat.CurrentUser.Username;
        if (!TextUtils.isEmpty(username)) {
            HttpUtil.Parameters p = new HttpUtil.Parameters();
            p.add("username", username);
            p.add("skip", 0);
            p.add("take", 50);
            HttpUtil.post(NetworkUtil.paymentOrderRecords, p, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                    Response<List<Orders>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Orders>>>() {
                    }.getType());

                    listOrder.clear();
                    if (resp.code == 200) {
                        List<Orders> info = resp.info;
                        for (Orders o : info) {
                            listOrder.add(o);
                        }
                    }
                    adapterOrder.notifyDataSetChanged();
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: " + msg);
                    CommonUtil.toast(R.string.network_error);
                }
            });
        }
        //初始化充值对话框
        View view = getLayoutInflater().inflate(R.layout.dialog_payment, null);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
        view.findViewById(R.id.bt_positive).setOnClickListener(this);
        paymentListView = (ListView) view.findViewById(R.id.listview);

        paymentDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);//去标题
        paymentDialog.setContentView(view);
        paymentDialog.setCancelable(true);
        paymentDialog.setCanceledOnTouchOutside(true);
        paymentDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去背景

        //初始化充值金额
        listPay = new ArrayList<>();
        adapterPay = new AdapterPay(listPay);
        paymentListView.setAdapter(adapterPay);
        paymentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < listPay.size(); i++) {
                    listPay.get(i).is_check = i == position;
                }
                adapterPay.notifyDataSetChanged();
            }
        });

        //取得价目表
        HttpUtil.post(NetworkUtil.productSelect, null, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<List<Product>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Product>>>() {
                }.getType());
                listPay.clear();
                if (resp.code == 200) {
                    for (Product p : resp.info) {
                        listPay.add(new Pay(p.Coin, p.USD, p.CNY));
                    }
                }
                adapterPay.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                CommonUtil.toast(R.string.ActivityPayment_pricelist_failure);
            }
        });

        //初始化充值进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void initView() {
        findViewById(R.id.iv_home).setOnClickListener(this);

        orderListView = (ListView) findViewById(R.id.listview);
        rb_paypal = (RadioButton) findViewById(R.id.rb_paypal);
        rb_alipay = (RadioButton) findViewById(R.id.rb_alipay);

        //绑定点击事件
        ll_paypal = (LinearLayout) findViewById(R.id.ll_paypal);
        ll_alipay = (LinearLayout) findViewById(R.id.ll_alipay);
        ll_paypal.setOnClickListener(this);
        ll_alipay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                finish();
                break;
            case R.id.ll_paypal:
                rb_paypal.setChecked(true);
                rb_alipay.setChecked(false);
                paymentDialog.show();
                break;
            case R.id.ll_alipay:
                rb_paypal.setChecked(false);
                rb_alipay.setChecked(true);
                paymentDialog.show();
                break;
            //对话框控件
            case R.id.iv_close: {
                //充值对话框隐藏及充值金额选中状态重置
                paymentDialog.dismiss();
                for (Pay p : listPay) {
                    p.is_check = false;
                }
                adapterPay.notifyDataSetChanged();
            }
            break;
            case R.id.bt_positive: {
                pay = null;
                for (Pay p : listPay) {
                    if (p.is_check) {
                        pay = p;
                        break;
                    }
                }

                if (pay == null) {
                    CommonUtil.toast(R.string.ActivityPayment_select_topup_money);
                    return;
                }

                progressDialog.setMessage(getString(R.string.ActivityPayment_creating_order));
                progressDialog.show();

                //注意测试环境
                final Orders order = new Orders(rb_paypal.isChecked() ? pay.usd : pay.cny, rb_paypal.isChecked() ? "USD" : "CNY", "ChineseChat " + pay.subject, "ChineseChat " + pay.subject);
                HttpUtil.Parameters p = new HttpUtil.Parameters();
                p.add("id", 0 + "");
                p.add("username", ChineseChat.CurrentUser.Username);
                p.add("Currency", order.Currency);
                p.add("Amount", order.Amount + "");
                p.add("Quantity", order.Quantity + "");
                p.add("Price", order.Price + "");
                p.add("Main", order.Main + "");
                p.add("Body", order.Body + "");
                p.add("coin", pay.coin + "");
                HttpUtil.post(NetworkUtil.paymentCreateOrder, p, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        Response<Orders> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Orders>>() {
                        }.getType());
                        orderId = resp.info.Id;


                        if (rb_alipay.isChecked()) {
                            new AlipayThread(resp.info.LastOrderString).start();
                        } else if (rb_paypal.isChecked()) {
                            launchPayPalPayment(order);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        progressDialog.dismiss();
                        CommonUtil.toast(R.string.ActivityPayment_order_failure);
                        android.util.Log.i(TAG, "onFailure: " + msg);
                    }
                });

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_PAYPAL:
                handlePaypalResult(resultCode, data);
                break;
        }
    }

    private class AdapterPay extends BaseAdapter<Pay> {
        public AdapterPay(List<Pay> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Pay item = getItem(position);
            View inflate = View.inflate(ActivityPayment.this, R.layout.listitem_payment, null);
            ImageView cb_payment = (ImageView) inflate.findViewById(R.id.iv_payment);
            TextView tv_coins = (TextView) inflate.findViewById(R.id.tv_coins);
            TextView tv_price = (TextView) inflate.findViewById(R.id.tv_price);

            cb_payment.setSelected(item.is_check);
            tv_coins.setText(item.subject);
            tv_price.setText(rb_paypal.isChecked() ? (item.usd + " USD") : (item.cny + " RMB"));
            return inflate;
        }
    }

    private class AdapterOrder extends BaseAdapter<Orders> {
        public AdapterOrder(List<Orders> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Orders item = getItem(position);
            View inflate = getLayoutInflater().inflate(R.layout.listitem_records, null);
            TextView tv_main = (TextView) inflate.findViewById(R.id.tv_main);
            TextView tv_amount = (TextView) inflate.findViewById(R.id.tv_amount);
            TextView tv_state = (TextView) inflate.findViewById(R.id.tv_state);
            TextView tv_time = (TextView) inflate.findViewById(R.id.tv_createtime);

            tv_main.setText(getString(R.string.ActivityPayment_item) + item.Main);
            tv_amount.setText(getString(R.string.ActivityPayment_amount) + item.Amount + " " + ("USD".equals(item.Currency) ? "USD" : "RMB"));
            tv_state.setText(Html.fromHtml(getString(R.string.ActivityPayment_status) + "<font " + ("SUCCESS".equals(item.TradeStatus) ? ">" : " color='#ff0000'>") + ("SUCCESS".equals(item.TradeStatus) ? "Completed" : "Failed") + "</font>"));
            tv_time.setText(sdf.format(item.CreateTime));
            return inflate;
        }
    }

    private class Pay {
        int coin;
        String subject;
        BigDecimal usd;
        BigDecimal cny;
        boolean is_check;

        public Pay(int coin, BigDecimal us, BigDecimal cn) {
            this.coin = coin;
            this.subject = "Coin " + coin;
            this.usd = us;
            this.cny = cn;
        }
    }

    private class AlipayThread extends Thread {
        private String mLastOrderString;

        public AlipayThread(String lastOrderString) {
            this.mLastOrderString = lastOrderString;
        }

        @Override
        public void run() {

            // 构造PayTask 对象
            PayTask alipay = new PayTask(ActivityPayment.this);

            // 调用支付接口，获取支付结果
            // mLastOrderString 最终订单信息,主要包含商户的订单信息，key=“value”形式，以&连接,包含签名和签名类型
            // true     是否需要一个loading加载动画做为在钱包唤起之前的过渡
            String result = alipay.pay(mLastOrderString, true);

            Log.i(TAG, "result: " + result);

            PayResult payResult = new PayResult(result);
            handleAlipayResult(payResult);
        }
    }

    /**
     * Launching PalPay payment activity to complete the payment
     */
    private void launchPayPalPayment(Orders order) {
        PayPalPayment thingsToBuy = new PayPalPayment(order.Amount, order.Currency, order.Main, PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(ActivityPayment.this, com.paypal.android.sdk.payments.PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, thingsToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL);
    }

    private void handleAlipayResult(PayResult payResult) {
        Log.i(TAG, "handleAlipayResult: " + payResult.getResult());
        /**
         * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/ detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665& docType=1) 建议商户依赖异步通知
         */
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
        final String resultStatus = payResult.getResultStatus();
        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            // progressDialog.setMessage("正在验证订单");
            // progressDialog.show();

            HttpUtil.Parameters p = new HttpUtil.Parameters();
            p.add("result", resultInfo);
            p.add("orderId", orderId);
            HttpUtil.post(NetworkUtil.paymentVerifyAliPay, p, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    Log.i(TAG, "onSuccess: " + responseInfo.result);
                    Response<Orders> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Orders>>() {
                    }.getType());

                    if (resp.code == 200) {
                        CommonUtil.toast(R.string.ActivityPayment_recharge_success);

                        //保存到应用
                        ChineseChat.CurrentUser.Coins += pay.coin;
                        getSharedPreferences("user", MODE_PRIVATE).edit().putInt("coins", ChineseChat.CurrentUser.Coins).commit();
                        finish();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    CommonUtil.toast(R.string.ActivityPayment_recharge_failure);
                }
            });


        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 判断resultStatus 为非"9000"则代表可能支付失败
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(resultStatus, "8000")) {
                        CommonUtil.toast("支付结果确认中");
                    } else {
                        // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        CommonUtil.toast(R.string.ActivityPayment_recharge_failure);
                    }
                }
            });
        }
    }

    private void handlePaypalResult(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK: {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if ("approved".equals(confirm.getProofOfPayment().getState())) {
                    progressDialog.setMessage(getString(R.string.ActivityPayment_check_order));
                    progressDialog.show();

                    HttpUtil.Parameters p = new HttpUtil.Parameters();
                    p.add("paymentId", confirm.getProofOfPayment().getPaymentId());
                    p.add("orderId", orderId);
                    HttpUtil.post(NetworkUtil.paymentVerifyPayPal, p, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Log.i(TAG, "onSuccess: " + responseInfo.result);
                            progressDialog.dismiss();
                            CommonUtil.toast(R.string.ActivityPayment_recharge_success);

                            //保存到应用
                            ChineseChat.CurrentUser.Coins += pay.coin;
                            getSharedPreferences("user", MODE_PRIVATE).edit().putInt("coins", ChineseChat.CurrentUser.Coins).commit();

                            finish();
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast(R.string.ActivityPayment_recharge_failure);
                        }
                    });
                }
                Log.i(TAG, "Activity.RESULT_OK:" + confirm.toJSONObject().toString());
            }
            break;
            case Activity.RESULT_CANCELED: {
                Log.i(TAG, "The user canceled.");
            }
            break;
            case com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID: {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
            break;
        }
    }
}

