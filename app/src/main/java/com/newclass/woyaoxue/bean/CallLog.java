package com.newclass.woyaoxue.bean;

import java.util.Date;
import java.util.List;

public class CallLog
{
	public String Id;

	public Theme Theme;
	public String Target = "东方不败";
	public User Teacher;
	public User Sutdent;
	public Date Start;
	public Date Finish;
	public List<Theme> Themes;
	public int Score;
}
