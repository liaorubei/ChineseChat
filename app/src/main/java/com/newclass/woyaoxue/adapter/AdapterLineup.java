package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.User;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * Created by liaorubei on 2016/1/14.
 */
public class AdapterLineup extends BaseAdapter<User> {
    private Context mContext;

    public AdapterLineup(List<User> list, Context context) {
        super(list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        String accid = mContext.getSharedPreferences("user", Context.MODE_PRIVATE).getString("accid", "");
        View inflate = View.inflate(mContext, R.layout.listitem_teacherqueue, null);
        TextView tv_nickname = (TextView) inflate.findViewById(R.id.tv_nickname);
        TextView tv_username = (TextView) inflate.findViewById(R.id.tv_username);
        TextView tv_category = (TextView) inflate.findViewById(R.id.tv_category);

        tv_nickname.setText(user.Name + (accid.equals(user.Accid) ? "(本人)" : ""));
        tv_username.setText(user.Username);
        tv_category.setText(user.Category == 1 ? "教师" : "学生");

        tv_username.setTextColor(accid.equals(user.Accid) ? Color.RED : Color.BLACK);
        return inflate;
    }
}
