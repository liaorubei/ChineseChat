package com.newclass.woyaoxue.activity;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 电话接听界面
 * @author liaorubei
 *
 */
public class PickActivity extends Activity implements OnClickListener
{
	public static final String TARGET = "TARGET";
	protected static final String CALL_TYPE_KEY = "CALL_TYPE_KEY";
	protected static final int CALL_TYPE_AUDIO = 1;
	protected static final int CALL_TYPE_VIDEO = 2;

	private Button bt_hangup, bt_mute, bt_free, bt_face, bt_text, bt_card, bt_more;
	private ImageView iv_icon;
	private TextView tv_nickname;
	private Chronometer cm_time;
	private AVChatCallback<Void> avChatCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_teacher);

		initView();
		initData();
	}

	private void initData()
	{
		Intent intent = getIntent();
		String target = intent.getStringExtra(TARGET);
		int call_type = intent.getIntExtra(CALL_TYPE_KEY, CALL_TYPE_AUDIO);

		AVChatManager.getInstance().call(target, AVChatType.AUDIO, null, new AVChatCallback<AVChatData>()
		{

			@Override
			public void onSuccess(AVChatData arg0)
			{
				cm_time.start();
			}

			@Override
			public void onFailed(int arg0)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onException(Throwable arg0)
			{
				// TODO Auto-generated method stub

			}
		});

	}

	private void initView()
	{
		bt_hangup = (Button) findViewById(R.id.bt_hangup);
		bt_mute = (Button) findViewById(R.id.bt_mute);
		bt_free = (Button) findViewById(R.id.bt_free);
		bt_face = (Button) findViewById(R.id.bt_face);
		bt_text = (Button) findViewById(R.id.bt_text);
		bt_card = (Button) findViewById(R.id.bt_card);
		bt_more = (Button) findViewById(R.id.bt_more);

		tv_nickname = (TextView) findViewById(R.id.tv_nickname);
		cm_time = (Chronometer) findViewById(R.id.cm_time);

		bt_hangup.setOnClickListener(this);
		bt_mute.setOnClickListener(this);
		bt_free.setOnClickListener(this);
		bt_face.setOnClickListener(this);
		bt_text.setOnClickListener(this);
		bt_card.setOnClickListener(this);
		bt_more.setOnClickListener(this);

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_hangup:
			hangup();
			break;

		default:
			break;
		}
	}

	private void hangup()
	{
		if (avChatCallback == null)
		{
			avChatCallback = new AVChatCallback<Void>()
			{

				@Override
				public void onSuccess(Void arg0)
				{

				}

				@Override
				public void onFailed(int arg0)
				{

				}

				@Override
				public void onException(Throwable arg0)
				{

				}
			};
		}
		AVChatManager.getInstance().hangUp(avChatCallback);
	}
}
