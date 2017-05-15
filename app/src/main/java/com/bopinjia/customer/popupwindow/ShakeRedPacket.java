package com.bopinjia.customer.popupwindow;

import com.bopinjia.customer.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShakeRedPacket extends PopupWindow {
	private View mMenuView;
	public TextView mRedAmount;
	public ImageView mClose;
	public ShakeRedPacket(Context context) {
		super(context);
	}

	public ShakeRedPacket(Activity context, View.OnClickListener itemsOnClick ,String str) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.wj_popupwindow_shake_red, null);
		
		mRedAmount = (TextView) mMenuView.findViewById(R.id.tv_red_amount);
		mClose = (ImageView) mMenuView.findViewById(R.id.iv_close);
		
		
		mMenuView.findViewById(R.id.tvtry_again).setOnClickListener(itemsOnClick);
		mMenuView.findViewById(R.id.tv_chakan).setOnClickListener(itemsOnClick);
		
		
		mRedAmount.setText(str);
		
		mClose.setOnClickListener(itemsOnClick);
	 

		this.setContentView(mMenuView);

		this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

		this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

		this.setFocusable(true);

		this.setAnimationStyle(R.style.AnimBottom);

		this.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(0xb0000000);

		this.setBackgroundDrawable(dw);

	}
}
