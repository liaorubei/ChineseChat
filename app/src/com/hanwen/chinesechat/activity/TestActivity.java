package com.hanwen.chinesechat.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.fragment.FragmentChatHskk;
import com.hanwen.chinesechat.fragment.FragmentCourse;
import com.hanwen.chinesechat.fragment.FragmentCourseNest;
import com.hanwen.chinesechat.fragment.FragmentHaveDownloaded;
import com.hanwen.chinesechat.fragment.FragmentNews;
import com.hanwen.chinesechat.fragment.FragmentTopics;
import com.hanwen.chinesechat.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class TestActivity extends FragmentActivity {
    private static final String TAG = "TestActivity";
    private ListView listView;

    String[] strings = new String[100];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = "你好：" + i;
        }


        try {
            BufferedReader r = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory(), "json.txt")));

            String json = r.readLine();

            JsonModel<List<Data>> o = new Gson().fromJson(json, new TypeToken<JsonModel<List<Data>>>() {}.getType());
            for (Data d : o.Data) {
                Log.i(TAG, "onCreate: " + d);
            }

            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "onCreate: ");

        Intent intent = new Intent(getApplicationContext(), ActivityChat.class);
        intent.putExtra(ActivityChat.KEY_CHAT_MODE, ActivityChat.CHAT_MODE_INCOMING);
        // startActivity(intent);

/*        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(TAG, "onGlobalLayout: ");
                //listView.smoothScrollToPosition(strings.length);
            }
        });*/

    }

    class JsonModel<T> {
        /// <summary>
        /// 错误码(0表示无错误)
        /// </summary>
        private Integer Code = Integer.valueOf(0);

        /// <summary>
        /// 是否成功
        /// </summary>
        private Boolean Success = Boolean.FALSE;

        /// <summary>
        /// 错误信息
        /// </summary>
        public String[] Error = null;

        /// <summary>
        /// 消息S
        /// </summary>
        public String Message = null;

        /// <summary>
        /// Json数据
        /// </summary>
        public T Data = null;

        /// <summary>
        /// 服务器响应时间
        /// </summary>
        public Long ResponseTicks = Long.valueOf(0);


        public Integer getCode() {
            return Code;
        }

        public void setCode(Integer code) {
            Code = code;
        }

        public Boolean getSuccess() {
            return Success;
        }

        public void setSuccess(Boolean success) {
            Success = success;
        }

        public String[] getError() {
            return Error;
        }

        public void setError(String[] error) {
            Error = error;
        }

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            Message = message;
        }

        public T getData() {
            return Data;
        }

        public void setData(T data) {
            Data = data;
        }

        public Long getResponseTicks() {
            return ResponseTicks;
        }

        public void setResponseTicks(Long responseTicks) {
            ResponseTicks = responseTicks;
        }
    }

    class Data {

        private int LeiMuID;

        private String Name;

        private int CultureID;

        private String CultureName;

        private String CultureDescription;

        private String UrlName;

        private int ParentID;

        private String ImageJSON;

        private int Sort;

        private int IsShow;

        private String PriceRangeJSON;

        private int Status;

        private int Type;

        private String URL;

        private String PlatformJSON;

        private String CreateTime;

        private String UpdateTime;

        private String CreateUserName;

        private String UpdateUserName;

        private String _id;

        public void setLeiMuID(int LeiMuID) {
            this.LeiMuID = LeiMuID;
        }

        public int getLeiMuID() {
            return this.LeiMuID;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public String getName() {
            return this.Name;
        }

        public void setCultureID(int CultureID) {
            this.CultureID = CultureID;
        }

        public int getCultureID() {
            return this.CultureID;
        }

        public void setCultureName(String CultureName) {
            this.CultureName = CultureName;
        }

        public String getCultureName() {
            return this.CultureName;
        }

        public void setCultureDescription(String CultureDescription) {
            this.CultureDescription = CultureDescription;
        }

        public String getCultureDescription() {
            return this.CultureDescription;
        }

        public void setUrlName(String UrlName) {
            this.UrlName = UrlName;
        }

        public String getUrlName() {
            return this.UrlName;
        }

        public void setParentID(int ParentID) {
            this.ParentID = ParentID;
        }

        public int getParentID() {
            return this.ParentID;
        }

        public void setImageJSON(String ImageJSON) {
            this.ImageJSON = ImageJSON;
        }

        public String getImageJSON() {
            return this.ImageJSON;
        }

        public void setSort(int Sort) {
            this.Sort = Sort;
        }

        public int getSort() {
            return this.Sort;
        }

        public void setIsShow(int IsShow) {
            this.IsShow = IsShow;
        }

        public int getIsShow() {
            return this.IsShow;
        }

        public void setPriceRangeJSON(String PriceRangeJSON) {
            this.PriceRangeJSON = PriceRangeJSON;
        }

        public String getPriceRangeJSON() {
            return this.PriceRangeJSON;
        }

        public void setStatus(int Status) {
            this.Status = Status;
        }

        public int getStatus() {
            return this.Status;
        }

        public void setType(int Type) {
            this.Type = Type;
        }

        public int getType() {
            return this.Type;
        }

        public void setURL(String URL) {
            this.URL = URL;
        }

        public String getURL() {
            return this.URL;
        }

        public void setPlatformJSON(String PlatformJSON) {
            this.PlatformJSON = PlatformJSON;
        }

        public String getPlatformJSON() {
            return this.PlatformJSON;
        }

        public void setCreateTime(String CreateTime) {
            this.CreateTime = CreateTime;
        }

        public String getCreateTime() {
            return this.CreateTime;
        }

        public void setUpdateTime(String UpdateTime) {
            this.UpdateTime = UpdateTime;
        }

        public String getUpdateTime() {
            return this.UpdateTime;
        }

        public void setCreateUserName(String CreateUserName) {
            this.CreateUserName = CreateUserName;
        }

        public String getCreateUserName() {
            return this.CreateUserName;
        }

        public void setUpdateUserName(String UpdateUserName) {
            this.UpdateUserName = UpdateUserName;
        }

        public String getUpdateUserName() {
            return this.UpdateUserName;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String get_id() {
            return this._id;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "LeiMuID=" + LeiMuID +
                    ", Name='" + Name + '\'' +
                    ", CultureID=" + CultureID +
                    ", CultureName='" + CultureName + '\'' +
                    ", CultureDescription='" + CultureDescription + '\'' +
                    ", UrlName='" + UrlName + '\'' +
                    ", ParentID=" + ParentID +
                    ", ImageJSON='" + ImageJSON + '\'' +
                    ", Sort=" + Sort +
                    ", IsShow=" + IsShow +
                    ", PriceRangeJSON='" + PriceRangeJSON + '\'' +
                    ", Status=" + Status +
                    ", Type=" + Type +
                    ", URL='" + URL + '\'' +
                    ", PlatformJSON='" + PlatformJSON + '\'' +
                    ", CreateTime='" + CreateTime + '\'' +
                    ", UpdateTime='" + UpdateTime + '\'' +
                    ", CreateUserName='" + CreateUserName + '\'' +
                    ", UpdateUserName='" + UpdateUserName + '\'' +
                    ", _id='" + _id + '\'' +
                    '}';
        }
    }

}
