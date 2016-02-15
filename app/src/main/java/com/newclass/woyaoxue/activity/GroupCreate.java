package com.newclass.woyaoxue.activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.bean.Group;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class GroupCreate extends Activity implements OnClickListener
{

	public static final int RESULT_CODE = 0;
	public static final int REQUEST_CODE = 1;
	private EditText et_name, et_theme, et_notice;
	private Button bt_create;
	private boolean post = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_create);

		//initView();
		getActionBar().setDisplayShowHomeEnabled(true);
	}

	private void initView()
	{
		//et_name = (EditText) findViewById(R.id.et_name);
		et_theme = (EditText) findViewById(R.id.et_theme);
		//et_notice = (EditText) findViewById(R.id.et_notice);

		bt_create = (Button) findViewById(R.id.bt_create);
		bt_create.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_create:
			if (!post)
			{
				post = true;

				String name = et_name.getText().toString();
				String theme = et_theme.getText().toString();
				String notice = et_notice.getText().toString();

				if (TextUtils.isEmpty(name) || TextUtils.isEmpty(theme) || TextUtils.isEmpty(notice))
				{
					CommonUtil.toast("要提交的内容不能为空");
					return;
				}

				SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
				String accid = sp.getString("accid", "");

				Parameters parameters = new Parameters();
				parameters.add("host", accid);// 创建人,主持
				parameters.add("name", name);// 群名称
				parameters.add("theme", theme);// 群主题
				parameters.add("notice", notice);// 群公告
				HttpUtil.post(NetworkUtil.groupCreate, parameters, new RequestCallBack<String>()
				{

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo)
					{
						Response<Group> r = new Gson().fromJson(responseInfo.result, new TypeToken<Response<Group>>()
						{
						}.getType());

						Log.i("logi", "Name:" + r.info.Name);
						post = false;
						setResult(GroupCreate.RESULT_CODE);
						finish();
					}

					@Override
					public void onFailure(HttpException error, String msg)
					{
						// TODO Auto-generated method stub
						post = false;
					}
				});
			}
			else
			{
				CommonUtil.toast("请不要重复提交");
			}

			break;

		default:
			break;
		}
	}
}
