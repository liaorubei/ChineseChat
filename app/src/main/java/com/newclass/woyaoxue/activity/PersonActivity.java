package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.FolderUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PersonActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PersonActivity";
    private LinearLayout ll_show, ll_edit;
    private TextView tv_nickname, tv_mobile, tv_email, tv_gender, tv_birth, tv_pick, tv_username, et_username;
    private EditText et_nickname, et_mobile, et_email, et_gender, et_birth;
    private RadioGroup rg_gender;
    private RadioButton rb_male, rb_female;
    private ImageView iv_avater, iv_edit, iv_pick;
    private boolean isEdit = false;
    private MenuItem menuItemCancel;
    private MenuItem menuItemHandle;
    private ProgressDialog saveDialog;
    private Gson gson = new Gson();
    private final int PHOTO_REQUEST_PICK = 50;
    private final int PHOTO_REQUEST_CROP = 51;
    private File tempIcon;
    private boolean isIconSwitch = false;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat sdf;
    private Bitmap tempBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        initView();
        initData();
        saveDialog = new ProgressDialog(this);
        tempIcon = new File(FolderUtil.rootDir(this), "temp_icon.PNG");

        createDatePickerDialog();

        sdf = new SimpleDateFormat("yyyy-MM-dd");
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        ViewModel model = new ViewModel();
        model.Name = sp.getString("nickname", "");
        model.Mobile = sp.getString("mobile", "");
        model.Email = sp.getString("email", "");
        model.Gender = sp.getInt("gender", -1);
        model.Birth = sp.getString("birth", "");
        model.Avater = sp.getString("avater", "");
        initShow(model);
        initEdit(model);
    }

    private void initEdit(ViewModel model) {
        new BitmapUtils(this).display(iv_edit, NetworkUtil.getFullPath(model.Avater));
        et_nickname.setText(model.Name);
        et_email.setText(model.Email);
        et_mobile.setText(model.Mobile);
        rg_gender.check(model.Gender == 0 ? R.id.rb_female : R.id.rb_male);
        et_birth.setText(model.Birth);
    }

    private void initShow(ViewModel model) {

        Log.i(TAG, "initShow: Avater=" + model.Avater);
        new BitmapUtils(this).display(iv_avater, NetworkUtil.getFullPath(model.Avater));
        tv_nickname.setText(model.Name);
        tv_email.setText(model.Email);
        tv_mobile.setText(model.Mobile);
        tv_gender.setText(model.Gender == 0 ? "女" : "男");
        tv_birth.setText(model.Birth);
    }

    private void initView() {

        ll_show = (LinearLayout) findViewById(R.id.ll_show);
        ll_edit = (LinearLayout) findViewById(R.id.ll_edit);

        tv_username = (TextView) findViewById(R.id.tv_username);
        et_username = (TextView) findViewById(R.id.et_username);

        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        tv_mobile = (TextView) findViewById(R.id.tv_mobile);
        tv_email = (TextView) findViewById(R.id.tv_email);
        tv_gender = (TextView) findViewById(R.id.tv_gender);
        tv_birth = (TextView) findViewById(R.id.tv_birth);
        iv_avater = (ImageView) findViewById(R.id.iv_avater);
        tv_pick = (TextView) findViewById(R.id.tv_pick);
        tv_pick.setOnClickListener(this);

        iv_edit = (ImageView) findViewById(R.id.iv_edit);
        iv_edit.setOnClickListener(this);

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        sp.getInt("id", -1);
        tv_nickname.setText(sp.getString("nickname", ""));
        tv_mobile.setText(sp.getString("mobile", ""));
        tv_email.setText(sp.getString("email", ""));
        tv_gender.setText(sp.getInt("gender", -1) == 0 ? "女" : "男");
        tv_birth.setText(sp.getString("birth", ""));
        tv_username.setText(sp.getString("username", ""));
        et_username.setText(sp.getString("username", ""));

        et_nickname = (EditText) findViewById(R.id.et_nickname);
        et_mobile = (EditText) findViewById(R.id.et_mobile);
        et_email = (EditText) findViewById(R.id.et_email);

        et_birth = (EditText) findViewById(R.id.et_birth);

        iv_pick = (ImageView) findViewById(R.id.iv_pick);
        iv_pick.setOnClickListener(this);
        rg_gender = (RadioGroup) findViewById(R.id.rg_gender);
        rb_male = (RadioButton) findViewById(R.id.rb_female);
        rb_female = (RadioButton) findViewById(R.id.rb_female);

        new BitmapUtils(this).display(iv_avater, NetworkUtil.getFullPath(sp.getString("avater", "")));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menuItemCancel = menu.add(menu.NONE, 1, 1, "取消");
        menuItemCancel.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItemCancel.setVisible(false);

        menuItemHandle = menu.add(Menu.NONE, 2, 2, "编辑");
        menuItemHandle.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 1:
                isEdit = false;
                switchMode();
                break;

            case 2: {
                if (isEdit) {
                    saveUserEx();
                }
                isEdit = !isEdit;
                switchMode();
            }
            break;


        }


        return true;
    }

    private void switchMode() {
        menuItemCancel.setVisible(isEdit);
        menuItemHandle.setTitle(isEdit ? "保存" : "编辑");
        ll_edit.setVisibility(isEdit ? View.VISIBLE : View.INVISIBLE);
        ll_show.setVisibility(isEdit ? View.INVISIBLE : View.VISIBLE);
    }

    private void saveUserEx() {


        saveDialog.show();
        com.lidroid.xutils.http.RequestParams params = new RequestParams();
        params.addBodyParameter("id", getSharedPreferences("user", MODE_PRIVATE).getInt("id", -1) + "");
        params.addBodyParameter("Name", et_nickname.getText().toString().trim());
        params.addBodyParameter("Email", et_email.getText().toString().trim());
        params.addBodyParameter("Mobile", et_mobile.getText().toString().trim());
        params.addBodyParameter("Gender", (rb_female.isChecked() ? 0 : 1) + "");
        params.addBodyParameter("Birth", et_birth.getText().toString().trim());
        if (isIconSwitch) {
            params.addBodyParameter("icon", tempIcon);
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.userUpdate, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (resp.code == 200) {
                    //保存用户信息到sp
                    SharedPreferences.Editor editor = PersonActivity.this.getSharedPreferences("user", MODE_PRIVATE).edit();
                    editor.putInt("id", resp.info.Id);
                    editor.putString("nickname", resp.info.Name);
                    editor.putString("avater", resp.info.Icon);
                    editor.putString("email", resp.info.Email);
                    editor.putString("birth", resp.info.Birth);
                    editor.putString("mobile", resp.info.Mobile);
                    editor.putInt("gender", resp.info.Gender);
                    editor.commit();

                    ViewModel model = new ViewModel();
                    model.Name = resp.info.Name;
                    model.Avater = resp.info.Icon;
                    model.Email = resp.info.Email;
                    model.Birth = resp.info.Birth;
                    model.Mobile = resp.info.Mobile;
                    model.Gender = resp.info.Gender;
                   // initShow(model);

                    if (tempBitmap != null) {
                        File avaterPNG = new File(getFilesDir(), model.Avater);
                        saveBitmap(tempBitmap, avaterPNG);
                    }

                } else {
                    CommonUtil.toast("修改失败");
                }


                saveDialog.dismiss();

                isEdit = false;
                switchMode();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                saveDialog.dismiss();
            }
        });

    }

    private void saveBitmap(Bitmap tempBitmap, File avaterPNG) {
        if (!avaterPNG.exists()) {
            if (!avaterPNG.getParentFile().exists()) {
                avaterPNG.getParentFile().mkdirs();
            }
            try {
                OutputStream stream = new FileOutputStream(avaterPNG);
                tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                Log.i(TAG, "saveBitmap: " + avaterPNG.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit:
                pick();
                break;
            case R.id.iv_pick:

                createDatePickerDialog();
                datePickerDialog.show();

                break;
        }
    }

    private void createDatePickerDialog() {
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //  Log.i(TAG, "onDateSet: year:" + year + " monthOfYear:" + monthOfYear + " dayOfMonth:" + dayOfMonth);
            }
        }, 2000, 1, 1);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar l = Calendar.getInstance();
                l.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth());
                et_birth.setText(sdf.format(l.getTime()));
            }
        });
    }


    // 选取图片
    public void pick() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_PICK);
    }

    //剪切图片
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);

        //intent.putExtra("scale", true);//黑边
        //intent.putExtra("scaleUpIfNeeded", true);//黑边

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);//要求返回数据
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PHOTO_REQUEST_PICK:
                // 从相册返回的数据
                if (data != null) {
                    // 得到图片的全路径
                    Uri uri = data.getData();
                    crop(uri);
                }
                break;
            case PHOTO_REQUEST_CROP:
                // 从剪切图片返回的数据
                if (data != null) {
                    try {
                        tempBitmap = data.getParcelableExtra("data");
                        OutputStream out = new FileOutputStream(tempIcon);
                        tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        this.iv_edit.setImageBitmap(tempBitmap);
                        this.iv_avater.setImageBitmap(tempBitmap);
                        out.close();
                        isIconSwitch = true;
                    } catch (Exception e) {
                        isIconSwitch = false;
                        CommonUtil.toast("图片裁剪保存时出错");
                    }
                }
                break;
        }


        //   super.onActivityResult(requestCode, resultCode, data);
    }


    private class ViewModel {
        public String Icon;
        public String Name;
        public String Email;

        public String Birth;
        public String Mobile;
        public int Gender;
        public String Avater;
    }
}
