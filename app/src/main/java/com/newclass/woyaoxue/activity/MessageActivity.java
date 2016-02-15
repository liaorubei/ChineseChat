package com.newclass.woyaoxue.activity;

import java.util.List;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageActivity extends Activity implements OnClickListener
{
	private Button bt_text, bt_video;
	private SurfaceView sv_video;
	private LinearLayout ll_text;
	private Observer<List<IMMessage>> messageObserver;// 消息监听
	private String target;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		target = getIntent().getStringExtra("target");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);

		initView();

		messageObserver = new Observer<List<IMMessage>>()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(List<IMMessage> messages)
			{
				for (IMMessage imMessage : messages)
				{
					if (imMessage.getMsgType() == MsgTypeEnum.text)
					{
						TextView textView = new TextView(MessageActivity.this);
						textView.setText(imMessage.getFromAccount() + " " + imMessage.getContent());
						ll_text.addView(textView);
					}
					Log.i("logi", "来信息了:" + imMessage.getContent() + " 消息类型:" + imMessage.getMsgType());
				}
			}
		};

		// 消息监听注册 
		NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(messageObserver, true);
	}

	private void initView()
	{
		bt_text = (Button) findViewById(R.id.bt_text);
		bt_video = (Button) findViewById(R.id.bt_video);
		ll_text = (LinearLayout) findViewById(R.id.ll_text);
		sv_video = (SurfaceView) findViewById(R.id.sv_video);

		bt_text.setOnClickListener(this);
		bt_video.setOnClickListener(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// 消息监听注销
		NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(messageObserver, false);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_text:
			IMMessage message = MessageBuilder.createTextMessage(target, SessionTypeEnum.P2P, "文本内容1");
			NIMClient.getService(MsgService.class).sendMessage(message, false);

			break;

		case R.id.bt_video:
			AVChatType callType = AVChatType.VIDEO;
			VideoChatParam videoParam = new VideoChatParam(sv_video, 0);// 视频显示Surface,角度
			AVChatManager.getInstance().call(target, callType, null, new AVChatCallback<AVChatData>()
			{

				@Override
				public void onException(Throwable arg0)
				{
					Log.i("call onException");
					System.currentTimeMillis();

				}

				@Override
				public void onFailed(int arg0)
				{
					Log.i("call onFailed");

				}

				@Override
				public void onSuccess(AVChatData arg0)
				{
					Log.i("call onSuccess");
				}
			});

			break;

		default:
			break;
		}

	}
}
