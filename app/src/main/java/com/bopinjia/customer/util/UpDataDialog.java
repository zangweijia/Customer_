package com.bopinjia.customer.util;

import com.bopinjia.customer.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class UpDataDialog extends PopupWindow {
	private View mMenuView;
	public Button mBtnOk, mBtnCancel;

	public UpDataDialog(Context context) {
		super(context);
	}

	public UpDataDialog(Activity context, View.OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.wj_updata_dialog, null);
		LinearLayout mm = (LinearLayout) mMenuView.findViewById(R.id.mm);
		mBtnOk = (Button) mMenuView.findViewById(R.id.bt_send);
		mBtnCancel = (Button) mMenuView.findViewById(R.id.bt_cancel);

		mBtnCancel.setOnClickListener(itemsOnClick);
		mBtnOk.setOnClickListener(itemsOnClick);
		mm.setOnClickListener(null);
		this.setContentView(mMenuView);

		this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

		this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);


		this.setAnimationStyle(R.style.AnimBottom);

		this.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(0xb0000000);

		this.setBackgroundDrawable(dw);

	}
}
