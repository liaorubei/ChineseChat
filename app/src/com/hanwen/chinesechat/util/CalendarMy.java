package com.hanwen.chinesechat.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Fylx on 2016/8/29.
 */
public class CalendarMy {

    private final Calendar calendar = Calendar.getInstance();
    private DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public CalendarMy set(int field, int value) {
        calendar.set(field, value);
        return this;
    }

    public String toString(String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.getTime());
    }

    @Override
    public String toString() {
        return simpleDateFormat.format(calendar.getTime());
    }

    public CalendarMy add(int field, int value) {
        calendar.add(field, value);
        return this;
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public CalendarMy set(int year, int month, int day, int hourOfDay, int minute, int second) {
        calendar.set(year, month, day, hourOfDay, minute, second);
        return this;
    }
}
