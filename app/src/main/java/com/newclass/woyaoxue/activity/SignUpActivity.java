package com.newclass.woyaoxue.activity;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.protocol.HTTP;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.newclass.woyaoxue.bean.Answer;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 帐号注册界面
 * @author liaorubei
 *
 */
public class SignUpActivity extends Activity implements OnClickListener
{
	public static final int SignUp = 0;

	private EditText et_phone, et_captcha;
	private EditText et_nickname, et_password, et_repassword;
	private Button bt_signup;

	private TextView tv_next, tv_captcha, tv_term;
	private CheckBox cb_is_teacher;

	private LinearLayout ll_first, ll_second;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		initView();
	}

	private void initView()
	{
		et_phone = (EditText) findViewById(R.id.et_phone);
		et_captcha = (EditText) findViewById(R.id.et_captcha);
		tv_next = (TextView) findViewById(R.id.tv_next);

		tv_next.setOnClickListener(this);

		et_nickname = (EditText) findViewById(R.id.et_nickname);
		et_password = (EditText) findViewById(R.id.et_password);
		et_repassword = (EditText) findViewById(R.id.et_repassword);

		bt_signup = (Button) findViewById(R.id.bt_signup);
		bt_signup.setOnClickListener(this);

		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		ll_second = (LinearLayout) findViewById(R.id.ll_second);

		cb_is_teacher = (CheckBox) findViewById(R.id.cb_is_teacher);

		/*
		 * bt_signup.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { String account = et_phone.getText().toString().trim(); String password = et_password.getText().toString().trim(); String repassword = et_repassword.getText().toString().trim();
		 * 
		 * if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) { Toast.makeText(SignUpActivity.this, "帐号或密码不能为空", Toast.LENGTH_SHORT).show(); return; }
		 * 
		 * if (!password.equals(repassword)) { Toast.makeText(SignUpActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show(); return; }
		 * 
		 * HttpUtils httpUtils = new HttpUtils(); RequestParams params = new RequestParams(); params.addBodyParameter("account", account); params.addBodyParameter("password", password); params.addBodyParameter("NickName", account); params.addBodyParameter("phone", account);
		 * 
		 * bt_signup.setEnabled(false); httpUtils.send(HttpMethod.POST, NetworkUtil.userCreate, params, new RequestCallBack<String>() {
		 * 
		 * @Override public void onSuccess(ResponseInfo<String> responseInfo) { Answer json = new Gson().fromJson(responseInfo.result, Answer.class);
		 * 
		 * Log.i("logi", "创建成功:" + json.toString()); if (200 == json.code) { Toast.makeText(SignUpActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
		 * 
		 * // 返回注册信息 Intent data = new Intent(); data.putExtra("username", et_phone.getText().toString().trim()); data.putExtra("password", et_password.getText().toString().trim()); setResult(SignUp, data); finish(); } else { Toast.makeText(SignUpActivity.this, json.desc, Toast.LENGTH_SHORT).show(); } }
		 * 
		 * @Override public void onFailure(HttpException error, String msg) { Toast.makeText(SignUpActivity.this, "网络异常,请重试", Toast.LENGTH_SHORT).show(); bt_signup.setEnabled(true); } });
		 * 
		 * } });
		 */
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.tv_next:// 提交验证码
		{
			String phone = et_phone.getText().toString().trim();
			String captcha = et_captcha.getText().toString().trim();

			if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(captcha))
			{
				CommonUtil.toast("数据不能为空");
				return;
			}

			Parameters parameters = new Parameters();
			parameters.add("phone", phone);
			parameters.add("captcha", captcha);
			HttpUtil.post(NetworkUtil.userVerify, parameters, new RequestCallBack<String>()
			{

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo)
				{
					Response<String> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<String>>()
					{}.getType());
					if (response.code == 200)
					{
						ll_first.setVisibility(View.INVISIBLE);
						ll_second.setVisibility(View.VISIBLE);
					}
					else
					{
						CommonUtil.toast(response.desc);
					}
					tv_next.setEnabled(true);
				}

				@Override
				public void onFailure(HttpException error, String msg)
				{
					tv_next.setEnabled(true);
				}
			});
			tv_next.setEnabled(false);
		}
			break;

		case R.id.bt_signup:

		{
			String phone = et_phone.getText().toString().trim();
			String nickname = et_nickname.getText().toString().trim();
			String password = et_password.getText().toString().trim();
			String repassword = et_repassword.getText().toString().trim();

			if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(nickname) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword))
			{
				CommonUtil.toast("数据不能为空");
				return;
			}

			if (!password.equals(repassword))
			{
				CommonUtil.toast("两次输入的密码不一致");
				return;
			}

			Parameters parameters = new Parameters();
			parameters.add("username", phone);
			parameters.add("password", password);
			parameters.add("nickname", nickname);
			parameters.add("category", "" + (cb_is_teacher.isChecked() ? 1 : 0));
			HttpUtil.post(NetworkUtil.userCreate, parameters, new RequestCallBack<String>()
			{

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo)
				{
					bt_signup.setEnabled(true);

					ll_first.setVisibility(View.INVISIBLE);
					ll_second.setVisibility(View.INVISIBLE);

					Response<User> json = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>()
					{}.getType());

					Log.i("logi", "创建成功:" + json.toString());
					if (200 == json.code)
					{
						Toast.makeText(SignUpActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

						// 返回注册信息
						Intent data = new Intent();
						data.putExtra("username", et_phone.getText().toString().trim());
						data.putExtra("password", et_password.getText().toString().trim());
						setResult(SignUp, data);
						finish();
					}
					else
					{
						Toast.makeText(SignUpActivity.this, json.desc, Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onFailure(HttpException error, String msg)
				{
					Toast.makeText(SignUpActivity.this, "网络异常,请重试", Toast.LENGTH_SHORT).show();
					bt_signup.setEnabled(true);
				}
			});
			bt_signup.setEnabled(false);
		}

			break;

		default:
			break;
		}
	}
}
