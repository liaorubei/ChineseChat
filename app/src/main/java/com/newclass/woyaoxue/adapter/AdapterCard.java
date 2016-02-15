package com.newclass.woyaoxue.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.NimSysNotice;
import com.newclass.woyaoxue.bean.Theme;
import com.voc.woyaoxue.R;

import java.util.List;

/**
 * Created by jb on 2016/1/15.
 */
public class AdapterCard extends BaseAdapter<Theme> {
    private Context mContext;

    public AdapterCard(List<Theme> cardList, Context context) {
        super(cardList);
        this.mContext = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Theme item = getItem(position);
        View inflate = View.inflate(this.mContext, R.layout.griditem_card, null);
        inflate.findViewById(R.id.iv_card).setVisibility(View.VISIBLE);
        TextView tv_theme = (TextView) inflate.findViewById(R.id.tv_theme);
        TextView tv_name = (TextView) inflate.findViewById(R.id.tv_name);
        tv_theme.setText(item.Name);
        tv_name.setText(item.Name);
        return inflate;
    }
}
