package com.newclass.woyaoxue.util;

import com.newclass.woyaoxue.MyApplication;

import android.content.Context;
import android.widget.Toast;

public class CommonUtil
{

	public static void toast(Context context, String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void toast(String text)
	{
		toast(MyApplication.getContext(), text);
	}

	public static String millisecondsFormat(long milliseconds)
	{
		long minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (milliseconds % (1000 * 60)) / 1000;
		return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
	}

}
