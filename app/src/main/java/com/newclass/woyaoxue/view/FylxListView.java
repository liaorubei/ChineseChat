package com.newclass.woyaoxue.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.newclass.woyaoxue.util.Log;
import com.voc.woyaoxue.R;

public class FylxListView extends ListView
{

	private View footerView;
	private View headerView;

	public FylxListView(Context context)
	{
		this(context, null);

	}

	public FylxListView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);

	}

	public FylxListView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		headerView = View.inflate(context, R.layout.fylxlistview_header, null);
		footerView = View.inflate(context, R.layout.fylxlistview_footer, null);
		headerView.measure(0, 0);// 手动测量
		int headerMeasuredHeight = headerView.getMeasuredHeight();
		//
		headerView.setPadding(0, -headerMeasuredHeight, 0, 0);
		headerView.invalidate();

		//addHeaderView(headerView);
		//addFooterView(footerView);

	}

}
