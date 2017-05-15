package com.bopinjia.customer.activity;

import com.bopinjia.customer.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityDayDropRed extends BaseActivity {

	private String userid;
	private String mPhone;
	private String mD5Pass;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_day_drop_redd);
		userid = getIntent().getStringExtra("userid");
		mPhone = getIntent().getStringExtra("phone");
		mD5Pass = getIntent().getStringExtra("pas");
		findViewById(R.id.tv_ll).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("userid", userid);
				i.putExtra("phone", mPhone);
				i.putExtra("pas", mD5Pass);
				forward(ActivityNewPersonDropRed.class, i);
				finish();
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.putExtra("userid", userid);
			i.putExtra("phone", mPhone);
			i.putExtra("pas", mD5Pass);
			forward(ActivityNewPersonDropRed.class, i);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
