package com.bopinjia.customer.activity;

import com.bopinjia.customer.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ActivitySpecialWebView extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_web_view);

		setTitle();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		TextView mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		TextView mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTiTleName.setText("分销管理");
		mTiTleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}
}
