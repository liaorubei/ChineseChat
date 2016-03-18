package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//个人资料编辑页面
public class ActivityPerson extends Activity implements View.OnClickListener {
    private static final String TAG = "PersonActivity";
    private static final int PHOTO_REQUEST_TAKE = 49;
    private static final int PHOTO_REQUEST_PICK = 50;
    private static final int PHOTO_REQUEST_CROP = 51;

    private TextView tv_birth, tv_username;
    private EditText et_nickname, et_mobile, et_email;
    private Spinner sp_job, sp_language, sp_country;
    private RadioGroup rg_gender;
    private RadioButton rb_male, rb_female;
    private ImageView iv_avater;

    private File cropImage;
    private File saveImage;
    private boolean isIconSwitch = false;
    private ProgressDialog saveDialog;
    private DatePickerDialog datePickerDialog;
    private Dialog dialogPick;
    private SimpleDateFormat sdf;
    private Gson gson = new Gson();

    private String[] 国家 = new String[]{"中国", "日本", "韩国", "朝鲜", "老挝", "越南"};
    private String[] 母语 = new String[]{"普通话", "日语", "韩语", "朝鲜语", "越南语"};
    private String[] 职业 = new String[]{"老师", "公务员", "司机", "学生", "建筑师", "设计师"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        initView();
        //  initData();
        saveDialog = new ProgressDialog(this);
        // cropImage = new File(FolderUtil.rootDir(this), "temp_icon.PNG");
        cropImage = new File(Environment.getExternalStorageDirectory(), "crop.png");
        saveImage = new File(Environment.getExternalStorageDirectory(), "save.png");
        if (cropImage.exists()) {
            cropImage.delete();
        }
        if (saveImage.exists()) {
            saveImage.delete();
        }

        createDatePickerDialog();

        sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initView() {
        //标题栏
        findViewById(R.id.iv_home).setOnClickListener(this);
        findViewById(R.id.tv_save).setOnClickListener(this);

        //编辑区域
        tv_username = (TextView) findViewById(R.id.tv_username);
        iv_avater = (ImageView) findViewById(R.id.iv_avater);

        et_nickname = (EditText) findViewById(R.id.et_nickname);
        et_email = (EditText) findViewById(R.id.et_email);
        et_mobile = (EditText) findViewById(R.id.et_mobile);

        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);

        tv_birth = (TextView) findViewById(R.id.tv_birth);

        sp_country = (Spinner) findViewById(R.id.sp_country);
        sp_language = (Spinner) findViewById(R.id.sp_language);
        sp_job = (Spinner) findViewById(R.id.sp_job);


        iv_avater.setOnClickListener(this);
        tv_birth.setOnClickListener(this);


        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);

        tv_username.setText(sp.getString("username", ""));

        et_nickname.setText(sp.getString("nickname", ""));
        et_email.setText(sp.getString("email", ""));
        et_mobile.setText(sp.getString("mobile", ""));
        int gender = sp.getInt("gender", -1);
        rb_female.setChecked(gender == 0);
        rb_male.setChecked(gender == 1);
        tv_birth.setText(sp.getString("birth", ""));


        rg_gender = (RadioGroup) findViewById(R.id.rg_gender);
        rb_male = (RadioButton) findViewById(R.id.rb_female);
        rb_female = (RadioButton) findViewById(R.id.rb_female);


        sp_country.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem_textview, 国家));
        sp_language.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem_textview, 母语));
        sp_job.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem_textview, 职业));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.tv_save:
                saveUserEx();
                break;
            case R.id.iv_avater:
                if (dialogPick == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View inflate = getLayoutInflater().inflate(R.layout.dialog_pick, null);
                    builder.setView(inflate);
                    inflate.findViewById(R.id.tv_album).setOnClickListener(this);
                    inflate.findViewById(R.id.tv_camera).setOnClickListener(this);
                    dialogPick = builder.create();
                    dialogPick.setCancelable(true);
                    dialogPick.setCanceledOnTouchOutside(true);
                }
                dialogPick.show();


                //  pick();

                // createDatePickerDialog();
                //datePickerDialog.show();
                break;
            case R.id.tv_album: {
                // Intent intent = new Intent(Intent.ACTION_PICK);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_REQUEST_PICK);
                dialogPick.dismiss();
            }
            break;

            case R.id.tv_camera: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveImage));
                startActivityForResult(intent, PHOTO_REQUEST_TAKE);
                dialogPick.dismiss();
            }
            break;
            case R.id.tv_birth:
                datePickerDialog.show();
                break;
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropImage));//Uri.fromFile(cropImage)
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_PICK:
                Log.i(TAG, "onActivityResult: PHOTO_REQUEST_PICK data=" + data);
                if (data != null) {

                }
                break;
            case PHOTO_REQUEST_TAKE:
                Log.i(TAG, "onActivityResult: PHOTO_REQUEST_TAKE data=" + data);
                if (resultCode == Activity.RESULT_OK) {
                    cropImageUri(Uri.fromFile(saveImage), 250, 250, PHOTO_REQUEST_CROP);
                }
                break;
            case PHOTO_REQUEST_CROP:
                Log.i(TAG, "onActivityResult: PHOTO_REQUEST_CROP data=" + data);
                if (data != null) {
                    new BitmapUtils(this).display(iv_avater, cropImage.getAbsolutePath());
                    isIconSwitch = true;
                }
                break;
        }
    }

    private void saveUserEx() {
        saveDialog.show();
        com.lidroid.xutils.http.RequestParams params = new RequestParams();
        params.addBodyParameter("id", getSharedPreferences("user", MODE_PRIVATE).getInt("id", -1) + "");
        params.addBodyParameter("Name", et_nickname.getText().toString().trim());
        params.addBodyParameter("Email", et_email.getText().toString().trim());
        params.addBodyParameter("Mobile", et_mobile.getText().toString().trim());
        params.addBodyParameter("Gender", (rb_female.isChecked() ? 0 : 1) + "");
        params.addBodyParameter("Birth", tv_birth.getText().toString().trim());
        if (isIconSwitch) {
            params.addBodyParameter("icon", cropImage);
            // params.addBodyParameter("icon",new File(Environment.getExternalStorageDirectory(),"图片.jpg"));//某个图片的路径
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.userUpdate, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (resp.code == 200) {
                    //保存用户信息到sp
                    SharedPreferences.Editor editor = ActivityPerson.this.getSharedPreferences("user", MODE_PRIVATE).edit();
                    editor.putInt("id", resp.info.Id);
                    editor.putString("nickname", resp.info.Name);
                    editor.putString("avater", resp.info.Icon);
                    editor.putString("email", resp.info.Email);
                    editor.putString("birth", resp.info.Birth);
                    editor.putString("mobile", resp.info.Mobile);
                    editor.putInt("gender", resp.info.Gender);
                    editor.commit();

                    CommonUtil.toast("修改成功");
                } else {
                    CommonUtil.toast("修改失败");
                }
                saveDialog.dismiss();
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


    private void createDatePickerDialog() {
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.i(TAG, "onDateSet: year:" + year + " monthOfYear:" + monthOfYear + " dayOfMonth:" + dayOfMonth);
            }
        }, 2000, 1, 1);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar l = Calendar.getInstance();
                l.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth());
                tv_birth.setText(sdf.format(l.getTime()));
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
        intent.putExtra("return-data", false);//要求返回数据
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CROP);
    }
}
