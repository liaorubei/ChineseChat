package com.hanwen.chinesechat.bean;

import java.util.List;

/**
 * Created by 儒北 on 2016-06-17.
 */
public class TeacherQueue {
    public List<User> Teacher;
    public NewUser Current;

    public class NewUser {
        public int Id;
        public String Username;
        public int IsOnline;
        public int IsEnable;
        public int IsActive;
        public int IsQueue;
    }
}
