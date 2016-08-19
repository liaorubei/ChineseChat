package com.hanwen.chinesechat.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Created by 儒北 on 2016-08-10.
 */
public class GsonUtil {

    private static GsonUtil instance;
    private Gson gson;

    private GsonUtil() {
        gson = new Gson();
    }

    public static GsonUtil Instance() {
        if (instance == null) {
            instance = new GsonUtil();
        }
        return instance;
    }


    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, (Type) classOfT);
    }

    public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}
