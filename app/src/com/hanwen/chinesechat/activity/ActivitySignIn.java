package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.SystemUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 用户登录界面
 *
 * @author liaorubei
 */
public class ActivitySignIn extends Activity implements OnClickListener {
    private static final String TAG = "ActivitySignIn";
    public static final int SignUp = 0;
    private static final int FROM_KICK_OUT = 1;
    private static final String KEY_SOURCE = "KEY_SOURCE";
    private AutoCompleteTextView et_username;
    private EditText et_password;
    private boolean enter_main = false;
    private View iv_username_clear;
    private View iv_password_clear;
    private ProgressDialog progressDialog;
    private List<User> users;


    private void initView() {
        View iv_home = findViewById(R.id.iv_home);
        iv_home.setOnClickListener(this);
        iv_home.setVisibility(enter_main ? View.INVISIBLE : View.VISIBLE);

        et_username = (AutoCompleteTextView) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);

        iv_username_clear = findViewById(R.id.iv_username_clear);
        iv_password_clear = findViewById(R.id.iv_password_clear);
        iv_username_clear.setOnClickListener(this);
        iv_password_clear.setOnClickListener(this);

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_username_clear.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_password_clear.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        findViewById(R.id.bt_login).setOnClickListener(this);
        findViewById(R.id.tv_signup).setOnClickListener(this);
        findViewById(R.id.tv_password).setOnClickListener(this);

        MyAdapter myAdapter = new MyAdapter(new ArrayList<String>());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"12315", "12316"});
        et_username.setAdapter(myAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignUp && resultCode == ActivitySignUp.SignUp && data != null) {
            String username = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            signInApp(username, password);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.iv_password_clear:
                et_password.setText("");
                break;
            case R.id.iv_username_clear:
                et_username.setText("");
                break;

            case R.id.bt_login:
                String account = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                    CommonUtil.toastCENTER(R.string.ActivitySignIn_email_or_password_not_null);
                    return;
                }

                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getString(R.string.ActivitySignIn_is_logging));
                }
                progressDialog.show();

                signInApp(account, password);
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
        enter_main = getIntent().getBooleanExtra("enter_main", false);
        int source = getIntent().getIntExtra(KEY_SOURCE, -1);

        users = ChineseChat.database().userList();

        initView();
        switch (source) {
            case FROM_KICK_OUT:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Note");
                builder.setMessage(String.format("Another device is attempting to log in to your ChineseChat account via password at time %1$s.If you didn't intend to log in to your account from this device,you should change your ChineseChat password in.", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
        }
    }

    public void signInApp(final String username, final String password) {
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("username", username);
        params.add("password", password);
        params.add("category", ChineseChat.isStudent() ? 0 : 1);
        params.add("system", 1);
        params.add("device", SystemUtil.getDeviceName());

        HttpUtil.post(NetworkUtil.userSignIn, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                CommonUtil.toastCENTER(R.string.ActivitySignIn_login_failure);
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());
                if (response.code == 200) {
                    Log.i(TAG, "汉问登录成功,Account=" + response.info.Accid + " token=" + response.info.Token);
                    // 登录云信
                    signInNim(response.info.Accid, response.info.Token);

                    // 保护登录信息
                    response.info.Username = username;
                    response.info.PassWord = password;
                    ChineseChat.CurrentUser = response.info;

                    getSharedPreferences("user", MODE_PRIVATE).edit().putString("userJson", new Gson().toJson(ChineseChat.CurrentUser)).apply();
                    CommonUtil.saveUserToSP(ActivitySignIn.this, response.info, true);
                } else {
                    progressDialog.dismiss();
                    //CommonUtil.toastCENTER(R.string.ActivitySignIn_login_failure);
                    CommonUtil.toast(response.desc);
                }

                ChineseChat.database().userInsertOrReplace(username, password);


            }
        });
    }

    @SuppressWarnings("unchecked")
    public void signInNim(String accid, String token) {
        NIMClient.getService(AuthService.class).login(new LoginInfo(accid, token)).setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onException(Throwable arg0) {
                Log.i(TAG, "云信登录异常:" + arg0.getMessage());
                CommonUtil.toastCENTER(R.string.ActivitySignIn_network_error_login_failure);
                progressDialog.dismiss();
            }

            @Override
            public void onFailed(int arg0) {
                Log.i(TAG, "云信登录失败:" + arg0);
                CommonUtil.toastCENTER(R.string.ActivitySignIn_network_error_login_failure);
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(LoginInfo info) {
                Log.i(TAG, "云信登录成功,Account=" + info.getAccount() + " token=" + info.getToken());

                Editor editor = ActivitySignIn.this.getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putString("accid", info.getAccount());
                editor.putString("token", info.getToken());
                editor.apply();

                if (enter_main) {
                    //进入到MainActivity主界面
                    startActivity(new Intent(ActivitySignIn.this, ActivityMain.class));
                }
                finish();
            }
        });
    }

    public static void startFromKickout(Context context) {
        Intent intent = new Intent(context, ActivitySignIn.class);
        intent.putExtra(KEY_SOURCE, FROM_KICK_OUT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private class MyAdapter extends BaseAdapter<String> implements Filterable {
        private final List<String> list;
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        public MyAdapter(List<String> list) {
            super(list);
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(ActivitySignIn.this);
            textView.setText(this.list.get(position));
            textView.setPadding(padding, padding, padding, padding);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(-1, -2);
            textView.setLayoutParams(layoutParams);
            return textView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    list.clear();
                    for (User u : users) {
                        if (u.Username.contains(constraint)) {
                            list.add(u.Username);
                        }
                    }
                    return null;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }
}
