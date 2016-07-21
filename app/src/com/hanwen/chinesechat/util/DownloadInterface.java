package com.hanwen.chinesechat.util;

public interface DownloadInterface {
	
	public void setFinish(boolean finish);
	
	public boolean getFinish();
	
	/*
	 * 设置文件下载地址
	 * @param url 文件下载地址
	 */
	public void setDownPath(String url);
	
	/*
	 * 设置文件保存路径
	 * @param path 本地保存路径
	 */
	public void setSavePath(String path);
	
	/*
	 * 设置下载缓存大小
	 * @param size (单位字节)
	 */
	public void setCacheSize(int size);//
	
	public void setRange(int range);
	
	/*
	 * 下载文件
	 */
	public void download() throws Exception;

	public interface OnDownloadListener{
		/**
		 * 新的下载线程
		 * @parem downSize 下载长度
		 */
		public void onDownloadThreadStart(DownloadBean tmpDownloadBean);
		
		/**
		 * 第一次下用来获取长度
		 * @parem downSize 下载长度
		 */
		public void onFirstDownload(DownloadBean tmpDownloadBean);
		/**
		 * 下载中
		 * @parem downSize 下载长度
		 */
		public void onDownload(DownloadBean tmpDownloadBean, String progress);//下载监听
		
		/**
		 * 下载完成
		 * @param downsize 下载长度
		 * @param url 文件下载地址
		 * @param path 本地保存路径
		 */
		public void onFinished(DownloadBean tmpDownloadBean);
		
		/**
		 * 下载停止
		 * @param downsize 下载长度
		 * @param url 文件下载地址
		 * @param path 本地保存路径
		 */
		public void onStop(DownloadBean tmpDownloadBean);
		
		/**
		 * 下载出错
		 */
		public void OnError(DownloadBean tmpDownloadBean, String error);
	}
	
	/*
	 * 下载地址无响应监听器
	 */
	public interface NoDownloadListener{
		
		/*
		 * 没有响应事件
		 * @param code 服务器返回的响应代码
		 */
		public void noDownload(int code);
		
	}
}
