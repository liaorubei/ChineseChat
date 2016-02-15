package com.newclass.woyaoxue.download;

import java.util.LinkedList;
import java.util.Observable;

import android.util.SparseArray;

import com.newclass.woyaoxue.bean.DownloadInfo;

public class DownloadManager extends Observable
{

	private static SparseArray<DownloadInfo> downloading = new SparseArray<DownloadInfo>();// 正在下载
	private static LinkedList<DownloadInfo> toDownload = new LinkedList<DownloadInfo>();// 准备下载

	public void change(int key, long current, long total)
	{
		DownloadInfo down = downloading.get(key);
		down.Current = current;
		down.Total = total;

		// 标识数据已经发生了改变
		setChanged();
	}

	public void enqueue(DownloadInfo path)
	{
		toDownload.add(path);
	}

	public static DownloadInfo get(int key)
	{
		DownloadInfo downloadInfo = downloading.get(key);
		if (downloadInfo == null)
		{
			for (DownloadInfo i : toDownload)
			{
				if (i.Id == key)
				{
					downloadInfo = i;
				}
			}
		}
		return downloadInfo;
	}

	public static int downloadingSize()
	{
		return downloading.size();
	}

	public static DownloadInfo download()
	{
		DownloadInfo remove = toDownload.remove();
		downloading.append(remove.Id, remove);
		return remove;
	}
}
