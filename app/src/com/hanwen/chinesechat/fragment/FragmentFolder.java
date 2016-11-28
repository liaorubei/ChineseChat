package com.hanwen.chinesechat.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityDocsDone;
import com.hanwen.chinesechat.activity.ActivityDocsTodo;
import com.hanwen.chinesechat.activity.ActivityFolder;
import com.hanwen.chinesechat.bean.Document;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.FolderDoc;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.hanwen.chinesechat.view.SquareLayout;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 课本列表
 */
public class FragmentFolder extends Fragment implements OnRefreshListener {
    public static final String TAG = "FragmentFolder";
    public static final String KEY_LEVEL = "KEY_LEVEL";
    public static final int TAKE = 25;
    private Dialog dialogPermission;
    private Dialog dialogProgress;
    private Level level;
    private List<FolderDoc> data = new ArrayList<>();
    private RecyclerView.Adapter adapter = new MyAdapter();
    private SwipeToLoadLayout swipe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        level = getArguments() == null ? new Level() : (Level) getArguments().getParcelable(KEY_LEVEL);
        dialogProgress = new Dialog(getActivity(), R.style.NoTitle);
        dialogProgress.setContentView(R.layout.dialog_loading);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.FragmentFolder_authorized_users);
        builder.setPositiveButton(R.string.FragmentFolder_dialog_positive, null);
        dialogPermission = builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipe = (SwipeToLoadLayout) view.findViewById(R.id.swipe);
        if (level.ShowCover == 1) {
            swipe.setBackgroundColor(Color.parseColor("#BEEDD5"));
        }
        swipe.setOnRefreshListener(this);
        swipe.setRefreshing(level.Id > 0);//如果Id大于0，那么说明该数据来源于网络，会进行异步请求
        swipe.setLoadMoreEnabled(false);

        RecyclerView listView = (RecyclerView) view.findViewById(R.id.swipe_target);
        listView.setLayoutManager(level.ShowCover == 1 ? new GridLayoutManager(view.getContext(), 3) : new LinearLayoutManager(view.getContext()));
        listView.addItemDecoration(level.ShowCover == 1 ? new DividerGridItemDecoration(view.getContext()) : new MyDecora(view.getContext(), LinearLayoutManager.VERTICAL));

        data.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

/*    @Override
    public void onResume() {
        super.onResume();
        if (level.Id < 0) {
            List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
            Log.i(TAG, "onResume: " + folders);
            data.clear();
            for (Folder folder : folders) {
                if (folder.DocsCount > 0) {
                    FolderDoc fd = new FolderDoc(folder);
                    fd.Name1 = folder.Name;
                    fd.Name2 = "课程：" + folder.DocsCount;
                    data.add(fd);
                }
            }
            adapter.notifyDataSetChanged();
            swipe.setRefreshEnabled(false);
        }
    }*/

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh: ");
        HttpUtil.Parameters params = new HttpUtil.Parameters();
        params.add("levelId", level.Id);
        HttpUtil.post(NetworkUtil.folderGetListByLevelId, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //Log.i(TAG, "onSuccess: " + responseInfo.result + " \r\n" + this.getRequestUrl());

                Response<List<Folder>> resp = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                if (200 == resp.code) {
                    data.clear();
                    for (Folder f : resp.info) {
                        FolderDoc object = new FolderDoc(f);
                        object.Name2 = level.ShowCover == 1 ? f.NameSubCn : String.format("课程：%1$2d", f.DocsCount);
                        data.add(object);
                    }
                    adapter.notifyDataSetChanged();
                }
                swipe.setRefreshing(false);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                swipe.setRefreshing(false);
            }
        });
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.listitem_folder1, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FolderDoc folderDoc = data.get(position);
            holder.tv_folder.setText(folderDoc.Name1);
            holder.tv_counts.setText(folderDoc.Name2);
            if (level.ShowCover == 1) {
                holder.tv_folder.setGravity(Gravity.CENTER);
                holder.tv_counts.setGravity(Gravity.CENTER);
                if (!TextUtils.isEmpty(folderDoc.Cover)) {
                    CommonUtil.showIcon(getContext(), holder.iv_covers, folderDoc.Cover);
                } else {
                    holder.iv_covers.setImageResource(R.drawable.ic_launcher_student);
                }
            } else {
                holder.sl_cover.setVisibility(View.GONE);
            }

            holder.rootView.setOnClickListener(new MyOnClickListener(folderDoc) {
                @Override
                public void onClick(View v) {
                    //打开未下载
                    if (level.Id > 0) {
                        if (this.Data.HasChildren) {
                            openKids(this.Data);
                        } else {
                            checkPermission(this.Data);
                        }
                    }
                    //打开已下载
                    else {
                        Folder folder = new Folder();
                        folder.Id = this.Data.Id;
                        folder.Name = this.Data.Name1;
                        ActivityDocsDone.start(getActivity(), folder);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private void checkPermission(final FolderDoc data) {

        if (data.Permission) {
            HttpUtil.Parameters params = new HttpUtil.Parameters();
            params.add("folderId", data.Id);
            params.add("userId", ChineseChat.CurrentUser.Id);
            HttpUtil.post(NetworkUtil.folderCheckPermission, params, new RequestCallBack<String>() {

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    //Log.i(TAG, "onSuccess: " + responseInfo.result + " \r\n" + this.getRequestUrl());
                    Response<List<Document>> response = new Gson().fromJson(responseInfo.result, new TypeToken<Response<List<Document>>>() {}.getType());
                    dialogProgress.dismiss();
                    if (200 == response.code) {
                        openDocs(data);
                    } else {
                        dialogPermission.show();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "onFailure: error" + error.getMessage() + " msg:" + msg);
                    dialogProgress.dismiss();
                }
            });

        } else {
            openDocs(data);
        }
    }

    private void openDocs(FolderDoc data) {
        Intent intent = new Intent(getActivity(), ActivityDocsTodo.class);
        Folder folder = new Folder();
        folder.Id = data.Id;
        folder.Name = data.Name1;
        intent.putExtra("folder", folder);
        intent.putExtra("showDate", level.ShowCover == 1);
        startActivity(intent);
    }

    private void openKids(FolderDoc data) {
        Intent intent = new Intent(getActivity(), ActivityFolder.class);
        Folder folder = new Folder();
        folder.Id = data.Id;
        folder.Name = data.Name1;
        intent.putExtra("folder", folder);
        intent.putExtra("showDate", level.ShowCover != 1);
        startActivity(intent);
    }

    private abstract class MyOnClickListener implements View.OnClickListener {
        protected FolderDoc Data;

        public MyOnClickListener(FolderDoc folderDoc) {
            this.Data = folderDoc;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_folder;
        TextView tv_counts;
        ImageView iv_covers;
        View ll_title;
        View rootView;
        public SquareLayout sl_cover;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_folder = (TextView) itemView.findViewById(R.id.tv_folder);
            tv_counts = (TextView) itemView.findViewById(R.id.tv_counts);
            iv_covers = (ImageView) itemView.findViewById(R.id.iv_covers);
            ll_title = itemView.findViewById(R.id.ll_title);
            sl_cover = (SquareLayout) itemView.findViewById(R.id.sl_cover);
            rootView = itemView;
        }
    }

    private class MyDecora extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public MyDecora(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }

    public class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private Drawable mDivider;

        public DividerGridItemDecoration(Context context) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        private int getSpanCount(RecyclerView parent) {
            // 列数
            int spanCount = -1;
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicWidth();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    if ((pos + 1) % spanCount == 0)// 如果是最后一列，则不需要绘制右边
                    {
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                        return true;
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                    return true;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                // StaggeredGridLayoutManager 且纵向滚动
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一行，则不需要绘制底部
                    if (pos >= childCount)
                        return true;
                } else
                // StaggeredGridLayoutManager 且横向滚动
                {
                    // 如果是最后一行，则不需要绘制底部
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            if (isLastRaw(parent, itemPosition, spanCount, childCount))// 如果是最后一行，则不需要绘制底部
            {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            } else if (isLastColum(parent, itemPosition, spanCount, childCount))// 如果是最后一列，则不需要绘制右边
            {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
            }
        }
    }
}
