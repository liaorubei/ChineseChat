package com.hanwen.chinesechat.util;

public interface DownloadInterface {
	
	void setFinish(boolean finish);
	
	boolean getFinish();
	
	/*
	 * 设置文件下载地址
	 * @param url 文件下载地址
	 */
	void setDownPath(String url);
	
	/*
	 * 设置文件保存路径
	 * @param path 本地保存路径
	 */
	void setSavePath(String path);
	
	/*
	 * 设置下载缓存大小
	 * @param size (单位字节)
	 */
	void setCacheSize(int size);//
	
	void setRange(int range);
	
	/*
	 * 下载文件
	 */
	void download() throws Exception;

	interface OnDownloadListener{
		/**
		 * 新的下载线程
		 * @parem downSize 下载长度
		 */
		void onDownloadThreadStart(DownloadBean tmpDownloadBean);
		
		/**
		 * 第一次下用来获取长度
		 * @parem downSize 下载长度
		 */
		void onFirstDownload(DownloadBean tmpDownloadBean);
		/**
		 * 下载中
		 * @parem downSize 下载长度
		 */
		void onDownload(DownloadBean tmpDownloadBean, String progress);//下载监听
		
		/**
		 * 下载完成
		 * @param downsize 下载长度
		 * @param url 文件下载地址
		 * @param path 本地保存路径
		 */
		void onFinished(DownloadBean tmpDownloadBean);
		
		/**
		 * 下载停止
		 * @param downsize 下载长度
		 * @param url 文件下载地址
		 * @param path 本地保存路径
		 */
		void onStop(DownloadBean tmpDownloadBean);
		
		/**
		 * 下载出错
		 */
		void OnError(DownloadBean tmpDownloadBean, String error);
	}
	
	/*
	 * 下载地址无响应监听器
	 */
	interface NoDownloadListener{
		
		/*
		 * 没有响应事件
		 * @param code 服务器返回的响应代码
		 */
		void noDownload(int code);
		
	}
}
