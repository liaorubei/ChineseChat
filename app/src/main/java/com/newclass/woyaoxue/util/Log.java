package com.newclass.woyaoxue.util;

public class Log
{

	private static boolean logOn = true;

	public static void i(String tag, String msg)
	{
		if (logOn)
		{
			android.util.Log.i("logi:" + tag, msg);
		}
	}

	public static void i(Object msg)
	{
		if (logOn)
		{
			android.util.Log.i("logi", "" + msg);
		}
	}

}
