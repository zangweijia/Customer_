package com.bopinjia.customer.activity;

import com.bopinjia.customer.R;
import com.bopinjia.customer.R.id;
import com.bopinjia.customer.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ActivityReflectSuccess extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_reflect_success);
		setTitle();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		TextView mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});
		TextView mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

		mTiTleName.setText("提现申请");

		findViewById(R.id.tv_next).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

}
