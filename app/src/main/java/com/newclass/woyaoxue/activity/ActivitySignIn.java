package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.MainActivity;
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
    private boolean enter_main = false;

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
        findViewById(R.id.iv_home).setOnClickListener(this);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        bt_login = (Button) findViewById(R.id.bt_login);
        tv_signup = (TextView) findViewById(R.id.tv_signup);

        bt_login.setOnClickListener(this);
        tv_signup.setOnClickListener(this);
        findViewById(R.id.tv_password).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignUp && resultCode == ActivitySignUp.SignUp && data != null) {
            String username = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            signIn(username, password);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.bt_login:
                bt_login.setEnabled(false);

                String account = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                    CommonUtil.toastCENTER(R.string.ActivitySignIn_email_or_password_not_null);
                    return;
                }

                // 开始登录
                signIn(account, password);
                break;
            case R.id.tv_signup:
                if (ChineseChat.isStudent()) {
                    startActivityForResult(new Intent(ActivitySignIn.this, ActivitySignUp.class), SignUp);
                } else {
                    CommonUtil.toast("教师端暂时无法注册");
                }
                break;
            case R.id.tv_password:
                startActivity(new Intent(getApplicationContext(), ActivityReset.class));
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initView();
        enter_main = getIntent().getBooleanExtra("enter_main", false);
    }

    public void signIn(final String username, final String password) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("username", username);
        params.addBodyParameter("password", password);

        new HttpUtils().send(HttpMethod.POST, NetworkUtil.userSignIn, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                bt_login.setEnabled(true);
                CommonUtil.toastCENTER(R.string.ActivitySignIn_login_failure);
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
                    response.info.Username = username;
                    response.info.PassWord = password;
                    ChineseChat.CurrentUser = response.info;

                    CommonUtil.saveUserToSP(ActivitySignIn.this, response.info, true);
                } else {
                    CommonUtil.toastCENTER(R.string.ActivitySignIn_login_failure);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void signInNim(String accid, String token) {
        NIMClient.getService(AuthService.class).login(new LoginInfo(accid, token)).setCallback(new CallbackLogin());

        // 监听用户在线状态
        //NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(observerOnlineStatus, true);
    }

    protected void initAVChatManager() {
        // 消息监听注册
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(observerReceiveMessage, true);
    }

    private class CallbackLogin implements RequestCallback<LoginInfo> {
        @Override
        public void onException(Throwable arg0) {
            Log.i(TAG, "云信登录异常:" + arg0.getMessage());
            CommonUtil.toastCENTER(R.string.ActivitySignIn_network_error_login_failure);
        }

        @Override
        public void onFailed(int arg0) {
            Log.i(TAG, "云信登录失败:" + arg0);
            CommonUtil.toastCENTER(R.string.ActivitySignIn_network_error_login_failure);
        }

        @Override
        public void onSuccess(LoginInfo info) {
            Log.i(TAG, "云信登录成功,Account:" + info.getAccount() + " token=" + info.getToken());

            Editor editor = ActivitySignIn.this.getSharedPreferences("user", MODE_PRIVATE).edit();
            editor.putString("accid", info.getAccount());
            editor.putString("token", info.getToken());
            editor.commit();

            initAVChatManager();

            if (enter_main) {
                //进入到MainActivity主界面
                startActivity(new Intent(ActivitySignIn.this, MainActivity.class));
            }

            finish();
        }
    }
}
