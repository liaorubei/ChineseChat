package com.hanwen.chinesechat.bean;

import java.util.List;

public class Document
{
	public int Id;
	public int LevelId;
	public int FolderId;
	public String Title;
	public String TitleTwo;
	public List<Lyric> Lyrics;
	public String SoundPath;
	public double Duration;
	public long Length;
	/**
	 * 播放时长
	 */
	public String LengthString;
	public String DateString;

	@Override
	public String toString()
	{
		return "Document [Id=" + Id + ", LevelId=" + LevelId + ", FolderId=" + FolderId + ", Title=" + Title + ", TitleTwo=" + TitleTwo + ", Lyrics=" + Lyrics + ", SoundPath=" + SoundPath + ", Duration=" + Duration + ", Length=" + Length + ", LengthString=" + LengthString + ", DateString=" + DateString + "]";
	}

}
