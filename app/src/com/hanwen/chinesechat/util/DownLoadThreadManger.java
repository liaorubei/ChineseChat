package com.hanwen.chinesechat.util;

import android.content.Context;

import com.hanwen.chinesechat.util.DownloadInterface.OnDownloadListener;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DownLoadThreadManger implements OnDownloadListener {
    public static final String ACTION = "com.ultrapower.android.me.DownLoad";
    public final static int MAXTHREADCOUNT = 3;
    public final static int undownload = 0;
    public final static int downloading = 0x01;// 更新状态栏的下载进度
    public final static int downloadSuccess = 0x02;// 下载成功
    public final static int downloadError = 0x03;// 下载失败
    public final static int downloadStop = 0x04;// 下载暂停
    public final static int hadInstalled = 0x99;//已经安装

    private HashMap<String, DownLoadThread> dowloadingMap;


    private ArrayList<OnDownloadListener> onDownloadListenerList;
    private Context mContext;

    public DownLoadThreadManger(Context mContext) {
        this.mContext = mContext;
        onDownloadListenerList = new ArrayList<OnDownloadListener>();
        dowloadingMap = new HashMap<String, DownLoadThread>();
    }


    /**
     * @param db
     */
    public void addDownLoadMission(DownloadBean db) {
        if (dowloadingMap.containsKey(db.getUrl())) {
            //已经在下载队列中
//			dowloadingMap.remove(db.getUrl());
            return;
        }
        DownLoadThread downLoadThread = new DownLoadThread();
        downLoadThread.setDownloadBean(db);
        downLoadThread.setOnDownloadListener(this);
        dowloadingMap.put(db.getUrl(), downLoadThread);
        downLoadThread.start();
    }

    /**
     * 暂停下载
     *
     * @param db
     */
    public void stopDownLoadMission(DownloadBean db) {
        if (!dowloadingMap.containsKey(db.getUrl())) {
            //错误
            return;
        }
        DownLoadThread downLoadThread = dowloadingMap.get(db.getUrl());
        downLoadThread.setFinish(true);
    }

    public void addOnDownloadListener(OnDownloadListener odl) {
        if (onDownloadListenerList != null) {
            if (odl != null && !onDownloadListenerList.contains(odl)) {
                onDownloadListenerList.add(odl);
            }
        }
    }

    public void removeOnDownloadListener(OnDownloadListener odl) {
        if (onDownloadListenerList != null) {
            if (odl != null && onDownloadListenerList.contains(odl)) {
                onDownloadListenerList.remove(odl);
            }
        }
    }

    @Override
    public void onDownloadThreadStart(DownloadBean tmpDownloadBean) {
        // TODO Auto-generated method stub
        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.onDownloadThreadStart(tmpDownloadBean);
        }
    }

    @Override
    public void onFirstDownload(DownloadBean tmpDownloadBean) {
        // TODO Auto-generated method stub
        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.onFirstDownload(tmpDownloadBean);
        }
    }

    @Override
    public void onDownload(DownloadBean tmpDownloadBean, String progress) {
        // TODO Auto-generated method stub
        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.onDownload(tmpDownloadBean, progress);
        }
    }

    @Override
    public void onFinished(DownloadBean tmpDownloadBean) {
        // TODO Auto-generated method stub
        dowloadingMap.remove(tmpDownloadBean.getUrl());

        File oldFile = new File(tmpDownloadBean.getSavePath());
        File newFile = new File(DownLoadManger.SDCARDPATH
                + tmpDownloadBean.getFileName());
        oldFile.renameTo(newFile);

        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.onFinished(tmpDownloadBean);
        }
    }

    @Override
    public void onStop(DownloadBean tmpDownloadBean) {
        // TODO Auto-generated method stub
        dowloadingMap.remove(tmpDownloadBean.getUrl());
        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.onStop(tmpDownloadBean);
        }
    }

    @Override
    public void OnError(DownloadBean tmpDownloadBean, String error) {
        // TODO Auto-generated method stub
        dowloadingMap.remove(tmpDownloadBean.getUrl());
        for (OnDownloadListener onDownloadListener : onDownloadListenerList) {
            if (onDownloadListener != null)
                onDownloadListener.OnError(tmpDownloadBean, error);
        }
    }

    public static double getProgress(float downSize, float totalReadSize) {
        if (totalReadSize <= 0) {
            return 0;
        }
        DecimalFormat format = new DecimalFormat("0.00");
        float size = downSize * 100 / totalReadSize;
        return Double.parseDouble(format.format(size));
    }
}
