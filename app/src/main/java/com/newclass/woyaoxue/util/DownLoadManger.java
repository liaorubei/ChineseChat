package com.newclass.woyaoxue.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

public class DownLoadManger {
	private DownLoadThreadManger downLoadThreadManger;

	public static final String SDCARDPATH = Environment
			.getExternalStorageDirectory().getPath() + "/" + "UltraME/";
	private Context mContext;

	public DownLoadManger(Context mContext) {
		this.mContext = mContext;
		downLoadThreadManger = new DownLoadThreadManger(mContext);
		File destDir = new File(SDCARDPATH);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
	}

	public boolean scanFileExist(DownloadBean downloadBean) {
		if (downloadBean.getDownLoadType() == DownLoadThreadManger.downloadSuccess) {
			File finishFile = new File(SDCARDPATH + downloadBean.getFileName());
			return finishFile.exists() ? true : false;
		} else {
			File tmpFile = new File(SDCARDPATH + downloadBean.getFileName()
					+ ".tmp");
			return tmpFile.exists() ? true : false;
		}
	}

	public boolean scanSuccessFileExist(DownloadBean downloadBean) {
		File finishFile = new File(SDCARDPATH + downloadBean.getFileName());
		return finishFile.exists() ? true : false;
	}

	public void delectFile(DownloadBean downloadBean) {
		if (downloadBean.getDownLoadType() == DownLoadThreadManger.downloadSuccess) {
			File finishFile = new File(SDCARDPATH + downloadBean.getFileName());
			if (finishFile.exists()) {
				finishFile.delete();
			}
		} else {
			File tmpFile = new File(SDCARDPATH + downloadBean.getFileName()
					+ ".tmp");
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
		}
	}

	// sdcard是否可读写
	public static boolean IsCanUseSdCard() {
		try {
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// Android获取一个用于打开APK文件的intent
	public static Intent getApkFileIntent(String param) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	/**
	 * 存在最少一个在下载中
	 * 
	 * @param downloadBeanList
	 * @param downloadBean
	 * @return
	 */
	public static boolean checkDownloadingExist(
			ArrayList<DownloadBean> downloadBeanList) {
		for (int i = 0; i < downloadBeanList.size(); i++) {
			if (downloadBeanList.get(i).getDownLoadType() == DownLoadThreadManger.downloading) {
				return true;
			}
		}
		return false;
	}

	/**
	 * * 获取url对应的文件名
	 * 
	 * @param pictureurl
	 * @return
	 */
	public static String getFileNameFromUrl(String pictureurl) {

		String regstr = "(http:|https:)\\/\\/[\\S\\.:/]*\\/(\\S*)\\.(\\S*)";
		String postfix = "", filename = "", resultname = "";
		Pattern patternForImg = Pattern.compile(regstr,
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = patternForImg.matcher(pictureurl);
		if (matcher.find()) {
			filename = matcher.group(2);
			postfix = matcher.group(3);
		}
		resultname = filename + "." + postfix;

		return resultname;
	}

	/**
	 * 根据包名跳转到app
	 * 
	 * @param pkg
	 * @param context
	 * @return
	 */
	public static boolean startAppByPackage(String pkg, Context context) {
		PackageManager packageManager = context.getPackageManager();
		Intent intent = null;
		intent = packageManager.getLaunchIntentForPackage(pkg);
		if (intent == null) {
			return false;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		return true;
	}

	

	public static  boolean startAppByActivity(String pkg, String activity,
			Context context) {
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager
				.getInstalledPackages(0);

		boolean isPresence = false;// 是否安装需要打开的软件

		for (int i = 0; i < packageInfoList.size(); i++) {
			PackageInfo pak = (PackageInfo) packageInfoList.get(i);
			if (pak.applicationInfo.packageName.equals(pkg)) {
				isPresence = true;
				break;
			}
		}

		if (isPresence) {// 有这个程序.
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			Intent intent = new Intent();

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName(pkg, activity));
			if (packageManager.resolveActivity(intent, 0) == null) {
				// 说明系统中不存在这个activity
				return false;
			}
			if (context.getPackageName().equals(pkg)) {
				try {
					Class.forName(activity);
				} catch (Exception e) {
					// 这是异常信息
					return false;
				}
			}
			context.startActivity(intent);
			return true;
		}
		return false;
	}
	
	
	public DownLoadThreadManger getDownLoadThreadManger() {
		if (downLoadThreadManger != null) {
			return downLoadThreadManger;
		} else {
			return new DownLoadThreadManger(mContext);
		}
	}

	public void setDownLoadThreadManger(
			DownLoadThreadManger downLoadThreadManger) {
		this.downLoadThreadManger = downLoadThreadManger;
	}
	
}
