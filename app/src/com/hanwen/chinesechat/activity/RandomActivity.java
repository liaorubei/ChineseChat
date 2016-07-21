package com.hanwen.chinesechat.activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.activity.ActivityCall;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RandomActivity extends Activity implements OnClickListener
{
	private Button bt_call;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_random);
		initView();

		startActivity(new Intent(this, ActivitySignIn.class));
	}

	private void initView()
	{
		bt_call = (Button) findViewById(R.id.bt_call);
		bt_call.setOnClickListener(this);

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_call:
			bt_call.setEnabled(false);

			Parameters parameters = new Parameters();
			parameters.add("id", getSharedPreferences("user", MODE_PRIVATE).getInt("id", 0) + "");
			HttpUtil.post(NetworkUtil.ObtainTeacher, parameters, new RequestCallBack<String>()
			{

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo)
				{
					bt_call.setEnabled(true);
					Response<User> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<User>>()
					{}.getType());
					if (response.code == 200)
					{
						Intent intent = new Intent(getApplication(), ActivityCall.class);
						intent.putExtra(ActivityCall.KEY_TARGET_ID, response.info.Id);
						intent.putExtra(ActivityCall.KEY_TARGET_ACCID, response.info.Accid);
						intent.putExtra(ActivityCall.KEY_TARGET_NICKNAME, response.info.Nickname);
						startActivity(intent);
					}
					else
					{
						CommonUtil.toast(response.desc);
					}
				}

				@Override
				public void onFailure(HttpException error, String msg)
				{
					bt_call.setEnabled(true);
					CommonUtil.toast("网络异常,请求失败");
				}
			});

			break;

		default:
			break;
		}
	}
}
