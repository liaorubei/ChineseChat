package com.hanwen.chinesechat.bean;

import java.util.List;

public class Answer
{
	public String desc;
	public Integer code;
	public Info info;

	public class Info
	{
		public String accid;
		public String token;
		public String name;
		public String username;
		public String password;
		@Override
		public String toString()
		{
			return "Info [accid=" + accid + ", token=" + token + ", name=" + name + ", username=" + username + ", password=" + password + "]";
		}	
		
		public List<People> others;
	}

	@Override
	public String toString()
	{
		return "Answer [desc=" + desc + ", code=" + code + ", info=" + info + "]";
	}
	
	public class People{
		
		public String AccId;
		public String NickName;
	}
}
