package com.newclass.woyaoxue.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.newclass.woyaoxue.adapter.AdapterFolder;
import com.newclass.woyaoxue.base.BaseAdapter;
import com.newclass.woyaoxue.bean.Folder;
import com.newclass.woyaoxue.bean.Response;
import com.newclass.woyaoxue.util.CommonUtil;
import com.newclass.woyaoxue.util.HttpUtil;
import com.newclass.woyaoxue.util.HttpUtil.Parameters;
import com.newclass.woyaoxue.util.Log;
import com.newclass.woyaoxue.util.NetworkUtil;
import com.voc.woyaoxue.R;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ContentViewFolder extends ContentView
{
	protected static final String TAG = "ContentViewFolder";
	private int mLevelId;
	private List<Folder> list;
	private SwipeRefreshLayout srl;
	private BaseAdapter<Folder> adapter;
	private Gson gson = new Gson();
	private String take = "15";

	public ContentViewFolder(Context context, int levelId)
	{
		super(context);
		this.mLevelId = levelId;
		initData();
	}

	public void initData()
	{
		Parameters parameters = new Parameters();
		parameters.add("levelId", mLevelId + "");
		parameters.add("skip", 0 + "");
		parameters.add("take", take);
		Log.i(TAG, "parameters:" + parameters.get());
		HttpUtil.post(NetworkUtil.folderGetByLevelId, parameters, new RequestCallBack<String>()
		{

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo)
			{
				Response<List<Folder>> resp = gson.fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>()
				{}.getType());
				if (resp.code == 200)
				{
					List<Folder> folders = resp.info;
					list.clear();
					for (Folder folder : folders)
					{
						list.add(folder);
					}
				}
				adapter.notifyDataSetChanged();
				srl.setRefreshing(false);
				showView(ViewState.SUCCESS);
			}

			@Override
			public void onFailure(HttpException error, String msg)
			{
				CommonUtil.toast("加载失败");
				srl.setRefreshing(false);
				showView(ViewState.FAILURE);
			}
		});
	}

	@Override
	public View onCreateSuccessView()
	{
		View inflate = View.inflate(getContext(), R.layout.contentview_folder, null);
		srl = (SwipeRefreshLayout) inflate.findViewById(R.id.srl);
		srl.setColorSchemeColors(R.color.color_red_f04c62, R.color.color_green_00ae8c, R.color.color_blue_0888ff);
		srl.setOnRefreshListener(new OnRefreshListener()
		{

			@Override
			public void onRefresh()
			{
				initData();
			}
		});
		ListView listview = (ListView) inflate.findViewById(R.id.listview);
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Folder folder = list.get(position);
			//	start

			}
		});

		list = new ArrayList<Folder>();
		adapter = new AdapterFolder(list, getContext());
		listview.setAdapter(adapter);

		return inflate;
	}

}
