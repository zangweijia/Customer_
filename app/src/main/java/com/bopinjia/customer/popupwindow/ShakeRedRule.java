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

public class ShakeRedRule extends PopupWindow{
	
	private View mMenuView;
	public TextView mTryAgain;
	
	public ShakeRedRule(Context context) {
		super(context);
	}

	public ShakeRedRule(Activity context, View.OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.wj_popupwindow_rule, null);
		
		mTryAgain = (TextView) mMenuView.findViewById(R.id.tv_konw);
		
		mTryAgain.setOnClickListener(itemsOnClick);
	 

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