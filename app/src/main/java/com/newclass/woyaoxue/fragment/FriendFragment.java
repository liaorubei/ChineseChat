package com.newclass.woyaoxue.fragment;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.VideoChatParam;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FriendFragment extends Fragment implements OnClickListener
{

	private Button bt_call, bt_text;
	private SurfaceView sv_video;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("logi", "FriendFragment onCreate");
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View inflate = inflater.inflate(R.layout.fragment_random, null);
		initView(inflate);

		TextView textView = new TextView(getActivity());
		textView.setText("好友");
		return textView;
	}

	private void initView(View view)
	{
		sv_video = (SurfaceView) view.findViewById(R.id.sv_video);
		bt_call = (Button) view.findViewById(R.id.bt_call);
		bt_text = (Button) view.findViewById(R.id.bt_text);

		bt_call.setOnClickListener(this);
		bt_text.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_call:
			// 请求服务器

			String teacherAccid = "bf09f7dd02e549f4a16af0cf8e9a5701";
			// 发起网络通话,并监听是否拨通的通知

			VideoChatParam param = new VideoChatParam(sv_video, 0);
			AVChatManager.getInstance().call(teacherAccid, AVChatType.AUDIO, param, new AVChatCallback<AVChatData>()
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

			break;

		case R.id.bt_text:
			IMMessage message = MessageBuilder.createTextMessage("bf09f7dd02e549f4a16af0cf8e9a5701", SessionTypeEnum.P2P, "来自学生的消息");
			NIMClient.getService(MsgService.class).sendMessage(message, false);
			break;

		default:
			break;
		}
	}

}
