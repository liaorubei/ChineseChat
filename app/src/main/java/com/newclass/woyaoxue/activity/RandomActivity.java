package com.newclass.woyaoxue.activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

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

		startActivity(new Intent(this, SignInActivity.class));
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
						Intent intent = new Intent(getApplication(), CallActivity.class);
						intent.putExtra(CallActivity.KEY_TARGET_ID, response.info.Id);
						intent.putExtra(CallActivity.KEY_TARGET_ACCID, response.info.Accid);
						intent.putExtra(CallActivity.KEY_TARGET_NICKNAME, response.info.Name);
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
