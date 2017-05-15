package com.bopinjia.customer.activity;

import com.bopinjia.customer.R;
import com.bopinjia.customer.util.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ActivityUserSecurity extends BaseActivity {

	private String bindingPhone;

	public static ActivityUserSecurity instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_user_security);

		instance = this;

		if (!StringUtils.isNull(getLoginPhone())) {
			bindingPhone = getLoginPhone().substring(0, 4) + "****" + getLoginPhone().substring(8, 11);
			((TextView) findViewById(R.id.phone)).setText(bindingPhone);
		} else {
		}

		findViewById(R.id.change_pass).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forward(ActivityChangPass.class);

			}
		});

		findViewById(R.id.change_phone).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forward(ActivityChangePhoneOne.class);
			}
		});

		findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backward();
			}
		});

	}

}
