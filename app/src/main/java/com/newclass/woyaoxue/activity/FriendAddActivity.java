package com.newclass.woyaoxue.activity;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FriendAddActivity extends Activity implements OnClickListener
{
	private Button bt_reject, bt_accept;
	private EditText et_content;

	private String content;
	private String account;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friendadd);

		Intent intent = getIntent();
		account = intent.getStringExtra("account");
		content = intent.getStringExtra("content");

		initView();

	}

	private void initView()
	{
		bt_accept = (Button) findViewById(R.id.bt_accept);
		bt_reject = (Button) findViewById(R.id.bt_reject);
		et_content = (EditText) findViewById(R.id.et_content);

		bt_accept.setOnClickListener(this);
		bt_reject.setOnClickListener(this);
		et_content.setText(content);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_accept:
			NIMClient.getService(FriendService.class).ackAddFriendRequest(account, true); // 通过对方的好友请求

			break;
		case R.id.bt_reject:
			NIMClient.getService(FriendService.class).ackAddFriendRequest(account, true); // 通过对方的好友请求
			break;
		default:
			break;
		}

	}

}
