package com.newclass.woyaoxue.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.LinearLayout;
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
import com.newclass.woyaoxue.ChineseChat;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.bean.User;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.FileUtil;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//个人资料编辑页面
public class ActivityPerson extends Activity implements View.OnClickListener {
    private static final String TAG = "PersonActivity";
    private static final int REQUEST_CODE_AVATAR_TAKE = 49;
    private static final int REQUEST_CODE_AVATAR_PICK = 50;
    private static final int REQUEST_CODE_AVATAR_CROP = 51;
    private static final int REQUEST_CODE_PHOTOS_PICK = 60;

    private TextView tv_birth, tv_username;
    private EditText et_nickname, et_mobile, et_email, et_job, et_school, et_about;
    private Spinner sp_language, sp_country;
    private RadioGroup rg_gender;
    private RadioButton rb_male, rb_female;
    private ImageView iv_avatar;

    private File cropImage;
    private File saveImage;
    private boolean isIconSwitch = false;
    private ProgressDialog saveDialog;
    private DatePickerDialog datePickerDialog;
    private Dialog dialogPick;
    private SimpleDateFormat birthDateFormat;
    private Gson gson = new Gson();

    private List<String> countries = new ArrayList<String>();
    private List<String> languages = new ArrayList<String>();
    private ArrayAdapter<String> adapter_countries;
    private ArrayAdapter<String> adapter_languages;
    private SimpleDateFormat imageDateFormat;
    private File fileAvatarTake;
    private File fileAvatarCrop;
    private LinearLayout ll_photos;
    private int sizeUploadPhoto;
    private int sizePaddingLeft;
    private List<File> filePhotos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        initView();
        initData();

        saveDialog = new ProgressDialog(this);
        cropImage = new File(Environment.getExternalStorageDirectory(), "crop.png");
        saveImage = new File(Environment.getExternalStorageDirectory(), "save.png");

        createDatePickerDialog();

        birthDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        imageDateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cropImage.exists()) {
            cropImage.delete();
        }
        if (saveImage.exists()) {
            saveImage.delete();
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

        et_job = (EditText) findViewById(R.id.et_job);
        sp_country = (Spinner) findViewById(R.id.sp_country);
        sp_language = (Spinner) findViewById(R.id.sp_language);
        et_about = (EditText) findViewById(R.id.et_about);
        et_school = (EditText) findViewById(R.id.et_school);

        ll_photos = (LinearLayout) findViewById(R.id.ll_photos);

        iv_avatar.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
    }

    private void initData() {
        User user = ChineseChat.CurrentUser;
        //头像
        CommonUtil.showIcon(getApplicationContext(), iv_avatar, user.Avatar);
        //帐号
        tv_username.setText(user.Username);
        et_nickname.setText(user.Nickname);
        et_email.setText(user.Email);
        et_mobile.setText(user.Mobile);
        rb_female.setChecked(user.Gender == 0);
        rb_male.setChecked(user.Gender == 1);
        tv_birth.setText(user.Birth);
        et_job.setText(user.Job);
        et_about.setText(user.About);

        //显示国家，languages，工作
        String country = user.Country;
        String language = user.Language;

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

        if (ChineseChat.isStudent()) {


        } else {

            filePhotos = new ArrayList<File>();
            int measuredWidth = getResources().getDisplayMetrics().widthPixels;
            sizePaddingLeft = ll_photos.getPaddingLeft();
            sizeUploadPhoto = (measuredWidth - ll_photos.getPaddingLeft() - ll_photos.getPaddingRight() - (4 * sizePaddingLeft)) / 5;
            Log.i(TAG, "measuredWidth: " + measuredWidth + " paddingLeft=" + sizePaddingLeft + " size=" + sizeUploadPhoto);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(R.drawable.enqueue);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(sizeUploadPhoto, sizeUploadPhoto);
            ll_photos.addView(imageView, layout);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: 添加图片");
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, REQUEST_CODE_PHOTOS_PICK);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.tv_save: {

                com.lidroid.xutils.http.RequestParams params = new RequestParams();
                params.addBodyParameter("id", getSharedPreferences("user", MODE_PRIVATE).getInt("id", -1) + "");
                params.addBodyParameter("Name", et_nickname.getText().toString().trim());
                params.addBodyParameter("Email", et_email.getText().toString().trim());
                params.addBodyParameter("Mobile", et_mobile.getText().toString().trim());
                params.addBodyParameter("Gender", (rb_female.isChecked() ? 0 : 1) + "");
                params.addBodyParameter("Birth", tv_birth.getText().toString().trim());
                params.addBodyParameter("country", sp_country.getSelectedItem() + "");
                params.addBodyParameter("language", sp_language.getSelectedItem() + "");
                params.addBodyParameter("job", et_job.getText().toString().trim());
                params.addBodyParameter("school", et_school.getText().toString().trim());
                params.addBodyParameter("about", et_about.getText().toString().trim());
                // params.addbodyparameter

                if (isIconSwitch) {
                    params.addBodyParameter("icon", fileAvatarCrop);//把裁剪后的文件上传
                }
                if (filePhotos != null && filePhotos.size() > 0) {
                    for (File file : filePhotos) {
                        params.addBodyParameter("new_photos", file);//把裁剪后的文件上传
                    }
                }

                if (ChineseChat.isStudent()) {
                    saveStudent(params);
                } else {
                    saveTeacher(params);
                }


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
                break;
            case R.id.tv_album: {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_AVATAR_PICK);
                dialogPick.dismiss();
            }
            break;

            case R.id.tv_camera: {
                fileAvatarTake = getTakeOutputMediaFile();//定义好相片的路径
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileAvatarTake));
                startActivityForResult(intent, REQUEST_CODE_AVATAR_TAKE);
                dialogPick.dismiss();
            }
            break;
            case R.id.tv_birth:
                datePickerDialog.show();
                break;
        }
    }

    private void saveTeacher(RequestParams params) {
        saveDialog.show();
        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.userUpdate, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (resp.code == 200) {
                    ChineseChat.CurrentUser = resp.info;
                    CommonUtil.saveUserToSP(getApplicationContext(), resp.info, false);
                    CommonUtil.toast(getString(R.string.ActivityPerson_success));
                } else {
                    CommonUtil.toast(getString(R.string.ActivityPerson_failure));
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

    private void saveStudent(RequestParams params) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: " + requestCode + " resultCode:" + resultCode + " data:" + data);
        switch (requestCode) {
            case REQUEST_CODE_AVATAR_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    fileAvatarCrop = getCropOutputMediaFile();//为剪切的图片指定新的文件路径
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(data.getData(), "image/*");//剪切的来源是选择的图片的Uri,注:image/*要设置对
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 400);
                    intent.putExtra("outputY", 400);
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileAvatarCrop));
                    intent.putExtra("return-data", false);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                    intent.putExtra("noFaceDetection", true);
                    startActivityForResult(intent, REQUEST_CODE_AVATAR_CROP);
                }
                break;
            case REQUEST_CODE_AVATAR_TAKE:
                if (resultCode == Activity.RESULT_OK) {
                    //这个时候data返回为空,因为我们在MediaStore.EXTRA_OUTPUT中设置和Uri,所以数据现在在Uri中
                    fileAvatarCrop = getCropOutputMediaFile();//为剪切的图片指定新的文件路径
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(fileAvatarTake), "image/*");//裁剪的来源是拍照的相片的Uri
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 400);
                    intent.putExtra("outputY", 400);
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileAvatarCrop));
                    intent.putExtra("return-data", false);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                    intent.putExtra("noFaceDetection", true);
                    startActivityForResult(intent, REQUEST_CODE_AVATAR_CROP);
                }
                break;
            case REQUEST_CODE_AVATAR_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    isIconSwitch = true;//如果裁剪成功,说明头像要更换了
                    new BitmapUtils(this).display(iv_avatar, fileAvatarCrop.getAbsolutePath());
                }
                break;
            case REQUEST_CODE_PHOTOS_PICK: {
                if (resultCode == Activity.RESULT_OK) {
                    filePhotos.add(new File(FileUtil.getPath(this, data.getData())));
                    ImageView imageView = new ImageView(this);
                    imageView.setImageURI(data.getData());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeUploadPhoto, sizeUploadPhoto);
                    layoutParams.setMargins(0, 0, sizePaddingLeft, 0);
                    ll_photos.addView(imageView, 0, layoutParams);
                }

            }
            break;
        }
    }

    private File getTakeOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ChineseChat");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    private File getCropOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ChineseChat");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    private void createDatePickerDialog() {
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.i(TAG, "onDateSet: year:" + year + " monthOfYear:" + monthOfYear + " dayOfMonth:" + dayOfMonth);
            }
        }, 2000, 1, 1);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ActivityPerson_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar l = Calendar.getInstance();
                l.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth());
                tv_birth.setText(birthDateFormat.format(l.getTime()));
            }
        });
    }


    // 选取图片
    public void pick() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, REQUEST_CODE_AVATAR_PICK);
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
        startActivityForResult(intent, REQUEST_CODE_AVATAR_CROP);
    }


}
