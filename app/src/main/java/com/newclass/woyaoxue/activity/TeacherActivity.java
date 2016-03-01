package com.newclass.woyaoxue.activity;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TeacherActivity extends Activity implements OnClickListener
{
	private Button bt_queue, bt_group;
	private String accid;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_queue);
		initView();
		NIMClient.getService(AuthService.class).logout();
		startActivity(new Intent(this, ActivitySignIn.class));
	}

	private void initView()
	{
		bt_queue = (Button) findViewById(R.id.bt_queue);
		bt_group = (Button) findViewById(R.id.bt_group);
		bt_queue.setOnClickListener(this);
		bt_group.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_queue:
			startActivity(new Intent(getApplication(), TeacherQueueActivity.class));
			break;

		case R.id.bt_group:
			break;

		default:
			break;
		}
	}
}
