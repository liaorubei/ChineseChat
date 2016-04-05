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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private ImageView iv_avatar;

    private File cropImage;
    private File saveImage;
    private boolean isIconSwitch = false;
    private ProgressDialog saveDialog;
    private DatePickerDialog datePickerDialog;
    private Dialog dialogPick;
    private SimpleDateFormat sdf;
    private Gson gson = new Gson();

    private List<String> countries = new ArrayList<String>();
    private List<String> languages = new ArrayList<String>();
    private List<String> vocations = new ArrayList<String>();
    private ArrayAdapter<String> adapter_countries;
    private ArrayAdapter<String> adapter_languages;
    private ArrayAdapter<String> adapter_vocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        initView();
        //  initData();
        saveDialog = new ProgressDialog(this);
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
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);

        et_nickname = (EditText) findViewById(R.id.et_nickname);
        et_email = (EditText) findViewById(R.id.et_email);
        et_mobile = (EditText) findViewById(R.id.et_mobile);

        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);

        tv_birth = (TextView) findViewById(R.id.tv_birth);

        sp_country = (Spinner) findViewById(R.id.sp_country);
        sp_language = (Spinner) findViewById(R.id.sp_language);
        sp_job = (Spinner) findViewById(R.id.sp_job);


        iv_avatar.setOnClickListener(this);
        tv_birth.setOnClickListener(this);

        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);

        //头像
        CommonUtil.showIcon(getApplicationContext(), iv_avatar, sp.getString("Avatar", ""));
        //帐号
        tv_username.setText(sp.getString("username", ""));

        et_nickname.setText(sp.getString("nickname", ""));
        et_email.setText(sp.getString("email", ""));
        et_mobile.setText(sp.getString("mobile", ""));
        int gender = sp.getInt("gender", -1);
        rb_female.setChecked(gender == 0);
        rb_male.setChecked(gender == 1);
        tv_birth.setText(sp.getString("birth", ""));

        //显示国家，languages，工作
        String country = sp.getString("country", "");
        String language = sp.getString("language", "");
        String vocation = sp.getString("vocation", "");

        String[] stringArray1 = getResources().getStringArray(R.array.countries);
        for (String s : stringArray1) {
            countries.add(s);
        }
        if (!countries.contains(country)) {
            countries.add(0, country);
        }
        adapter_countries = new ArrayAdapter<String>(this, R.layout.listitem_textview, countries);
        sp_country.setAdapter(adapter_countries);
        sp_country.setSelection(countries.lastIndexOf(country));

        String[] stringArray2 = getResources().getStringArray(R.array.languages);
        for (String s : stringArray2) {
            languages.add(s);
        }
        if (!languages.contains(language)) {
            languages.add(0, language);
        }
        adapter_languages = new ArrayAdapter<String>(this, R.layout.listitem_textview, languages);
        sp_language.setAdapter(adapter_languages);
        sp_language.setSelection(languages.lastIndexOf(language));

        String[] stringArray3 = getResources().getStringArray(R.array.vocations);
        for (String s : stringArray3) {
            vocations.add(s);
        }
        if (!vocations.contains(vocation)) {
            vocations.add(0, vocation);
        }
        adapter_vocations = new ArrayAdapter<String>(this, R.layout.listitem_textview, vocations);
        sp_job.setAdapter(adapter_vocations);
        sp_job.setSelection(vocations.lastIndexOf(vocation));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.tv_save: {
                saveDialog.show();
                com.lidroid.xutils.http.RequestParams params = new RequestParams();
                params.addBodyParameter("id", getSharedPreferences("user", MODE_PRIVATE).getInt("id", -1) + "");
                params.addBodyParameter("Name", et_nickname.getText().toString().trim());
                params.addBodyParameter("Email", et_email.getText().toString().trim());
                params.addBodyParameter("Mobile", et_mobile.getText().toString().trim());
                params.addBodyParameter("Gender", (rb_female.isChecked() ? 0 : 1) + "");
                params.addBodyParameter("Birth", tv_birth.getText().toString().trim());
                params.addBodyParameter("country", sp_country.getSelectedItem() + "");
                params.addBodyParameter("language", sp_language.getSelectedItem() + "");
                params.addBodyParameter("job", sp_job.getSelectedItem() + "");

                if (isIconSwitch) {
                    params.addBodyParameter("icon", cropImage);
                }
                new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.userUpdate, params, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Log.i(TAG, "onSuccess: " + responseInfo.result);
                        Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                        }.getType());

                        if (resp.code == 200) {
                            CommonUtil.saveUserToSP(getApplicationContext(), resp.info);
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
            break;
            case R.id.iv_avatar:
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
                    new BitmapUtils(this).display(iv_avatar, cropImage.getAbsolutePath());
                    isIconSwitch = true;
                }
                break;
        }
    }

    private void saveBitmap(Bitmap tempBitmap, File avatarPNG) {
        if (!avatarPNG.exists()) {
            if (!avatarPNG.getParentFile().exists()) {
                avatarPNG.getParentFile().mkdirs();
            }
            try {
                OutputStream stream = new FileOutputStream(avatarPNG);
                tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                Log.i(TAG, "saveBitmap: " + avatarPNG.getAbsolutePath());
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
