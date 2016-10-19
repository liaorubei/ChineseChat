package com.hanwen.chinesechat.util;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpUtil {
    public static class Parameters {

        private Map<String, String> map;

        public Parameters() {
            map = new HashMap<String, String>();
        }

        public void add(String key, Object value) {
            this.map.put(key, "" + value);
        }

        public Set<Entry<String, String>> get() {
            return this.map.entrySet();
        }
    }

    public static void post(String url, Parameters parameters, RequestCallBack<String> callBack) {
        RequestParams params = new RequestParams();
        if (parameters != null) {
            Set<Entry<String, String>> set = parameters.get();
            for (Entry<String, String> entry : set) {
                params.addBodyParameter(entry.getKey(), entry.getValue());
            }
        }
        new HttpUtils().send(HttpMethod.POST, url, params, callBack);
    }
}
