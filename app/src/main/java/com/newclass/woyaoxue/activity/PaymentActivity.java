package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Orders;
import com.newclass.woyaoxue.bean.PayResult;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.ConstantsUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.newclass.woyaoxue.util.SignUtils;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.voc.woyaoxue.R;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PaymentActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PaymentActivity";
    private static final String KEY_SUBJECT = "KEY_SUBJECT";
    private static final String KEY_USD = "KEY_USD";
    private static final String KEY_CNY = "KEY_CNY";
    private RadioButton rb_alipay, rb_paypal;
    private Button bt_pay;

    /*
    // 商户PID
    public static final String PARTNER = "2088121919363034";
    // 商户收款账号
    public static final String SELLER = "huangjb@chinesechat.cn";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPFdmm8Dh7Snwkp3se+FUuZGJE9kPO9uZsVBEeMnyuUmB2384sUORH/X/zAtoqcz1nDV5u0jQIo4jUnORhTAV+EX9HG5sjzImWDKcHbeSrXrezZAD+I3ddRp6Pspw136Gndz43w/tP0d9TymJGtz+5kZxa77ruudgyS5rsocjXyhAgMBAAECgYEAzo8CmTr2Kj7fYYdp+cepmHQyotbv5yAeR3VWb4Ygd1bCSPiAwY9iQ95//6Uua9VLEamdRRhEJYYcNCuZgizRhqG582Va05c2m9zcTMeS+dUMbNdyIY+ZxOzudGOn3C/QNN8/XaCzZtsgeKzDeNMNnjs0MqjOq8k3gPLKW4AFR9kCQQD+koRM1TXyMHF9VZTuYhPyHNY88bJgAH6ByTsplBbAPiJbg/xQsszRs7CU/n6ANboVPsKWJBDhrhf3AWy2sUafAkEA8rggPVHDEJr3C3x2XQ3hjmxTDMDnYM4FE6mLNUk2xv/1iXpvWKI1yCoyTCgCRANuIHDqF3LAaBsTj2Kr3h60vwJBAPUSMeERhIxxzF+fKu/OZWs4DZrABztaXm8tPRJK6RgK+OJnDljVuE3MkZrt4PQmRMy9DXCiqcnI4nM84N6DjPsCQAg4zH7HQkBRv4SYFrpYOgfFC5sm/a99yxY7bAfGDyD2kq6xgwwRkpjRNRr3T/xV0Wkv6f4ZWQMtx5/Xy9KeX6kCQQDZtKF09SPIb3X7ZsRUNEkeOBWwdutstgILTmnzTP/K1ORq1Udv64sPTyDV8OwKRGQ0sdMGDA67OzeqbBgJbw45";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDxXZpvA4e0p8JKd7HvhVLmRiRPZDzvbmbFQRHjJ8rlJgdt/OLFDkR/1/8wLaKnM9Zw1ebtI0CKOI1JzkYUwFfhF/RxubI8yJlgynB23kq163s2QA/iN3XUaej7KcNd+hp3c+N8P7T9HfU8piRrc/uZGcWu+67rnYMkua7KHI18oQIDAQAB";
*/
    private static final int SDK_PAY_FLAG = 1;


    private static PayPalConfiguration paypalConfig = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(ConstantsUtil.PAYPAL_CLIENT_ID);


    private String subject;
    private Serializable usd;
    private Serializable cny;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initView();

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        initData();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    private void initData() {
        Intent intent = getIntent();
        subject = intent.getStringExtra(KEY_SUBJECT);
        usd = intent.getSerializableExtra(KEY_USD);
        cny = intent.getSerializableExtra(KEY_CNY);

        tv_subject.setText(subject);
        tv_money.setText("CNY:" + cny);
    }

    public static void start(Context context, String subject, BigDecimal usd, BigDecimal cny) {
        Intent intent = new Intent(context, PaymentActivity.class);

        intent.putExtra(KEY_SUBJECT, subject);
        intent.putExtra(KEY_USD, usd);
        intent.putExtra(KEY_CNY, cny);
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private TextView tv_money, tv_subject;

    private void initView() {
        tv_subject = (TextView) findViewById(R.id.tv_subject);
        tv_money = (TextView) findViewById(R.id.tv_money);
        rb_alipay = (RadioButton) findViewById(R.id.rb_alipay);
        rb_paypal = (RadioButton) findViewById(R.id.rb_paypal);

        bt_pay = (Button) findViewById(R.id.bt_pay);
        bt_pay.setOnClickListener(this);
        rb_alipay.setOnClickListener(this);
        rb_paypal.setOnClickListener(this);
    }

    private Gson gson = new Gson();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_pay: {
                // alipay();
                progressDialog.setMessage("正在创建订单");
                progressDialog.show();

                final Orders order = new Orders(0.01, "USD", "ChineseChat 学币充值", "ChineseChat 学币1000枚");
                HttpUtil.Parameters p = new HttpUtil.Parameters();
                p.add("id", 0 + "");
                p.add("username", getSharedPreferences("user", MODE_PRIVATE).getString("username", ""));
                p.add("Currency", order.Currency);
                p.add("Amount", order.Amount + "");
                p.add("Quantity", order.Quantity + "");
                p.add("Price", order.Price + "");
                p.add("Main", order.Main + "");
                p.add("Body", order.Body + "");
                HttpUtil.post(NetworkUtil.paymentCreateOrder, p, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Response<Orders> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<Orders>>() {
                        }.getType());
                        orderId = resp.info.Id;
                        progressDialog.dismiss();

                        if (rb_alipay.isChecked()) {
                            AlipayThread d = new AlipayThread(resp.info.LastOrderString);
                            d.start();
                        } else if (rb_paypal.isChecked())

                        {
                            launchPayPalPayment(order);
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                        progressDialog.dismiss();
                        CommonUtil.toast("订单创建失败");
                    }
                });

            }
            break;
            case R.id.rb_alipay:
                rb_paypal.setChecked(false);
                tv_money.setText("CNY:" + cny);

                break;
            case R.id.rb_paypal:
                rb_alipay.setChecked(false);
                tv_money.setText("USD:" + usd);
                break;


        }
    }

    /**
     * call alipay sdk pay. 调用支付宝SDK支付
     */
    public void alipay() {

        /*
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) { //
                    finish();
                }
            }).show();
            return;
        }
        */
        String orderInfo = getOrderInfo("ChineseChat充值", "ChineseChat充值1000学币", "0.01");
        Log.i(TAG, "签名之前: " + orderInfo);


        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        Log.i(TAG, "签名之后: " + sign);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
            Log.i(TAG, "已编码签名: " + sign);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();
        Log.i(TAG, "签名之后+URL编码+签名类型: " + payInfo);
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(PaymentActivity.this);

                // 调用支付接口，获取支付结果
                // payInfo  主要包含商户的订单信息，key=“value”形式，以&连接
                // true     是否需要一个loading加载动画做为在钱包唤起之前的过渡
                String result = alipay.pay(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                //  mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        //  payThread.start();
    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String subject, String body, String price) {
/*
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm" + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";
*/
        return "";//orderInfo;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return "";//SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        Log.i(TAG, "getOutTradeNo: " + key);

        return "123456789";
    }

    private static final int REQUEST_CODE_PAYPAL = 50;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_PAYPAL:

                handlePaypalResult(resultCode, data);


                break;

            case 111:

                break;


        }

    }

    private String orderId;

    private void handlePaypalResult(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK: {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if ("approved".equals(confirm.getProofOfPayment().getState())) {
                    progressDialog.setMessage("正在验证订单");
                    progressDialog.show();

                    HttpUtil.Parameters p = new HttpUtil.Parameters();
                    p.add("paymentId", confirm.getProofOfPayment().getPaymentId());
                    p.add("orderId", orderId);
                    HttpUtil.post(NetworkUtil.paymentVerifyPayPal, p, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Log.i(TAG, "onSuccess: " + responseInfo.result);
                            progressDialog.dismiss();
                            CommonUtil.toast("支付成功");
                            finish();
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast("支付失败");
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

    /**
     * Launching PalPay payment activity to complete the payment
     */
    private void launchPayPalPayment(Orders order) {
        PayPalPayment thingsToBuy = new PayPalPayment(new BigDecimal(order.Amount), order.Currency, order.Main, PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(PaymentActivity.this, com.paypal.android.sdk.payments.PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, thingsToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL);
    }

    private class AlipayThread extends Thread {
        private String mLastOrderString;

        public AlipayThread(String lastOrderString) {
            this.mLastOrderString = lastOrderString;
        }

        @Override
        public void run() {

            // 构造PayTask 对象
            PayTask alipay = new PayTask(PaymentActivity.this);

            // 调用支付接口，获取支付结果
            // mLastOrderString 最终订单信息,主要包含商户的订单信息，key=“value”形式，以&连接,包含签名和签名类型
            // true     是否需要一个loading加载动画做为在钱包唤起之前的过渡
            String result = alipay.pay(mLastOrderString, true);

            Log.i(TAG, "result: " + result);

            PayResult payResult = new PayResult(result);
            handleAlipayResult(payResult);
        }
    }

    private void handleAlipayResult(PayResult payResult) {
        Log.i(TAG, "handleAlipayResult: " + payResult.getResult());
        /**
         * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/ detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665& docType=1) 建议商户依赖异步通知
         */
        String resultInfo = payResult.getResult();// 同步返回需要验证的信息

        String resultStatus = payResult.getResultStatus();
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
                        CommonUtil.toast("支付成功");
                        finish();
                    }

                  //  progressDialog.dismiss();
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                   // progressDialog.dismiss();
                }
            });


        } else {
            // 判断resultStatus 为非"9000"则代表可能支付失败
            // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                Toast.makeText(PaymentActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

            } else {
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                Toast.makeText(PaymentActivity.this, "支付失败", Toast.LENGTH_SHORT).show();

            }
        }
    }

}
