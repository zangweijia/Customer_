package com.bopinjia.customer.popupwindow;

import com.bopinjia.customer.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ShakeRedPacketNoJiHui extends PopupWindow {

	private View mMenuView;
	public ImageView mIKnow;
	public ImageView mClose;
	
	public ShakeRedPacketNoJiHui(Context context) {
		super(context);
	}

	public ShakeRedPacketNoJiHui(Activity context, View.OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.wj_popupwindow_shake_no_red, null);
		
		mIKnow = (ImageView) mMenuView.findViewById(R.id.iv_know);
		
		mClose = (ImageView) mMenuView.findViewById(R.id.iv_nored_close);

		 
		mIKnow.setOnClickListener(itemsOnClick);
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

