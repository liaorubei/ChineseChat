package com.newclass.woyaoxue.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.newclass.woyaoxue.MyApplication;
import com.newclass.woyaoxue.activity.SignInActivity;

import android.content.Intent;

public class HttpUtil
{

	public static class Parameters
	{

		private Map<String, String> map;

		public Parameters()
		{
			map = new HashMap<String, String>();

		}

		public void add(String key, String value)
		{
			this.map.put(key, value);
		}

		public Set<Entry<String, String>> get()
		{
			return this.map.entrySet();
		}

	}

	public static void post(String url, Parameters parameters, RequestCallBack<String> callBack)
	{
		RequestParams params = new RequestParams();
		if (parameters != null)
		{
			Set<Entry<String, String>> set = parameters.get();
			for (Entry<String, String> entry : set)
			{
				params.addBodyParameter(entry.getKey(), entry.getValue());
			}
		}
		new HttpUtils().send(HttpMethod.POST, url, params, callBack);
	}
}
