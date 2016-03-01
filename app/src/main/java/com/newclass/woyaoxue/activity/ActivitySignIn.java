package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.newclass.woyaoxue.MyApplication;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * 用户登录界面
 *
 * @author liaorubei
 */
public class ActivitySignIn extends Activity implements OnClickListener {
    private static final String TAG = "ActivitySignIn";
    public static final int SignUp = 0;
    private Button bt_login;
    private EditText et_username, et_password;
    private TextView tv_signup;

    private RequestCallback<LoginInfo> callbackSignin = new RequestCallback<LoginInfo>() {
        @Override
        public void onException(Throwable arg0) {
            Log.i("logi", "云信登录异常:" + arg0.getMessage());
            CommonUtil.toast("网络异常,登录失败");
        }

        @Override
        public void onFailed(int arg0) {
            Log.i("logi", "云信登录失败:" + arg0);
            CommonUtil.toast("网络异常,登录失败");
        }

        @Override
        public void onSuccess(LoginInfo info) {
            Log.i(TAG, "云信登录成功,Account:" + info.getAccount() + " token=" + info.getToken());

            Editor editor = ActivitySignIn.this.getSharedPreferences("user", MODE_PRIVATE).edit();
            editor.putString("accid", info.getAccount());
            editor.putString("token", info.getToken());
            editor.commit();

            initAVChatManager();

            finish();
        }
    };
    private Observer<List<IMMessage>> observerReceiveMessage = new Observer<List<IMMessage>>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void onEvent(List<IMMessage> list) {
            for (IMMessage imMessage : list) {
                Log.i(TAG, "收到消息MsgType:" + imMessage.getMsgType() + " Content:" + imMessage.getContent() + " FromAccount:" + imMessage.getFromAccount() + " SessionId:" + imMessage.getSessionId());
            }
        }
    };

    private void initView() {
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        bt_login = (Button) findViewById(R.id.bt_login);
        tv_signup = (TextView) findViewById(R.id.tv_signup);

        bt_login.setOnClickListener(this);
        tv_signup.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignUp && resultCode == ActivitySignUp.SignUp && data != null) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            signIn(username, password);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                bt_login.setEnabled(false);

                String account = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                    Toast.makeText(ActivitySignIn.this, "帐号或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 开始登录
                signIn(account, password);

                break;

            case R.id.tv_signup:
                startActivityForResult(new Intent(ActivitySignIn.this, ActivitySignUp.class), SignUp);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // 取出保存的用户数据,如果存在就直接登录
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        if (!(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))) {
            // signIn(username, password);
        }

        initView();

        //  NIMClient.getService(AuthService.class).logout();// 登出帐号
    }

    public void signIn(final String username, final String password) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("username", username);
        params.addBodyParameter("password", password);

        new HttpUtils().send(HttpMethod.POST, NetworkUtil.userSignIn, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                bt_login.setEnabled(true);
                Toast.makeText(MyApplication.getContext(), "网络异常,请稍后重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "汉问登录成功,下面开始登录云信");
                bt_login.setEnabled(true);
                Response<User> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (response.code == 200) {
                    // 登录云信
                    signInNim(response.info.Accid, response.info.Token);

                    // 保护登录信息
                    response.info.UserName = username;
                    response.info.PassWord = password;
                    CommonUtil.saveUserToSP(ActivitySignIn.this, response.info);
                } else {
                    Toast.makeText(MyApplication.getContext(), "登录失败,帐号密码不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void signInNim(String accid, String token) {
        NIMClient.getService(AuthService.class).login(new LoginInfo(accid, token)).setCallback(callbackSignin);

        // 监听用户在线状态
        //NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(observerOnlineStatus, true);
    }

    protected void initAVChatManager() {
        // 消息监听注册
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(observerReceiveMessage, true);
    }

}
