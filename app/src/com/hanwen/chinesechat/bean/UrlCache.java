package com.hanwen.chinesechat.bean;

public class UrlCache
{
	public UrlCache(String url, String result, long at)
	{
		this.Url=url;this.Json=result;this.UpdateAt=at;
	}
	public UrlCache()
	{
		// TODO Auto-generated constructor stub
	}
	public String Url;
	public String Json;
	public long UpdateAt;
}
