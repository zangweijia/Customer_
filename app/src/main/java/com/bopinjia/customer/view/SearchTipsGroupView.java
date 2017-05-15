package com.bopinjia.customer.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import com.bopinjia.customer.R;

/**
 * Description:很多搜索界面弹出来的提示语 User: xjp Date: 2015/4/15 Time: 9:09
 */

public class SearchTipsGroupView extends LinearLayout {
	LinearLayout layout = null;
	LayoutParams layoutLp = null;
	private Context context;

	public SearchTipsGroupView(Context context) {
		super(context);
		this.context = context;
		setOrientation(VERTICAL);// 设置方向
	}

	public SearchTipsGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setOrientation(VERTICAL);// 设置方向
	}

	public SearchTipsGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		setOrientation(VERTICAL);// 设置方向
	}

	public void deleteAllView() {
		int size = this.getChildCount();
		for (int i = 0; i < size; i++) {
			this.removeViewAt(i);
		}
	}

	/**
	 * 外部接口调用
	 *
	 * @param items
	 * @param onItemClick
	 */
	public void initViews(String items[], final SearchTipsGroupViewOnItemClick onItemClick) {

		int length = 0;// 一行加载item 的宽度

		boolean isNewLine = true;// 是否换行

		int screenWidth = getScreenWidth();// 屏幕的宽度

		int size = items.length;

		for (int i = 0; i < size; i++) {// 便利items
			if (isNewLine) {// 是否开启新的一行
				layout = new LinearLayout(context);
				layout.setOrientation(HORIZONTAL);
				layoutLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				layoutLp.topMargin = 10;
			}

			View view = LayoutInflater.from(context).inflate(R.layout.wj_item_searchtipsgrid_textview, null);
			TextView itemView = (TextView) view.findViewById(R.id.text);
			itemView.setText(items[i]);

			final int j = i;
			itemView.setOnClickListener(new OnClickListener() {// 给每个item设置点击事件
				@Override
				public void onClick(View v) {
					if (null != onItemClick) {
						onItemClick.SearchTipsGroupViewonClick(j);
					}
				}
			});

			// 设置item的参数
			LayoutParams itemLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			itemLp.leftMargin = 10;

			// 得到当前行的长度
			length += 10 + getViewWidth(itemView);
			if (length > screenWidth) {// 当前行的长度大于屏幕宽度则换行
				length = 0;
				addView(layout, layoutLp);
				isNewLine = true;
				i--;
			} else {// 否则添加到当前行
				isNewLine = false;
				layout.addView(view, itemLp);
			}
		}
		addView(layout, layoutLp);
	}

	/**
	 * @param items
	 * @param onItemClick
	 */
	public void initViews(List<String> items, SearchTipsGroupViewOnItemClick onItemClick) {
		if (items != null) {
			if (!items.isEmpty()) {
				int size = items.size();
				String[] arr = (String[]) items.toArray(new String[size]);
				initViews(arr, onItemClick);
			}
		}

	}

	/**
	 * 得到手机屏幕的宽度
	 *
	 * @return
	 */
	private int getScreenWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	/**
	 * 得到view控件的宽度
	 *
	 * @param view
	 * @return
	 */
	private int getViewWidth(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return view.getMeasuredWidth();
	}
}