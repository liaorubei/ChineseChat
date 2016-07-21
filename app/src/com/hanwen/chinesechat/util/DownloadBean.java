package com.hanwen.chinesechat.util;

public class DownloadBean {

	private int drawableUninstalled; 
	private int drawableInstalled;
	private int downLoadType;  
	private String appName;  //河南IT产品支撑
	private String pageName;
	
	private String url;  //http:xxxx.henan.apk
	private String savePath;  //sdcard/me/ + fileName
	private String fileName;   //henanme.apk
	private int fileSize;
	private int range;
	
	
	public int getDrawableUninstalled() {
		return drawableUninstalled;
	}
	public void setDrawableUninstalled(int drawableUninstalled) {
		this.drawableUninstalled = drawableUninstalled;
	}
	public int getDrawableInstalled() {
		return drawableInstalled;
	}
	public void setDrawableInstalled(int drawableInstalled) {
		this.drawableInstalled = drawableInstalled;
	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	
	}
	public String getPageName() {
		return pageName;
	}
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSavePath() {
		return savePath;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.savePath = DownLoadManger.SDCARDPATH + this.fileName+".tmp";
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public int getRange() {
		return range;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public int getDownLoadType() {
		return downLoadType;
	}
	public void setDownLoadType(int downLoadType) {
		this.downLoadType = downLoadType;
	}
	public int getDrawable(){
		return downLoadType == DownLoadThreadManger.hadInstalled?drawableInstalled:drawableUninstalled;
	}
	
}
