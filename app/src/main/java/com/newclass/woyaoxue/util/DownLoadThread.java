package com.newclass.woyaoxue.util;

import android.util.Log;

import com.newclass.woyaoxue.util.DownloadInterface.OnDownloadListener;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class DownLoadThread extends Thread {
    public static final int TIME_OUT = 10000;
    private static byte[] cache = new byte[10240];// 缓存10K
    private DecimalFormat format = new DecimalFormat("0.00");

    private DownloadBean downloadBean;
    private boolean finish = false;
    private boolean work = false;
    private DownloadInterface.OnDownloadListener onDownloadListener;

    @Override
    public void run() {
        // TODO Auto-generated method stub

        int downSize = downloadBean.getRange();
        boolean noDownload = false;// 下载失败标志
        work = true;
        downloadBean.setDownLoadType(DownLoadThreadManger.downloading);
        onDownloadListener.onDownloadThreadStart(downloadBean);
        try {
            URL url = new URL(downloadBean.getUrl());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(TIME_OUT);
            http.setRequestMethod("GET");
            http.setRequestProperty("Range", "bytes=" + downloadBean.getRange() + "-");
            http.setRequestProperty("Charset", "UTF-8");
            http.setRequestProperty("Connection", "Keep-Alive");
            http.connect();

            int code = http.getResponseCode();
            if (200 <= code && 300 >= code) {
                System.out.println("the service was responsed data");
                InputStream inStream;
                try {
                    inStream = http.getInputStream();
                } catch (Exception e) {
                    throw new Exception(
                            "get the url InputStream an error happend ");
                }
                int off = 0;
                File saveFile = new File(downloadBean.getSavePath());
                if (!saveFile.exists()) {
                    saveFile.getParentFile().mkdirs();
                    saveFile.createNewFile();
                }
                RandomAccessFile randomFile = new RandomAccessFile(new File(
                        downloadBean.getSavePath()), "rwd");
                int fileSize = http.getContentLength();
                if (fileSize != randomFile.length() && downloadBean.getRange() == 0) {
                    System.out.println("file length is not equal ");
                    randomFile.setLength(fileSize);
                }
                randomFile.seek(downloadBean.getRange());

                if (downloadBean.getRange() == 0) {
                    downloadBean.setFileSize(fileSize);
                    onDownloadListener.onFirstDownload(downloadBean);
                }
                while ((off = inStream.read(cache)) != -1) {
                    if (!finish) {
                        randomFile.seek(downSize);
                        randomFile.write(cache, 0, off);
                        downSize += off;
                        downloadBean.setRange(downSize);
                        onDownloadListener.onDownload(downloadBean,
                                progress(downSize, downloadBean.getFileSize()));
                    } else {
                        work = false;
                        downloadBean.setRange(downSize);
                        downloadBean.setDownLoadType(DownLoadThreadManger.downloadStop);
                        onDownloadListener.onStop(downloadBean);
                        break;
                    }
                }
                if (work) {
                    downloadBean.setDownLoadType(DownLoadThreadManger.downloadSuccess);
                    onDownloadListener.onFinished(downloadBean);

                }
                randomFile.close();
                inStream.close();
                http.disconnect();
            } else {
                System.out.println("ResponseCode = " + code);
                noDownload = true;
            }
        } catch (Exception e) {
            System.out.println("下载过程出错\n:" + e.toString());
            noDownload = true;
        }
        work = false;
        finish = false;
        if (noDownload) {
            Log.e("dz", "downSize = " + downSize + "   Range " + downloadBean.getRange());
            downloadBean.setRange(downSize);
            downloadBean.setDownLoadType(DownLoadThreadManger.downloadStop);
            onDownloadListener.OnError(downloadBean,
                    "the service has no response");
        }
    }

    public void setFinish(boolean finish) {
        this.finish = finish;

    }

    public boolean getFinish() {
        // TODO Auto-generated method stub
        return this.finish;
    }

    public boolean isWork() {
        return this.work;
    }

    private String progress(float downSize, float totalReadSize) {
        float size = (float) downSize * 100 / (float) totalReadSize;
        return format.format(size);
    }

    public DownloadInterface.OnDownloadListener getOnDownloadListener() {
        return onDownloadListener;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    public DownloadBean getDownloadBean() {
        return downloadBean;
    }

    public void setDownloadBean(DownloadBean downloadBean) {
        this.downloadBean = downloadBean;
    }
}
