package com.hanwen.chinesechat.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
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
import com.hanwen.chinesechat.activity.ActivityAlbum;
import com.hanwen.chinesechat.activity.ActivityChat;
import com.hanwen.chinesechat.activity.ActivityProfile;
import com.hanwen.chinesechat.activity.ActivitySignIn;
import com.hanwen.chinesechat.base.BaseAdapter;
import com.hanwen.chinesechat.bean.ChatData;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.bean.TeacherQueue;
import com.hanwen.chinesechat.bean.User;
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

public class FragmentChoose extends Fragment {
    private static final int PERMISSION_REQUESTCODE_RECORD_AUDIO = 1;
    private static final int REQUEST_CODE_DOCUMENT = 1;
    private String TAG = "FragmentChoose";
    private static final int REFRESH_DATA = 1;
    private static Gson gson = new Gson();
    private boolean visible = false;
    private List<User> list;
    private MyAdapter adapter;
    private SwipeRefreshLayout srl;
    private int time = 0;
    private AlertDialog dialogLogin;
    private XListView xListView;
    private int take = 150;
    private boolean first = false;
    private User teacher;

    //region handler
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    int offset = 1;//递归时间，单位秒
                    time += offset;
                    if (visible && time >= 30) {
                        refresh(true);
                    }

                    sendEmptyMessageDelayed(REFRESH_DATA, offset * 1000);
                    break;
            }
        }
    };
    //endregion
    private boolean permissionCheck = false;//标明是否已经进行过权限确认显示
    private int documentId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            documentId = getArguments().getInt("documentId", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        list = new ArrayList<User>();
        adapter = new MyAdapter(list);

        xListView = (XListView) view.findViewById(R.id.listview);
        xListView.setAdapter(adapter);
        xListView.setPullupEnable(false);
        xListView.setPullDownEnable(false);
        xListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }

            @Override
            public void onLoadMore() {
                refresh(false);
            }
        });
        xListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position <= list.size()) {
                    ActivityProfile.start(getActivity(), list.get(position - 1), true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        time = 60;
        visible = true;
        first = true;
        handler.removeCallbacksAndMessages(null);
        Message message = handler.obtainMessage();
        message.what = REFRESH_DATA;
        handler.sendMessage(message);
    }

    @Override
    public void onPause() {
        super.onPause();
        visible = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void refresh(final boolean refresh) {
        time = 0;
        if (first) {//只第一次自动刷新的时候出现这个加载图标
            srl.setRefreshing(true);
            first = false;
        }

        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("id", ChineseChat.CurrentUser.Id);
        params.add("skip", refresh ? 0 : list.size());
        params.add("take", take);
        HttpUtil.post(NetworkUtil.getTeacherOnline, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i(TAG, "onSuccess: " + responseInfo.result);
                Response<TeacherQueue> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<TeacherQueue>>() {
                }.getType());
                if (refresh) {
                    list.clear();
                }

                xListView.setPullupEnable(true);
                List<User> info = resp.info.Teacher;
                if (resp.code == 200) {
                    for (User u : info) {
                        list.add(u);
                    }
                    if (info.size() == 0) {
                        xListView.stopLoadMore(XListViewFooter.STATE_EMPTY);
                    } else if (info.size() > 0 && info.size() < take) {
                        xListView.stopLoadMore(XListViewFooter.STATE_NOMORE);
                    } else if (info.size() == take) {
                        xListView.stopLoadMore(XListViewFooter.STATE_NORMAL);
                    }


                } else {
                    xListView.stopLoadMore(XListViewFooter.STATE_ERRORS);
                }
                srl.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);

                srl.setRefreshing(false);
                xListView.setPullupEnable(true);
                xListView.stopLoadMore(XListViewFooter.STATE_ERRORS);
            }
        });
    }

    public class MyAdapter extends BaseAdapter<User> {
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

            //头像和昵称,语言
            if (!TextUtils.isEmpty(user.Avatar)) {
                CommonUtil.showBitmap(holder.iv_avatar, NetworkUtil.getFullPath(user.Avatar));
            } else {
                holder.iv_avatar.setImageResource(R.drawable.ic_launcher_student);
            }
            holder.iv_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (documentId > 1) {

                        //startActivityForResult();
                    } else {
                        //startActivity();

                    }


                    ActivityAlbum.start(getActivity(), new String[]{NetworkUtil.getFullPath(user.Avatar)}, 0);
                }
            });
            holder.tv_nickname.setText(user.Nickname);
            holder.tv_spoken.setText(user.Spoken);


            //颜色
            if (user.IsOnline) {
                if (user.IsEnable) {
                    int color = getResources().getColor(R.color.color_app);
                    holder.tv_nickname.setTextColor(color);
                    holder.tv_status.setTextColor(color);
                    holder.iv_status.setBackgroundColor(color);
                    holder.iv_status.setImageResource(R.drawable.teacher_online);
                    holder.tv_status.setText(R.string.FragmentChoose_tips_online);
                } else {
                    int color = getResources().getColor(R.color.teacher_busy);
                    holder.tv_nickname.setTextColor(color);
                    holder.tv_status.setTextColor(color);
                    holder.iv_status.setBackgroundColor(color);
                    holder.iv_status.setImageResource(R.drawable.teacher_busy);
                    holder.tv_status.setText(R.string.FragmentChoose_tips_busy);
                }
            } else {
                int color = getResources().getColor(R.color.color_app_normal);
                holder.tv_nickname.setTextColor(color);
                holder.tv_status.setTextColor(color);
                holder.iv_status.setBackgroundColor(color);
                holder.iv_status.setImageResource(R.drawable.teacher_offline);
                holder.tv_status.setText(R.string.FragmentChoose_tips_offline);
            }

            //设置点击
            holder.bt_call.setBackgroundResource(user.IsOnline ? R.drawable.selector_choose_calla : R.drawable.selector_choose_callb);
            holder.bt_call.setEnabled(user.IsEnable);
            holder.bt_call.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    teacher = user;
                    //如果没有登录,那么要求登录
                    if (NIMClient.getStatus() != StatusCode.LOGINED) {
                        showLoginDialog();
                        return;
                    }

                    //安卓6.0运行时权限检查
                    boolean isGranted = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

                    if (!isGranted) {
/*                        if (!permissionCheck) {
                            permissionCheck = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Unavailable");
                            builder.setMessage("You must turn on the microphone before calling the tutor. Please enter  Settings options to allow ChineseChat's access to your microphone");
                            builder.show();
                            return;
                        }*/
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUESTCODE_RECORD_AUDIO);
                    } else {
                        callTeacher(user);
                    }
                }
            });
            return convertView;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUESTCODE_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTeacher(teacher);
            } else {
                Toast.makeText(getActivity(), "when you call a teacher you must open the microphone!", Toast.LENGTH_SHORT).show();
            }
        }
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

        HttpUtil.Parameters parameters = new HttpUtil.Parameters();
        parameters.add("id", ChineseChat.CurrentUser.Id);
        parameters.add("target", teacher.Id);
        HttpUtil.post(NetworkUtil.chooseTeacher, parameters, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Response<ChooseTeacherModel> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<ChooseTeacherModel>>() {
                }.getType());
                if (resp.code == 200) {

                    //兼容老版本写法1.1.7及之前,当教师是1.1.7之前的版本,要把学生的信息传递给教师
                    JsonObject json = new JsonObject();
                    json.addProperty("Id", resp.info.Student.Id);
                    json.addProperty("Avatar", resp.info.Student.Avatar);
                    json.addProperty("Nickname", resp.info.Student.Nickname);
                    json.addProperty("Country", resp.info.Student.Country);

                    //新版本要求显示教师及学生明细1.1.8版本
                    JsonObject student = new JsonObject();
                    student.addProperty("Id", resp.info.Student.Id);
                    student.addProperty("Avatar", resp.info.Student.Avatar);
                    student.addProperty("Nickname", resp.info.Student.Nickname);
                    student.addProperty("Country", resp.info.Student.Country);

                    JsonObject studentSummary = new JsonObject();
                    studentSummary.addProperty("month", resp.info.Student.Summary.month);
                    studentSummary.addProperty("count", resp.info.Student.Summary.count);
                    studentSummary.addProperty("duration", resp.info.Student.Summary.duration);
                    student.add("Summary", studentSummary);
                    json.add("Student", student);

                    JsonObject teacher = new JsonObject();
                    teacher.addProperty("Id", resp.info.Teacher.Id);
                    teacher.addProperty("Avatar", resp.info.Teacher.Avatar);
                    teacher.addProperty("Nickname", resp.info.Teacher.Nickname);
                    teacher.addProperty("Country", resp.info.Teacher.Country);

                    JsonObject teacherSummary = new JsonObject();
                    teacherSummary.addProperty("month", resp.info.Teacher.Summary.month);
                    teacherSummary.addProperty("count", resp.info.Teacher.Summary.count);
                    teacherSummary.addProperty("duration", resp.info.Teacher.Summary.duration);
                    teacher.add("Summary", teacherSummary);
                    json.add("Teacher", teacher);

                    ChatData chatData = new ChatData();
                    chatData.setAccid(resp.info.Teacher.Accid);
                    chatData.setExtra(json.toString());
                    chatData.setChatType(AVChatType.AUDIO);

                    if (documentId > 0) {
                        Intent intent = new Intent(getContext(), ActivityChat.class);
                        intent.putExtra(ActivityChat.KEY_CHAT_MODE, ActivityChat.CHAT_MODE_OUTGOING);
                        intent.putExtra(ActivityChat.KEY_CHAT_MODE, chatData);
                        startActivityForResult(intent, REQUEST_CODE_DOCUMENT);
                    } else {
                        ActivityChat.start(getActivity(), ActivityChat.CHAT_MODE_OUTGOING, chatData);
                    }


                    //ActivityCall.start(getActivity(), resp.info.Id, resp.info.Accid, resp.info.Avatar, resp.info.Nickname, resp.info.Username, ActivityCall.CALL_TYPE_AUDIO);
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
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i(TAG, "onFailure: " + msg);
                CommonUtil.toast(R.string.network_error);
            }
        });
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

    /*
     *学生选择教师返回来的JSON帮忙类
     */
    private class ChooseTeacherModel {
        public User Student;
        public User Teacher;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DOCUMENT) {
            getActivity().finish();
        }
    }
}
