package com.hanwen.chinesechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.FileUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//个人资料编辑页面
public class ActivityPerson extends Activity implements View.OnClickListener {
    private static final String TAG = "PersonActivity";
    private static final int REQUEST_CODE_AVATAR_TAKE = 49;
    private static final int REQUEST_CODE_AVATAR_PICK = 50;
    private static final int REQUEST_CODE_AVATAR_CROP = 51;
    private static final int REQUEST_CODE_PHOTOS_PICK = 60;

    private TextView tv_birth, tv_username;
    private EditText et_nickname, et_mobile, et_email, et_job, et_school, et_about, et_hobby, et_spoken, et_location;
    private Spinner sp_language, sp_country;
    private RadioButton rb_male, rb_female;
    private ImageView iv_avatar;

    private File cropImage;
    private File saveImage;
    private boolean isIconSwitch = false;
    private ProgressDialog saveDialog;
    private DatePickerDialog datePickerDialog;
    private Dialog dialogPick;
    private Dialog dialogAlbum;
    private SimpleDateFormat birthDateFormat;
    private Gson gson = new Gson();

    private List<String> countries = new ArrayList<String>();
    private List<String> languages = new ArrayList<String>();
    private ArrayAdapter<String> adapter_countries;
    private ArrayAdapter<String> adapter_languages;
    private SimpleDateFormat imageDateFormat;
    private File fileAvatarTake;
    private File fileAvatarCrop;
    private LinearLayout ll_photos, ll_avatar, ll_nickname, ll_email, ll_mobile, ll_gender, ll_birth, ll_job, ll_education, ll_country, ll_language, ll_hobby, ll_spoken, ll_about, ll_location;
    private int sizeUploadPhoto;
    private int sizePaddingLeft;
    private List<File> filePhotos;
    private View viewOperation;//要操作的照片
    private List<String> deletedPhotos;
    private RadioGroup rg_gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        birthDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        imageDateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        initView();
        initData();

        saveDialog = new ProgressDialog(this);
        cropImage = new File(Environment.getExternalStorageDirectory(), "crop.png");
        saveImage = new File(Environment.getExternalStorageDirectory(), "save.png");


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

        rg_gender = (RadioGroup) findViewById(R.id.rg_gender);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);

        tv_birth = (TextView) findViewById(R.id.tv_birth);

        et_job = (EditText) findViewById(R.id.et_job);
        sp_country = (Spinner) findViewById(R.id.sp_country);
        sp_language = (Spinner) findViewById(R.id.sp_language);
        et_about = (EditText) findViewById(R.id.et_about);
        et_school = (EditText) findViewById(R.id.et_school);
        et_hobby = (EditText) findViewById(R.id.et_hobby);
        et_location = (EditText) findViewById(R.id.et_location);
        et_spoken = (EditText) findViewById(R.id.et_spoken);

        ll_avatar = (LinearLayout) findViewById(R.id.ll_avatar);
        ll_nickname = (LinearLayout) findViewById(R.id.ll_nickname);
        ll_email = (LinearLayout) findViewById(R.id.ll_email);
        ll_mobile = (LinearLayout) findViewById(R.id.ll_mobile);
        ll_gender = (LinearLayout) findViewById(R.id.ll_gender);
        ll_birth = (LinearLayout) findViewById(R.id.ll_birth);
        ll_job = (LinearLayout) findViewById(R.id.ll_job);
        ll_education = (LinearLayout) findViewById(R.id.ll_education);
        ll_country = (LinearLayout) findViewById(R.id.ll_country);
        ll_language = (LinearLayout) findViewById(R.id.ll_language);
        ll_hobby = (LinearLayout) findViewById(R.id.ll_hobby);
        ll_location = (LinearLayout) findViewById(R.id.ll_location);
        ll_spoken = (LinearLayout) findViewById(R.id.ll_spoken);
        ll_about = (LinearLayout) findViewById(R.id.ll_about);
        ll_photos = (LinearLayout) findViewById(R.id.ll_photos);

        iv_avatar.setOnClickListener(this);
        rb_female.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
    }

    private void initData() {
        User user = ChineseChat.CurrentUser;
        //头像
        CommonUtil.showBitmap(iv_avatar, NetworkUtil.getFullPath(user.Avatar));
        //帐号
        tv_username.setText(user.Username);
        et_nickname.setText(user.Nickname);
        et_email.setText(user.Username);
        et_mobile.setText(user.Mobile);
        rb_female.setChecked(user.Gender == 0);
        rb_male.setChecked(user.Gender == 1);
        tv_birth.setText(user.Birth);

        createDatePickerDialog(user.Birth);

        //显示国家，languages，工作
        String country = user.Country == null ? "" : user.Country;//国籍
        String language = user.Language == null ? "" : user.Language;//母语
        et_job.setText(user.Job);

        et_about.setText(user.About);
        et_school.setText(user.School);
        et_hobby.setText(user.Hobbies);
        et_spoken.setText(user.Spoken);
        et_location.setText(user.Country);//地标

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

        Log.i(TAG, "initData: ");
        adapter_languages = new ArrayAdapter<String>(this, R.layout.listitem_textview, languages);
        sp_language.setAdapter(adapter_languages);
        sp_language.setSelection(languages.lastIndexOf(language));

        if (ChineseChat.isStudent()) {
            et_school.setVisibility(View.GONE);
            et_hobby.setVisibility(View.GONE);
            et_spoken.setVisibility(View.GONE);

            ll_hobby.setVisibility(View.GONE);
            ll_location.setVisibility(View.GONE);
            ll_spoken.setVisibility(View.GONE);
            ll_about.setVisibility(View.GONE);
            ll_photos.setVisibility(View.GONE);
            ll_education.setVisibility(View.GONE);

            ll_language.setVisibility(View.VISIBLE);
            ll_country.setVisibility(View.VISIBLE);
            ll_job.setVisibility(View.VISIBLE);

        } else {
            filePhotos = new ArrayList<File>();
            int measuredWidth = getResources().getDisplayMetrics().widthPixels;
            sizePaddingLeft = ll_photos.getPaddingLeft();
            sizeUploadPhoto = (measuredWidth - ll_photos.getPaddingLeft() - ll_photos.getPaddingRight() - (4 * sizePaddingLeft)) / 5;
            Log.i(TAG, "measuredWidth: " + measuredWidth + " paddingLeft=" + sizePaddingLeft + " size=" + sizeUploadPhoto);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(R.drawable.icon_enqueue);
            imageView.setBackgroundResource(R.color.color_app_normal);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(sizeUploadPhoto, sizeUploadPhoto);
            ll_photos.addView(imageView, layout);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, REQUEST_CODE_PHOTOS_PICK);
                }
            });

            if (user.Photos == null)
                return;
            //
            for (String s : user.Photos) {
                ImageView photoAdd = new ImageView(this);
                photoAdd.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoAdd.setTag(s);//把图片路径保存下
                photoAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogAlbum == null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPerson.this);
                            View inflate = getLayoutInflater().inflate(R.layout.dialog_album_operation, null);
                            builder.setView(inflate);
                            builder.setCancelable(true);
                            dialogAlbum = builder.create();
                            dialogAlbum.setCanceledOnTouchOutside(true);
                            inflate.findViewById(R.id.tv_lookat).setOnClickListener(ActivityPerson.this);
                            inflate.findViewById(R.id.tv_delete).setOnClickListener(ActivityPerson.this);
                        }
                        viewOperation = v;
                        dialogAlbum.show();
                    }
                });
                CommonUtil.showBitmap(photoAdd, NetworkUtil.getFullPath(s));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(sizeUploadPhoto, sizeUploadPhoto);
                p.setMargins(0, 0, sizePaddingLeft, 0);
                ll_photos.addView(photoAdd, 0, p);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home:
                this.finish();
                break;
            case R.id.rb_female:
                Log.i(TAG, "onClick: " + rb_female.isChecked());
                break;
            case R.id.tv_save: {
                //通用参数
                com.lidroid.xutils.http.RequestParams params = new RequestParams();
                params.addBodyParameter("username", ChineseChat.CurrentUser.Username);
                params.addBodyParameter("nickname", et_nickname.getText().toString().trim());
                //params.addBodyParameter("Email", et_email.getText().toString().trim());
                params.addBodyParameter("Mobile", et_mobile.getText().toString().trim());
                if (rb_female.isChecked() || rb_male.isChecked()) {
                    params.addBodyParameter("Gender", (rb_female.isChecked() ? 0 : 1) + "");
                }
                params.addBodyParameter("Birth", tv_birth.getText().toString().trim());

                //头像和图片
                if (isIconSwitch) {
                    params.addBodyParameter("icon", fileAvatarCrop);//把裁剪后的文件上传
                }
                if (deletedPhotos != null && deletedPhotos.size() > 0) {
                    for (int i = 0; i < deletedPhotos.size(); i++) {
                        params.addBodyParameter("deletedPhotos", deletedPhotos.get(i));
                    }
                }
                if (filePhotos != null && filePhotos.size() > 0) {
                    for (int i = 0; i < filePhotos.size(); i++) {
                        params.addBodyParameter("newPhoto" + i, filePhotos.get(i));
                    }
                }

                //其他参数
                if (ChineseChat.isStudent()) {
                    params.addBodyParameter("language", sp_language.getSelectedItem() + "");
                    params.addBodyParameter("job", et_job.getText().toString().trim());
                    params.addBodyParameter("country", sp_country.getSelectedItem() + "");//国家
                    saveStudent(params);
                } else {
                    params.addBodyParameter("about", et_about.getText().toString().trim());
                    params.addBodyParameter("school", et_school.getText().toString().trim());
                    params.addBodyParameter("spoken", et_spoken.getText().toString().trim());
                    params.addBodyParameter("hobbies", et_hobby.getText().toString().trim());
                    params.addBodyParameter("country", et_location.getText().toString().trim());//地标
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

            case R.id.tv_lookat: {
                ActivityAlbum.start(this, new String[]{NetworkUtil.getFullPath((String) viewOperation.getTag())}, 0);
                dialogAlbum.dismiss();
            }
            break;
            case R.id.tv_delete:
                if (deletedPhotos == null) {
                    deletedPhotos = new ArrayList<>();
                }
                deletedPhotos.add((String) viewOperation.getTag());
                viewOperation.setVisibility(View.GONE);
                dialogAlbum.dismiss();
                break;
        }
    }

    private void saveTeacher(RequestParams params) {
        saveDialog.show();
        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.nimUserUpdateTeacher, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<User> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (resp.code == 200) {
                    ChineseChat.CurrentUser.Id = resp.info.Id;
                    ChineseChat.CurrentUser.Avatar = resp.info.Avatar;
                    ChineseChat.CurrentUser.Nickname = resp.info.Nickname;
                    ChineseChat.CurrentUser.Mobile = resp.info.Mobile;
                    ChineseChat.CurrentUser.Birth = resp.info.Birth;
                    ChineseChat.CurrentUser.Gender = resp.info.Gender;
                    ChineseChat.CurrentUser.School = resp.info.School;
                    ChineseChat.CurrentUser.Hobbies = resp.info.Hobbies;
                    ChineseChat.CurrentUser.Spoken = resp.info.Spoken;
                    ChineseChat.CurrentUser.About = resp.info.About;
                    ChineseChat.CurrentUser.Country = resp.info.Country;
                    ChineseChat.CurrentUser.Photos = resp.info.Photos;

                    CommonUtil.saveUserToSP(getApplicationContext(), ChineseChat.CurrentUser, false);
                    CommonUtil.toast(getString(R.string.ActivityPerson_success));
                } else {
                    CommonUtil.toast(getString(R.string.ActivityPerson_failure));
                }
                saveDialog.dismiss();
                finish();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                saveDialog.dismiss();
            }
        });
    }

    private void saveStudent(RequestParams params) {
        saveDialog.show();
        new HttpUtils().send(HttpRequest.HttpMethod.POST, NetworkUtil.nimUserUpdateStudent, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<User> o = gson.fromJson(responseInfo.result, new TypeToken<Response<User>>() {
                }.getType());

                if (o.code == 200) {
                    User info = o.info;
                    ChineseChat.CurrentUser.Avatar = info.Avatar;
                    ChineseChat.CurrentUser.Nickname = info.Nickname;
                    ChineseChat.CurrentUser.Mobile = info.Mobile;
                    ChineseChat.CurrentUser.Birth = info.Birth;
                    ChineseChat.CurrentUser.Gender = info.Gender;
                    ChineseChat.CurrentUser.Country = info.Country;
                    ChineseChat.CurrentUser.Language = info.Language;
                    ChineseChat.CurrentUser.Job = info.Job;

                    CommonUtil.saveUserToSP(getApplicationContext(), ChineseChat.CurrentUser, false);
                    CommonUtil.toast(R.string.ActivityPerson_success);
                } else {
                    CommonUtil.toast(R.string.ActivityPerson_failure);
                }
                saveDialog.dismiss();
                finish();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(R.string.ActivityPerson_failure);
                saveDialog.dismiss();
            }
        });
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
                    iv_avatar.setImageURI(Uri.fromFile(fileAvatarCrop));

                }
                break;
            case REQUEST_CODE_PHOTOS_PICK: {
                if (resultCode == Activity.RESULT_OK) {
                    File photoPick = new File(FileUtil.getPath(this, data.getData()));
                    filePhotos.add(photoPick);

                    ImageView photoAdd = new ImageView(this);
                    photoAdd.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    CommonUtil.showBitmap(photoAdd, photoPick.getAbsolutePath());
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(sizeUploadPhoto, sizeUploadPhoto);
                    p.setMargins(0, 0, sizePaddingLeft, 0);
                    ll_photos.addView(photoAdd, 0, p);
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
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

    private void createDatePickerDialog(String birth) {
        Calendar l = null;
        try {
            birthDateFormat.parse(birth);
            l = birthDateFormat.getCalendar();
        } catch (Exception ex) {
            Log.i(TAG, "转换异常: ");
        }

        if (l == null) {
            l = Calendar.getInstance();
            l.set(2000, 0, 1);
        }

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar l = Calendar.getInstance();
                l.set(year, monthOfYear, dayOfMonth);
                tv_birth.setText(birthDateFormat.format(l.getTime()));
            }
        }, l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH));
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
