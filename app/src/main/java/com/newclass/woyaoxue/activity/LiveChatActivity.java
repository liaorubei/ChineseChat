package com.newclass.woyaoxue.activity;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LiveChatActivity extends Activity implements OnClickListener
{
	public static final String CHATSTATE_KEY = "CHATSTATE_KEY";
	public static final int CHATSTATE_NONE = 0;
	public static final int CHATSTATE_CALL = 1;
	public static final int CHATSTATE_TAKE = 2;
	public static final int CHATSTATE_BUSY = 3;
	public static int CHAT_STATE = CHATSTATE_NONE;

	private Button bt_accept, bt_hangup;
	private SurfaceView sv_video;
	private TextView tv_call, tv_take, tv_busy;
	private AVChatData chatData;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Intent intent = getIntent();
		String target = intent.getStringExtra("target");
		CHAT_STATE = intent.getIntExtra(CHATSTATE_KEY, CHATSTATE_NONE);

		Log.i("logi", "chatState:" + CHAT_STATE + " target:" + target);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_livechat);

		if (CHAT_STATE == CHATSTATE_CALL)
		{
			VideoChatParam param = new VideoChatParam(sv_video, 0);
			AVChatManager.getInstance().call(target, AVChatType.AUDIO, param,null, new AVChatCallback<AVChatData>()
			{
				@Override
				public void onSuccess(AVChatData avChatData)
				{
					CommonUtil.toast("拨打成功");
				}

				@Override
				public void onFailed(int arg0)
				{
					CommonUtil.toast("网络失败,请重试");
				}

				@Override
				public void onException(Throwable arg0)
				{
					CommonUtil.toast("网络异常,请重试");
				}
			});

		}

		initView();
		resetGUI();

		// 监听网络通话被叫方的响应（接听、拒绝、忙）
		AVChatManager.getInstance().observeCalleeAckNotification(new Observer<AVChatCalleeAckEvent>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(AVChatCalleeAckEvent ackEvent)
			{
				Log.i("logi", "observeCalleeAckNotification:" + ackEvent.getEvent());

				AVChatEventType event = ackEvent.getEvent();

				switch (event)
				{
				case CALLEE_ACK_AGREE:// 被叫方同意接听
					if (ackEvent.isDeviceReady())
					{
						CommonUtil.toast("设备正常,开始通话");
						LiveChatActivity.CHAT_STATE = LiveChatActivity.CHATSTATE_BUSY;
						resetGUI();
					}
					else
					{
						CommonUtil.toast("设备异常,无法通话");
						finish();
					}
					break;
				case CALLEE_ACK_REJECT:
					CommonUtil.toast("对方拒绝接听");
					finish();
					break;

				case CALLEE_ACK_BUSY:
					CommonUtil.toast("对方忙");
					finish();
					break;
				default:
					break;
				}

			}
		}, true);

		// 监听网络通话对方挂断的通知,即在正常通话时,结束通话
		AVChatManager.getInstance().observeHangUpNotification(new Observer<AVChatCommonEvent>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(AVChatCommonEvent event)
			{
				Log.i("logi", "对方已挂断");
				finish();
			}
		}, true);

	}

	private void resetGUI()
	{
		tv_call.setVisibility(CHAT_STATE == CHATSTATE_CALL ? View.VISIBLE : View.INVISIBLE);
		tv_take.setVisibility(CHAT_STATE == CHATSTATE_TAKE ? View.VISIBLE : View.INVISIBLE);
		tv_busy.setVisibility(CHAT_STATE == CHATSTATE_BUSY ? View.VISIBLE : View.INVISIBLE);

		bt_accept.setVisibility(CHAT_STATE == CHATSTATE_TAKE ? View.VISIBLE : View.INVISIBLE);
	}

	private void initView()
	{
		bt_accept = (Button) findViewById(R.id.bt_accept);
		bt_hangup = (Button) findViewById(R.id.bt_hangup);
		sv_video = (SurfaceView) findViewById(R.id.sv_video);

		tv_call = (TextView) findViewById(R.id.tv_call);
		tv_take = (TextView) findViewById(R.id.tv_take);
		tv_busy = (TextView) findViewById(R.id.tv_busy);
		bt_accept.setOnClickListener(this);
		bt_hangup.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_accept:

			VideoChatParam videoParam = null;// new VideoChatParam(sv_video, 0);
			AVChatManager.getInstance().toggleLocalVideo(true, null);
			AVChatManager.getInstance().accept(videoParam, new AVChatCallback<Void>()
			{

				@Override
				public void onException(Throwable arg0)
				{
					Log.i("接受异常");
				}

				@Override
				public void onFailed(int arg0)
				{
					Log.i("接受失败");
				}

				@Override
				public void onSuccess(Void arg0)
				{
					bt_accept.setEnabled(false);
					CHAT_STATE = CHATSTATE_BUSY;
					resetGUI();
				}
			});
			break;
		case R.id.bt_hangup:
			// 拒绝来电,或者在挂断通话
			AVChatManager.getInstance().hangUp(new AVChatCallback<Void>()
			{

				@Override
				public void onSuccess(Void arg0)
				{
					finish();
				}

				@Override
				public void onFailed(int arg0)
				{
					Log.i("挂断失败");
				}

				@Override
				public void onException(Throwable arg0)
				{
					Log.i("挂断异常");
				}
			});
			break;
		default:
			break;
		}

	}
}
