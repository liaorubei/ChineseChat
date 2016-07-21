package com.hanwen.chinesechat.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.hanwen.chinesechat.activity.ActivityCall;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.HttpUtil.Parameters;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 拨打界面
 * @author liaorubei
 *
 */
public class ContentViewRandom extends ContentView implements OnClickListener
{

	private static final String TAG = "ContentViewRandom";
	private Context mContext;
	private Button bt_call;
	private Gson gson = new Gson();
	private View inflate;

	public ContentViewRandom(Context context)
	{
		super(context);
		this.mContext = context;
		initData();
	}

	@Override
	public View onCreateSuccessView()
	{
		inflate = View.inflate(getContext(), R.layout.contentview_random, null);
		bt_call = (Button) inflate.findViewById(R.id.bt_call);

		bt_call.setOnClickListener(this);
		return inflate;
	}

	@Override
	public void initData()
	{
		showView(ViewState.SUCCESS);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bt_call:
			if (NIMClient.getStatus() != StatusCode.LOGINED)
			{
				getContext().startActivity(new Intent(getContext(), ActivitySignIn.class));
				return;
			}

			bt_call.setEnabled(false);
			Parameters parameters = new Parameters();
			parameters.add("id", mContext.getSharedPreferences("user", Context.MODE_PRIVATE).getInt("id", 0) + "");
			HttpUtil.post(NetworkUtil.ObtainTeacher, parameters, new RequestCallBack<String>()
			{

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo)
				{
					bt_call.setEnabled(true);

					Response<User> response = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>()
					{}.getType());

					if (response.code == 200)
					{
						Intent intent = new Intent(mContext, ActivityCall.class);
						intent.putExtra(ActivityCall.KEY_TARGET_ID, response.info.Id);
						intent.putExtra(ActivityCall.KEY_TARGET_ACCID, response.info.Accid);
						intent.putExtra(ActivityCall.KEY_TARGET_NICKNAME, response.info.Nickname);
						mContext.startActivity(intent);
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
					CommonUtil.toast("获取教师失败");
				}
			});
			break;

		default:
			break;
		}

	}

}
