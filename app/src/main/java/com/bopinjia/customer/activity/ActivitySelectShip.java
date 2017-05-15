package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.List;

import com.bopinjia.customer.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class ActivitySelectShip extends BaseActivity {
	private List<RadioButton> radionButtonList = new ArrayList<RadioButton>();
	private RadioButton mChKPeisong, mChkZiti;
	private String tyid = "1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_select_ship);

		mChKPeisong = (RadioButton) findViewById(R.id.chk_peisong);
		mChKPeisong.setOnClickListener(this);
		radionButtonList.add(mChKPeisong);
		mChkZiti = (RadioButton) findViewById(R.id.chk_ziti);
		mChkZiti.setOnClickListener(this);
		radionButtonList.add(mChkZiti);

		if (getIntent().getStringExtra("tyId").equals("1")) {
			mChkZiti.setChecked(true);
		} else if (getIntent().getStringExtra("tyId").equals("3")) {
			mChKPeisong.setChecked(true);
		}

		findViewById(R.id.btn_next).setOnClickListener(this);
		findViewById(R.id.btn_return).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_next:
			if (mChKPeisong.isChecked()) {
				tyid = "3";
			} else if (mChkZiti.isChecked()) {
				tyid = "1";
			}
			Intent data = new Intent();
			data.putExtra("tyId", tyid);
			setResult(3, data);
			finish();
			break;
		case R.id.btn_return:
			finish();
			break;
		case R.id.chk_peisong:
		case R.id.chk_ziti:
			for (RadioButton button : radionButtonList) {
				if (button.getId() != v.getId()) {
					button.setChecked(false);
				}
			}
			break;
		default:
			break;
		}
	}
}
