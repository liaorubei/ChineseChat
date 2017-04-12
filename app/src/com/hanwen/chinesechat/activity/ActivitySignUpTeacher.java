package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.dialog.ProgressDialog;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.DeviceUtil;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.util.TextUtil;
import com.yuyh.library.imgsel.ImageLoader;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivitySignUpTeacher extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final String TAG = "ActivitySignUpTeacher";
    private static final int WHAT_FAILURE = 0;
    private static final int WHAT_SUCCESS = 1;
    private EditText et_username, et_password, et_repassword, et_realname, et_mobile, et_wechat, et_school, et_language, et_length;
    private View tv_error_username, tv_error_password, tv_error_repassword, tv_error_realname, tv_error_school, tv_error_language;
    private View tv_error_avatar, tv_error_card_a, tv_error_card_b, tv_error_proofs;
    private ImageView iv_avatar, iv_proofs, iv_card_a, iv_card_b, iv_others;
    private File fileAvatar, fileCard_A, fileCard_B, fileProofs, fileRecord, fileOthers;
    private ScrollView scrollView;
    private ProgressDialog dialog;
    private ImgSelConfig config;
    private Call call;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SUCCESS:
                    CommonUtil.toast("申请提交成功，请注意查看邮箱");
                    finish();
                    break;
                case WHAT_FAILURE:
                    CommonUtil.toast(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_teacher);

        // 自由配置选项
        ImageLoader loader = new ImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                CommonUtil.showBitmap(imageView, path);
            }
        };
        config = new ImgSelConfig.Builder(this, loader)
                .multiSelect(false)// 是否多选, 默认true
                .rememberSelected(false)// 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                .maxNum(9)// 最大选择图片数量，默认9
                .btnBgColor(Color.GRAY)// “确定”按钮背景色
                .btnTextColor(Color.BLUE)// “确定”按钮文字颜色
                .statusBarColor(Color.parseColor("#3F51B5"))// 使用沉浸式状态栏
                .backResId(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha)// 返回图标ResId
                .title("图片")// 标题
                .titleColor(Color.WHITE)// 标题文字颜色
                .titleBgColor(Color.parseColor("#3F51B5"))  // TitleBar背景色
                .needCrop(false)
                .cropSize(1, 1, 200, 200)// 裁剪大小。needCrop为true的时候配置
                .needCamera(true)// 第一个是否显示相机，默认true
                .build();

        findView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    private void findView() {
        findViewById(R.id.iv_home).setOnClickListener(this);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_repassword = (EditText) findViewById(R.id.et_repassword);
        et_realname = (EditText) findViewById(R.id.et_realname);
        et_mobile = (EditText) findViewById(R.id.et_mobile);
        et_wechat = (EditText) findViewById(R.id.et_wechat);
        et_school = (EditText) findViewById(R.id.et_school);
        et_language = (EditText) findViewById(R.id.et_language);
        et_length = (EditText) findViewById(R.id.et_length);

        //检查数据完整性回调
        et_username.setOnFocusChangeListener(this);
        et_password.setOnFocusChangeListener(this);
        et_repassword.setOnFocusChangeListener(this);
        et_realname.setOnFocusChangeListener(this);
        et_school.setOnFocusChangeListener(this);
        et_language.setOnFocusChangeListener(this);

        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        iv_proofs = (ImageView) findViewById(R.id.iv_proofs);
        iv_card_a = (ImageView) findViewById(R.id.iv_card_a);
        iv_card_b = (ImageView) findViewById(R.id.iv_card_b);
        iv_others = (ImageView) findViewById(R.id.iv_others);

        //图片缩放监听
        iv_avatar.setOnClickListener(this);
        iv_proofs.setOnClickListener(this);
        iv_card_a.setOnClickListener(this);
        iv_card_b.setOnClickListener(this);
        iv_others.setOnClickListener(this);

        //图片选择监听
        findViewById(R.id.bt_avatar).setOnClickListener(this);
        findViewById(R.id.bt_card_a).setOnClickListener(this);
        findViewById(R.id.bt_card_b).setOnClickListener(this);
        findViewById(R.id.bt_proofs).setOnClickListener(this);
        findViewById(R.id.bt_record).setOnClickListener(this);
        findViewById(R.id.bt_others).setOnClickListener(this);

        //异常提示控件
        tv_error_username = findViewById(R.id.tv_error_username);
        tv_error_password = findViewById(R.id.tv_error_password);
        tv_error_repassword = findViewById(R.id.tv_error_repassword);
        tv_error_realname = findViewById(R.id.tv_error_realname);
        tv_error_school = findViewById(R.id.tv_error_school);
        tv_error_language = findViewById(R.id.tv_error_language);

        tv_error_avatar = findViewById(R.id.tv_error_avatar);
        tv_error_card_a = findViewById(R.id.tv_error_card_a);
        tv_error_card_b = findViewById(R.id.tv_error_card_b);
        tv_error_proofs = findViewById(R.id.tv_error_proofs);

        findViewById(R.id.bt_submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.bt_avatar:
            case R.id.bt_card_a:
            case R.id.bt_card_b:
            case R.id.bt_proofs:
            case R.id.bt_others:
                //选取图片
            {
/*                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image*//*");
                startActivityForResult(intent, v.getId());*/
                ImgSelActivity.startActivity(this, config, v.getId());
            }
            break;
            case R.id.bt_record:
                // 跳转到图片选择器
                ImgSelActivity.startActivity(this, config, 500);
                break;

            case R.id.iv_avatar:
                if (fileAvatar == null || !fileAvatar.exists()) {
                    return;
                }
                ActivityAlbum.start(this, new String[]{fileAvatar.getAbsolutePath()}, 0);
                break;
            case R.id.iv_proofs:
                if (fileProofs == null || !fileProofs.exists()) {
                    return;
                }
                ActivityAlbum.start(this, new String[]{fileProofs.getAbsolutePath()}, 0);
                break;
            case R.id.iv_card_a:
                if (fileCard_A == null || !fileCard_A.exists()) {
                    return;
                }
                ActivityAlbum.start(this, new String[]{fileCard_A.getAbsolutePath()}, 0);
                break;
            case R.id.iv_card_b:
                if (fileCard_B == null || !fileCard_B.exists()) {
                    return;
                }
                ActivityAlbum.start(this, new String[]{fileCard_B.getAbsolutePath()}, 0);
                break;
            case R.id.iv_others:
                if (fileOthers == null || !fileOthers.exists()) {
                    return;
                }
                ActivityAlbum.start(this, new String[]{fileOthers.getAbsolutePath()}, 0);
                break;
            case R.id.bt_submit:
                //检查所有的上传参数
                boolean isPass = true;
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                String realname = et_realname.getText().toString();
                String language = et_language.getText().toString();
                String mobile = et_mobile.getText().toString();
                String wechat = et_wechat.getText().toString();
                String school = et_school.getText().toString();
                String length = et_length.getText().toString();

                if (!TextUtil.isEmail(username)) {
                    //Toast.makeText(this, "用户名要求使用邮箱", Toast.LENGTH_SHORT).show();
                    tv_error_username.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                if (TextUtils.isEmpty(password) || password.length() < 8) {
                    //Toast.makeText(this, "密码不能少于8位", Toast.LENGTH_SHORT).show();
                    tv_error_password.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                if (!TextUtils.equals(password, et_repassword.getText())) {
                    //Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    tv_error_repassword.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                if (TextUtils.isEmpty(realname)) {
                    //Toast.makeText(this, "请输入你的真实姓名", Toast.LENGTH_SHORT).show();
                    tv_error_realname.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                if (TextUtils.isEmpty(school)) {
                    //Toast.makeText(this, "请输入你的毕业院校", Toast.LENGTH_SHORT).show();
                    tv_error_school.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                if (TextUtils.isEmpty(language)) {
                    //Toast.makeText(this, "请输入你掌握的外语", Toast.LENGTH_SHORT).show();
                    tv_error_language.setVisibility(View.VISIBLE);
                    isPass = false;
                }

                //---------------------------------文件------------------------------------------
                if (fileAvatar == null || !fileAvatar.exists()) {
                    //Toast.makeText(this, "请选择你的头像照片", Toast.LENGTH_SHORT).show();
                    tv_error_avatar.setVisibility(View.VISIBLE);
                    isPass = false;
                }
                if (fileCard_A == null || !fileCard_A.exists()) {
                    //Toast.makeText(this, "请选择身份证的反面", Toast.LENGTH_SHORT).show();
                    tv_error_card_a.setVisibility(View.VISIBLE);
                    isPass = false;
                }
                if (fileCard_B == null || !fileCard_B.exists()) {
                    //Toast.makeText(this, "请选择身份证的正面", Toast.LENGTH_SHORT).show();
                    tv_error_card_b.setVisibility(View.VISIBLE);
                    isPass = false;
                }
                if (fileProofs == null || !fileProofs.exists()) {
                    //Toast.makeText(this, "请选择你的学历证明", Toast.LENGTH_SHORT).show();
                    tv_error_proofs.setVisibility(View.VISIBLE);
                    isPass = false;
                }
/*                if (!fileRecord.exists()) {
                    Toast.makeText(this, "请输入你掌握的外语", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                if (!isPass) {
                    Toast.makeText(this, "数据验证不通过，请修正后再试", Toast.LENGTH_SHORT).show();
                    //scrollView.scrollTo(0, 0);
                    return;
                }
                //
/*                else {
                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                    if (isPass) {
                        return;
                    }
                }*/


                dialog = new ProgressDialog(ActivitySignUpTeacher.this);
                dialog.show();

                MediaType mediaType = MediaType.parse("image/*");
                MultipartBody.Builder body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)//如果想要提交成功必须设置此属性，不然表单都提交不上去，真实情况
                        .addFormDataPart("username", username).addFormDataPart("password", password)
                        .addFormDataPart("realname", realname).addFormDataPart("language", language)
                        .addFormDataPart("mobile", mobile).addFormDataPart("wechat", wechat)
                        .addFormDataPart("school", school).addFormDataPart("length", length)
                        .addFormDataPart("avatar", "avatar.jpg", RequestBody.create(mediaType, fileAvatar))
                        .addFormDataPart("card_a", "card_a.jpg", RequestBody.create(mediaType, fileCard_A))
                        .addFormDataPart("card_b", "card_b.jpg", RequestBody.create(mediaType, fileCard_B))
                        .addFormDataPart("proofs", "proofs.jpg", RequestBody.create(mediaType, fileProofs))
                        .addFormDataPart("device", new Gson().toJson(new DeviceUtil()));
                //添加其他证明材料
                if (fileOthers != null && fileOthers.exists()) {
                    body.addFormDataPart("others", "others.jpg", RequestBody.create(mediaType, fileOthers));
                }
                //生成请求
                Request request = new Request.Builder().url(NetworkUtil.nimUserTeacherRegister).post(body.build()).build();
                call = new OkHttpClient.Builder().build().newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        dialog.hide();
                        Message message = handler.obtainMessage(WHAT_FAILURE);
                        message.obj = "申请提交失败，请重新再试";
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "onResponse: " + response);
                        if (response.isSuccessful()) {
                            com.hanwen.chinesechat.bean.Response resp = new Gson().fromJson(response.body().string(), com.hanwen.chinesechat.bean.Response.class);
                            if (resp.code == 200) {
                                handler.sendEmptyMessage(WHAT_SUCCESS);
                            } else {
                                Message msg = handler.obtainMessage();
                                msg.what = WHAT_FAILURE;
                                msg.obj = String.format("申请提交失败,%1$s", resp.desc);
                                handler.sendMessage(msg);
                            }
                        }
                        dialog.hide();
                    }
                });
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {


        //只有在控件失去焦点的时候才进行验证
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.et_username:
                    String e = et_username.getText().toString();
                    if (!TextUtil.isEmail(e)) {
                        tv_error_username.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.et_password:
                    String p = et_password.getText().toString();
                    if (TextUtils.isEmpty(p) || p.length() < 8) {
                        tv_error_password.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.et_repassword:
                    String rp = et_repassword.getText().toString();
                    if (TextUtils.isEmpty(rp) || rp.length() < 8 || !TextUtils.equals(rp, et_password.getText())) {
                        tv_error_repassword.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.et_realname:
                    String r = et_realname.getText().toString();
                    if (TextUtils.isEmpty(r)) {
                        tv_error_realname.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.et_school:
                    String s = et_school.getText().toString();
                    if (TextUtils.isEmpty(s)) {
                        tv_error_school.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.et_language:
                    String l = et_language.getText().toString();
                    if (TextUtils.isEmpty(l)) {
                        tv_error_language.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.et_username:
                    tv_error_username.setVisibility(View.INVISIBLE);
                    break;
                case R.id.et_password:
                    tv_error_username.setVisibility(View.INVISIBLE);
                    break;
                case R.id.et_repassword:
                    tv_error_repassword.setVisibility(View.INVISIBLE);
                    break;
                case R.id.et_realname:
                    tv_error_realname.setVisibility(View.INVISIBLE);
                    break;
                case R.id.et_school:
                    tv_error_school.setVisibility(View.INVISIBLE);
                    break;
                case R.id.et_language:
                    tv_error_language.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case R.id.bt_avatar:
                if (resultCode == Activity.RESULT_OK) {
                    List<String> result = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT);
                    String path = result.get(0);
                    if (!TextUtils.isEmpty(path)) {
                        fileAvatar = new File(path);
                        CommonUtil.showBitmap(iv_avatar, path);
                        tv_error_avatar.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            case R.id.bt_card_a:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
                    if (!TextUtils.isEmpty(path)) {
                        fileCard_A = new File(path);
                        CommonUtil.showBitmap(iv_card_a, path);
                        tv_error_card_a.setVisibility(View.INVISIBLE);
                        // tv_card_a.setText(fileCard_A.getName());
                    }
                }
                break;
            case R.id.bt_card_b:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
                    if (!TextUtils.isEmpty(path)) {
                        fileCard_B = new File(path);
                        CommonUtil.showBitmap(iv_card_b, path);
                        tv_error_card_b.setVisibility(View.INVISIBLE);
                        //  tv_card_b.setText(fileCard_B.getName());
                    }
                }
                break;
            case R.id.bt_proofs:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
                    if (!TextUtils.isEmpty(path)) {
                        fileProofs = new File(path);
                        CommonUtil.showBitmap(iv_proofs, path);
                        tv_error_proofs.setVisibility(View.INVISIBLE);
                        //  tv_proofs.setText(fileProofs.getName());
                    }
                }
                break;
            case R.id.bt_others:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT).get(0);
                    if (!TextUtils.isEmpty(path)) {
                        fileOthers = new File(path);
                        CommonUtil.showBitmap(iv_others, path);
                        // tv_others.setText(fileOthers.getName());
                    }
                }
                break;
            case 500:
                if (resultCode == Activity.RESULT_OK) {
                    List<String> stringArrayExtra = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT);
                    for (String s : stringArrayExtra) {
                        Log.i(TAG, "onActivityResult: " + s);
                    }
                }
                break;
        }
    }
}
