package com.hanwen.chinesechat.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityPayment;
import com.hanwen.chinesechat.activity.ActivityProfile;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.ChatData;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.TeacherQueue;
import com.hanwen.chinesechat.bean.User;
import com.hanwen.chinesechat.dialog.ProgressDialog;
import com.hanwen.chinesechat.service.ServiceQueue;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.XListView;
import com.hanwen.chinesechat.view.XListViewFooter;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师排队列表，学生端和教师端都可用，学生端显示拨打按钮，教师端显示入队按钮
 */
public class FragmentLineUp extends Fragment implements View.OnClickListener, XListView.IXListViewListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    private static final String TAG = "FragmentLineUp";
    private static final int REFRESH_DATA = 1;
    private static final int REQUEST_CODE_DOCUMENT = 1;
    private SwipeRefreshLayout srl;
    private static Gson gson = new Gson();
    private List<User> dataSet;
    private BaseAdapter<User> adapter;
    private int offset = 10;
    private int time = 0;
    private XListView listview;
    private int take = 50;
    private TextView tv_nickname, tv_line;
    private boolean resume = false;
    private User teacher;
    private AlertDialog dialogLogin;
    private int documentId = -1;
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    time += offset;
                    if (time >= 60) {
                        refresh(true);
                        time = 0;
                    }
                    Log.i(TAG, "handleMessage: time=" + time);
                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);//10秒回调一次，一分钟刷新一次
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            documentId = getArguments().getInt("documentId", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lineup, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        View rl_ctrl = view.findViewById(R.id.rl_ctrl);
        rl_ctrl.setVisibility(ChineseChat.isStudent() ? View.GONE : View.VISIBLE);
        ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        if (!TextUtils.isEmpty(ChineseChat.CurrentUser.Avatar)) {
            CommonUtil.showBitmap(iv_icon, NetworkUtil.getFullPath(ChineseChat.CurrentUser.Avatar));
        }
        tv_line = (TextView) view.findViewById(R.id.tv_line);
        tv_line.setOnClickListener(this);
        tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
        tv_nickname.setText(ChineseChat.CurrentUser.Nickname);

        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(this);

        dataSet = new ArrayList<>();
        adapter = new MyAdapter(dataSet);
        listview = (XListView) view.findViewById(R.id.listview);
        listview.setAdapter(adapter);
        listview.setPullupEnable(false);
        listview.setPullDownEnable(false);
        listview.setXListViewListener(this);
        listview.setOnItemClickListener(this);

        progressDialog = new ProgressDialog(getContext());
        //progressDialog.show();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        resume = true;
        time = 60;
        handler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
        time = 0;
        handler.removeCallbacksAndMessages(null);
        documentId = -1;
    }

    private void showLoginDialog() {
        if (dialogLogin == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.Fragment_person_dialog_title);
            builder.setMessage(R.string.Fragment_person_dialog_message);
            builder.setPositiveButton(R.string.Fragment_person_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getActivity(), ActivitySignIn.class));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.Fragment_person_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialogLogin = builder.create();
        }
        dialogLogin.show();
    }

    private void refresh(final boolean refresh) {
        if (resume) {
            srl.setRefreshing(true);
            resume = false;
        }

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", ChineseChat.CurrentUser.Id);
        params.add("skip", refresh ? 0 : dataSet.size());
        params.add("take", take);
        HttpUtil.post(NetworkUtil.getTeacherOnline, params, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);

                Response<TeacherQueue> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<TeacherQueue>>() {}.getType());
                if (refresh) {
                    dataSet.clear();
                }

                listview.setPullupEnable(true);
                if (resp.code == 200) {
                    //处理教师列表
                    List<User> users = resp.info.Teacher;
                    for (User user : users) {
                        dataSet.add(user);
                    }
                    listview.stopLoadMore(users.size() < take ? XListViewFooter.STATE_NOMORE : XListViewFooter.STATE_NORMAL);

                    //处理教师的入队情况
                    TeacherQueue.NewUser current = resp.info.Current;
                    boolean a = current != null && current.IsOnline == 1 && current.IsEnable == 1 && current.IsQueue == 1;
                    tv_line.setText(a ? R.string.FragmentLineUp_i_need_dequeue : R.string.FragmentLineUp_i_need_enqueue);
                    tv_line.setSelected(a);
                } else {
                    listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
                }


                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                srl.setRefreshing(false);
                listview.setPullupEnable(true);
                listview.stopLoadMore(XListViewFooter.STATE_ERRORS);
            }
        });
    }

    @Override//srl刷新事件,xListView刷新事件
    public void onRefresh() {
        refresh(true);
    }

    @Override//加载更多
    public void onLoadMore() {
        refresh(false);
    }

    @Override//view点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_line: {
                Intent service = new Intent(getContext(), ServiceQueue.class);
                if (tv_line.isSelected()) {
                    getContext().stopService(service);
                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", ChineseChat.CurrentUser.Id);
                    HttpUtil.post(NetworkUtil.teacherDequeue, parameters, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            tv_line.setSelected(false);
                            tv_line.setText(R.string.FragmentLineUp_i_need_enqueue);
                            CommonUtil.toast(R.string.FragmentLineUp_dequeue_success);
                            refresh(true);
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast(R.string.FragmentLineUp_dequeue_failure);
                        }
                    });
                } else {
                    getContext().startService(service);
                    HttpUtil.Parameters parameters = new HttpUtil.Parameters();
                    parameters.add("id", ChineseChat.CurrentUser.Id);
                    HttpUtil.post(NetworkUtil.teacherEnqueue, parameters, new RequestCallBack<String>() {

                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Response resp = gson.fromJson(responseInfo.result, new TypeToken<Response>() {
                            }.getType());
                            if (resp.code == 200) {
                                refresh(true);
                                CommonUtil.toast(R.string.FragmentLineUp_enqueue_success);
                                tv_line.setSelected(true);
                                tv_line.setText(R.string.FragmentLineUp_i_need_dequeue);
                            }
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            CommonUtil.toast(R.string.FragmentLineUp_enqueue_failure);
                        }
                    });
                }
            }
            break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - 1;
        if (0 <= index && index < dataSet.size()) {
            ActivityProfile.start(getActivity(), dataSet.get(index), ChineseChat.isStudent());
        }
    }

    private class MyAdapter extends BaseAdapter<User> {
        public MyAdapter(List<User> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final User user = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_choose, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            //三种状态，在线，忙线，掉线 {绿，红，灰}

            holder.tv_nickname.setText(String.format("%1$s%2$s", TextUtils.isEmpty(user.Nickname) ? "" : user.Nickname, TextUtils.equals(user.Username, ChineseChat.CurrentUser.Username) ? "[本人]" : ""));
            holder.tv_nickname.setTextColor(user.IsEnable ? getResources().getColor(R.color.color_app) : Color.RED);
            holder.tv_spoken.setText(user.Spoken);
            if (!TextUtils.isEmpty(user.Avatar)) {
                CommonUtil.showBitmap(holder.iv_avatar, NetworkUtil.getFullPath(user.Avatar));
            } else {
                holder.iv_avatar.setImageResource(R.drawable.ic_launcher_student);
            }
            holder.iv_status.setImageResource(user.IsEnable ? R.drawable.teacher_online : R.drawable.teacher_busy);

            //设置点击,可见,可用
            holder.bt_call.setVisibility(ChineseChat.isStudent() ? View.VISIBLE : View.GONE);
            //holder.bt_call.setBackgroundResource(R.drawable.selector_choose_callb);
            //holder.bt_call.setEnabled(user.IsEnable && user.IsOnline);
            holder.bt_call.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: ");
                    teacher = user;
                    //如果没有登录,那么要求登录
                    if (NIMClient.getStatus() != StatusCode.LOGINED) {
                        showLoginDialog();
                        return;
                    }

                    //安卓6.0运行时权限检查
                    boolean isGranted = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

                    if (isGranted) {
                        callTeacher(teacher);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUESTCODE_RECORD_AUDIO);
                    }

/*                    if (!isGranted) {
                        if (!permissionCheck) {
                            permissionCheck = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Unavailable");
                            builder.setMessage("You must turn on the microphone before calling the tutor. Please enter  Settings options to allow ChineseChat's access to your microphone");
                            builder.setPositiveButton("OK", null);
                            builder.show();
                            return;
                        }
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUESTCODE_RECORD_AUDIO);
                    } else {
                        callTeacher(user);
                    }*/
                }
            });
            return convertView;
        }
    }

    /**
     * 学生选择教师返回来的JSON帮忙类
     */
    private class ChooseTeacherModel {
        public User Student;
        public User Teacher;
    }

    private void callTeacher(User teacher) {
        if (!teacher.IsOnline) {
            CommonUtil.toast(R.string.FragmentChoose_offline);
            return;
        }

        if (!teacher.IsEnable) {
            CommonUtil.toast(R.string.FragmentChoose_busy);
            return;
        }
        progressDialog.show();
        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("id", ChineseChat.CurrentUser.Id);
        parameters.add("target", teacher.Id);
        HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<ChooseTeacherModel> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<ChooseTeacherModel>>() {}.getType());
                if (resp.code == 200) {

                    final User studentResp = resp.info.Student;
                    final User teacherResp = resp.info.Teacher;
                    if (studentResp.Coins <= 30) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setMessage(getString(R.string.FragmentLineUp_balance_tips, studentResp.Coins));
                        builder.setNegativeButton(R.string.FragmentLineUp_balance_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendCall(studentResp, teacherResp);
                            }
                        });
                        builder.setPositiveButton(R.string.FragmentLineUp_balance_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getContext(), ActivityPayment.class);
                                startActivity(intent);
                            }
                        });
                        builder.show();
                        return;
                    }

                    sendCall(studentResp, teacherResp);

                } else if (resp.code == 201) {
                    CommonUtil.toast(R.string.FragmentChoose_The_tutor_is_busy_now);
                } else if (resp.code == 202) {
                    CommonUtil.toast(R.string.FragmentChoose_invalid_tutor);
                } else if (resp.code == 203) {
                    CommonUtil.toast(R.string.FragmentChoose_Balance_is_not_enough);
                } else if (resp.code == 204) {
                    CommonUtil.toast(R.string.FragmentChoose_invalid_user);
                } else {
                    CommonUtil.toast(R.string.network_error);
                }

                progressDialog.hide();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(R.string.network_error);

                progressDialog.hide();
            }
        });

    }

    /**
     * 发送一个外拔电话，由学生拨出到老师
     *
     * @param student 学生实体，要求有Id，Nickname等
     * @param teacher 教师实体，要求有Id，Nickname等
     */
    private void sendCall(User student, User teacher) {
        //兼容老版本写法1.1.7及之前,当教师是1.1.7之前的版本,要把学生的信息传递给教师
        JsonObject json = new JsonObject();
        json.addProperty("Id", student.Id);
        json.addProperty("Avatar", student.Avatar);
        json.addProperty("Nickname", student.Nickname);
        json.addProperty("Country", student.Country);
        json.addProperty("DocumentId", documentId);

        //新版本要求显示教师及学生明细1.1.8版本
        JsonObject studentJson = new JsonObject();
        studentJson.addProperty("Id", student.Id);
        studentJson.addProperty("Avatar", student.Avatar);
        studentJson.addProperty("Nickname", student.Nickname);
        studentJson.addProperty("Country", student.Country);

        JsonObject studentSummary = new JsonObject();
        studentSummary.addProperty("month", student.Summary.month);
        studentSummary.addProperty("count", student.Summary.count);
        studentSummary.addProperty("duration", student.Summary.duration);
        studentJson.add("Summary", studentSummary);
        json.add("Student", studentJson);

        JsonObject teacherJson = new JsonObject();
        teacherJson.addProperty("Id", teacher.Id);
        teacherJson.addProperty("Avatar", teacher.Avatar);
        teacherJson.addProperty("Nickname", teacher.Nickname);
        teacherJson.addProperty("Country", teacher.Country);

        JsonObject teacherSummary = new JsonObject();
        teacherSummary.addProperty("month", teacher.Summary.month);
        teacherSummary.addProperty("count", teacher.Summary.count);
        teacherSummary.addProperty("duration", teacher.Summary.duration);
        teacherJson.add("Summary", teacherSummary);
        json.add("Teacher", teacherJson);

        //ChatData chatData = new ChatData();
        //chatData.setExtra(json.toString());
        ChatData chatData = ActivityChat.buildChat(teacher, student);
        chatData.setAccid(teacher.Accid);
        chatData.setChatType(AVChatType.AUDIO);

        Log.i(TAG, "onSuccess: " + documentId);
        if (documentId > 0) {
            Log.i(TAG, "onSuccess: " + json);
            Intent intent = new Intent(getContext(), ActivityChat.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ActivityChat.KEY_CHAT_MODE, ActivityChat.CHAT_MODE_OUTGOING);
            intent.putExtra(ActivityChat.KEY_CHAT_DATA, chatData);
            startActivityForResult(intent, REQUEST_CODE_DOCUMENT);
        } else {
            ActivityChat.start(getActivity(), ActivityChat.CHAT_MODE_OUTGOING, chatData);
        }
    }

    private static final int PERMISSION_REQUESTCODE_RECORD_AUDIO = 5;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DOCUMENT) {
            getActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: +" + grantResults[0]);

        if (requestCode == PERMISSION_REQUESTCODE_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTeacher(teacher);
            } else {
                new AlertDialog
                        .Builder(getContext())
                        .setMessage("when you call a teacher you must open the microphone!")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean b = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                                if (b) {
                                    Log.i(TAG, "请求: ");
                                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUESTCODE_RECORD_AUDIO);
                                } else {
                                    Log.i(TAG, "跳转: ");
                                    Intent settingIntent = new Intent();
                                    settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    settingIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    settingIntent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                                    startActivity(settingIntent);
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private class ViewHolder {
        public TextView tv_status;
        public TextView tv_nickname;

        public ImageView iv_avatar, iv_status;
        public TextView tv_spoken;
        public ImageView bt_call;

        public ViewHolder(View convertView) {
            this.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            this.iv_status = (ImageView) convertView.findViewById(R.id.iv_status);
            this.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
            this.tv_spoken = (TextView) convertView.findViewById(R.id.tv_spoken);
            this.bt_call = (ImageView) convertView.findViewById(R.id.bt_call);
            this.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
        }
    }
}