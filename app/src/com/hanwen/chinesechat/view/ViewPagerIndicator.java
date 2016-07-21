package com.hanwen.chinesechat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanwen.chinesechat.R;

public class ViewPagerIndicator extends LinearLayout
{
	/**
	 * 标题选中时的颜色
	 */
	private static final int COLOR_TEXT_HIGHLIGHTCOLOR = Color.parseColor("#3498db");
	/**
	 * 标题正常时的颜色
	 */
	private static final int COLOR_TEXT_NORMAL = Color.parseColor("#95a5a6");
	/**
	 * 默认的Tab数量
	 */
	private static final int COUNT_DEFAULT_TAB = 4;
	/**
	 * 三角形的宽度为单个Tab的1/6
	 */
	private static final float RADIO_TRIANGEL = 1.0f / 6;

	/**
	 * 三角形的最大宽度
	 */
	private final int DIMENSION_TRIANGEL_WIDTH = (int) (getScreenWidth() / 3 * RADIO_TRIANGEL);
	/**
	 * 初始时，三角形指示器的偏移量
	 */
	private int mInitTranslationX;

	/**
	 * 绘制三角形的画笔
	 */
	private Paint mPaint;

	/**
	 * path构成一个三角形
	 */
	private Path mPath;

	/**
	 * tab数量
	 */
	private int mTabVisibleCount = COUNT_DEFAULT_TAB;

	/**
	 * 手指滑动时的偏移量
	 */
	private float mTranslationX;
	/**
	 * 三角形的高度
	 */
	private int mTriangleHeight;

	/**
	 * 三角形的宽度
	 */
	private int mTriangleWidth;
	/**
	 * 与之绑定的ViewPager
	 */
	public ViewPager mViewPager;

	// 对外的ViewPager的回调接口
	private ViewPager.OnPageChangeListener onPageChangeListener;

	public ViewPagerIndicator(Context context)
	{
		this(context, null);
	}

	public ViewPagerIndicator(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// 获得自定义属性，tab的数量
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
		mTabVisibleCount = a.getInt(R.styleable.ViewPagerIndicator_item_count, COUNT_DEFAULT_TAB);
		if (mTabVisibleCount < 0)
			mTabVisibleCount = COUNT_DEFAULT_TAB;
		a.recycle();

		// 初始化画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.parseColor("#ffffff"));
		mPaint.setStyle(Style.FILL);
		mPaint.setPathEffect(new CornerPathEffect(3));

	}

	/**
	 * 绘制指示器
	 */
	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		canvas.save();
		// 画笔平移到正确的位置
		canvas.translate(mInitTranslationX + mTranslationX, getHeight() + 1);
		canvas.drawPath(mPath, mPaint);
		canvas.drawRect(0, 25, getScreenWidth() / 4, 25, mPaint);
		canvas.restore();

		super.dispatchDraw(canvas);
	}

	/**
	 * 根据标题生成长度经过计算的TextView
	 * 
	 * @param text
	 * @return
	 */
	private TextView generateTextView(CharSequence text)
	{
		TextView textView = new TextView(getContext());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.width = getScreenWidth() / mTabVisibleCount;

		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(COLOR_TEXT_NORMAL);
		textView.setText(text);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		textView.setBackgroundResource(R.drawable.selector_levels);
		textView.setLayoutParams(lp);
		return textView;
	}

	/**
	 * 获得屏幕的宽度
	 * 
	 * @return
	 */
	public int getScreenWidth()
	{
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 初始化三角形指示器
	 */
	private void initTriangle()
	{
		mPath = new Path();

		mTriangleHeight = (int) (mTriangleWidth / 2 / Math.sqrt(2));
		mPath.moveTo(0, 0);
		mPath.lineTo(mTriangleWidth, 0);
		mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
		mPath.close();
	}

	/**
	 * 设置布局中view的一些必要属性；如果设置了setTabTitles，布局中view则无效
	 */
	@Override
	protected void onFinishInflate()
	{

		super.onFinishInflate();

		int cCount = getChildCount();

		if (cCount == 0)
			return;

		for (int i = 0; i < cCount; i++)
		{
			View view = getChildAt(i);
			LinearLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
			lp.weight = 0;
			lp.width = getScreenWidth() / mTabVisibleCount;
			view.setLayoutParams(lp);
		}
		// 设置点击事件
		refreshTitle();
	}

	/**
	 * 初始化三角形的宽度
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mTriangleWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGEL);// 1/6 of
																		// width
		mTriangleWidth = Math.min(DIMENSION_TRIANGEL_WIDTH, mTriangleWidth);

		// 初始化三角形
		initTriangle();

		// 初始时的偏移量
		mInitTranslationX = getWidth() / mTabVisibleCount / 2 - mTriangleWidth / 2;
	}

	/**
	 * 重置文本颜色
	 */
	private void resetTextViewColor()
	{
		if (mViewPager != null)
		{
			int currentItem = mViewPager.getCurrentItem();
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++)
			{
				((TextView) this.getChildAt(i)).setTextColor(i == currentItem ? COLOR_TEXT_HIGHLIGHTCOLOR : COLOR_TEXT_NORMAL);
			}
		}
	}

	/**
	 * 指示器跟随手指滚动，以及容器滚动
	 * 
	 * @param position
	 * @param offset
	 * @param positionOffsetPixels
	 */
	public void scroll(int position, float offset, int positionOffsetPixels)
	{
		mTranslationX = getWidth() / mTabVisibleCount * (position + offset);
		int tabWidth = getScreenWidth() / mTabVisibleCount;
		this.scrollTo((int) (((mViewPager.getScrollX() + 0.0) / getScreenWidth()) * ((tabWidth + 0.0) / mTabVisibleCount)), 0);
	}

	// 对外的ViewPager的回调接口的设置
	public void setOnPageChangeListener(OnPageChangeListener listener)
	{
		this.onPageChangeListener = listener;
	}

	// 设置关联的ViewPager
	@SuppressWarnings("deprecation")
	public void setViewPager(ViewPager viewPager, int pos)
	{
		this.mViewPager = viewPager;
		this.mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				// 滚动
				scroll(position, positionOffset, positionOffsetPixels);
				// 回调
				if (onPageChangeListener != null)
				{
					onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}

			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
				// 回调
				if (onPageChangeListener != null)
				{
					onPageChangeListener.onPageScrollStateChanged(state);
				}
			}

			@Override
			public void onPageSelected(int position)
			{
				// 设置字体颜色高亮
				resetTextViewColor();
				// 回调
				if (onPageChangeListener != null)
				{
					onPageChangeListener.onPageSelected(position);
				}
			}
		});

		refreshTitle();

		// 设置当前页
		viewPager.setCurrentItem(pos);
	}

	public void refreshTitle()
	{
		if (this.mViewPager != null)
		{

			PagerAdapter pagerAdapter = this.mViewPager.getAdapter();
			int count = pagerAdapter.getCount();

			// 清除所有的内容
			this.removeAllViews();
			for (int i = 0; i < count; i++)
			{
				final int item = i;
				TextView textView = generateTextView(pagerAdapter.getPageTitle(i));
				textView.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						mViewPager.setCurrentItem(item, true);
					}
				});
				this.addView(textView);
			}
		}

		resetTextViewColor();
	}

	/**
	 * 设置可见的tab的数量
	 * 
	 * @param count
	 */
	public void setVisibleTabCount(int count)
	{
		this.mTabVisibleCount = count;
	}

}
