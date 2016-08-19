package com.hanwen.chinesechat.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanwen.chinesechat.ChineseChat;
import com.hanwen.chinesechat.R;
import com.hanwen.chinesechat.activity.ActivityDocsDone;
import com.hanwen.chinesechat.activity.ActivityDocsTodo;
import com.hanwen.chinesechat.bean.Folder;
import com.hanwen.chinesechat.bean.Level;
import com.hanwen.chinesechat.bean.Response;
import com.hanwen.chinesechat.database.Database;
import com.hanwen.chinesechat.util.CommonUtil;
import com.hanwen.chinesechat.util.GsonUtil;
import com.hanwen.chinesechat.util.HttpUtil;
import com.hanwen.chinesechat.util.Log;
import com.hanwen.chinesechat.util.NetworkUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.netease.cosine.core.Params;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p/>
 * create an instance of this fragment.
 */
public class FragmentFolder extends Fragment {

    public static final String KEY_LEVEL = "KEY_LEVEL";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FragmentFolder";
    private Level level;
    private String mParam2;
    private RecyclerView.LayoutManager layout;
    private RecyclerView listView;
    private RecyclerView.Adapter adapter;
    private List<Folder> dataSet;
    private android.support.v7.widget.RecyclerView.ItemDecoration decora;
    private SwipeRefreshLayout srl;
    private DisplayMetrics displayMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            level = getArguments().getParcelable(KEY_LEVEL);
            mParam2 = getArguments().getString(ARG_PARAM2);
        } else {
            level = new Level();
        }
        displayMetrics = getActivity().getResources().getDisplayMetrics();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume: level=" + level);

        if (level.Id < 0) {
            List<Folder> folders = ChineseChat.database().folderSelectListWithDocsCount();
            dataSet.clear();
            for (Folder folder : folders) {
                if (folder.DocsCount > 0) {
                    dataSet.add(folder);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: " + level);
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        srl.setColorSchemeResources(R.color.color_app);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (level.Id > 0) {
                    HttpUtil.Parameters params = new HttpUtil.Parameters();
                    params.add("levelId", level.Id);
                    params.add("skip", 0);
                    params.add("take", 25);
                    HttpUtil.post(NetworkUtil.folderGetByLevelId, params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Response<List<Folder>> resp = GsonUtil.Instance().fromJson(responseInfo.result, new TypeToken<Response<List<Folder>>>() {}.getType());
                            if (resp.code == 200 && resp.info != null) {
                                dataSet.clear();
                                for (Folder f : resp.info) {
                                    ChineseChat.database().folderInsertOrReplace(f);
                                    dataSet.add(f);
                                }
                                adapter.notifyDataSetChanged();
                            }
                            srl.setRefreshing(false);
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            srl.setRefreshing(false);
                        }
                    });
                } else if (level.Id < 0) {
                    onResume();
                    srl.setRefreshing(false);
                }
            }
        });

        listView = (RecyclerView) view.findViewById(R.id.list);
        if (level.ShowCover == 1) {
            decora = new DividerGridItemDecoration(getActivity());
            layout = new GridLayoutManager(getActivity(), 3);
        } else {
            decora = new MyDecora(getActivity(), LinearLayoutManager.VERTICAL);
            layout = new LinearLayoutManager(getActivity());
        }
        listView.setLayoutManager(layout);
        listView.addItemDecoration(decora);

        if (level.Folders == null) {
            level.Folders = new ArrayList<>();
        }

        dataSet = level.Folders;
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.listitem_folder1, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Folder folder = dataSet.get(position);
            holder.tv_folder.setText(folder.Name);
            holder.tv_counts.setText(String.format("课程：%1$d", folder.DocsCount));

            if (level.ShowCover == 1) {
                if (!TextUtils.isEmpty(folder.Cover)) {
                    CommonUtil.showBitmap(holder.iv_covers, NetworkUtil.getFullPath(folder.Cover));
                } else {
                    holder.iv_covers.setImageBitmap(null);
                }
                holder.ll_title.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_covers.setVisibility(View.GONE);
            }

            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (level.Id > 0) {
                        Intent intent = new Intent(getActivity(), ActivityDocsTodo.class);
                        intent.putExtra("folder", new Gson().toJson(folder));
                        startActivity(intent);
                    } else {
                        ActivityDocsDone.start(getActivity(), folder);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_folder;
        TextView tv_counts;
        ImageView iv_covers;
        View ll_title;
        View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_folder = (TextView) itemView.findViewById(R.id.tv_folder);
            tv_counts = (TextView) itemView.findViewById(R.id.tv_counts);
            iv_covers = (ImageView) itemView.findViewById(R.id.iv_covers);
            ll_title = itemView.findViewById(R.id.ll_title);
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
