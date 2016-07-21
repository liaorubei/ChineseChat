package com.hanwen.chinesechat.bean;

/**
 * Created by 儒北 on 2016-06-13.
 */
public class Summary {
    public int month;
    public int count;
    public int duration;

    @Override
    public String toString() {
        return "Summary{" +
                "month=" + month +
                ", count=" + count +
                ", duration=" + duration +
                '}';
    }
}
