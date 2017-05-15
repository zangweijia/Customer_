package com.bopinjia.customer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.StringUtils;

public class ActivityInformation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        findViewById(R.id.btn_return).setOnClickListener(this);

        TextView txtMessage = (TextView) findViewById(R.id.txt_msg);
        String msg = getIntent().getStringExtra(Constants.INTENT_EXTRA_MSG);
        if (!StringUtils.isNull(msg)) {
            txtMessage.setText(msg);
        } else {
            txtMessage.setVisibility(View.GONE);
        }

        TextView txtMessageInfo = (TextView) findViewById(R.id.txt_info);
        String msgInfo = getIntent().getStringExtra(Constants.INTENT_EXTRA_MSG_INFO);
        if (!StringUtils.isNull(msgInfo)) {
            txtMessageInfo.setText(msgInfo);
        } else {
            txtMessageInfo.setVisibility(View.GONE);
        }

        String msgStatus = getIntent().getStringExtra(Constants.INTENT_EXTRA_MSG_STATUS);
        if (Constants.MSG_STATUS_FAIL.equals(msgStatus)) {
            ((ImageView) findViewById(R.id.icon_image)).setImageResource(R.drawable.bg_info_fail);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
        default:
        case R.id.btn_return:

            if (mScreenManager.getLastActivity() != null) {
                forward(mScreenManager.getLastActivity().getClass());
                mScreenManager.setLastActivity(null);
            } else {
                backward();
            }

            break;
        }
    }
    @Override
	protected void onResume() {
		super.onResume();

		new Handler().postDelayed(new Runnable() {

			/**
			 * run the handler
			 */
			@Override
			public void run() {
				
				
				 if (mScreenManager.getLastActivity() != null) {
		                forward(mScreenManager.getLastActivity().getClass());
		                mScreenManager.setLastActivity(null);
		            } else {
		                backward();
		            }
			}
		}, 2000);

	}

}
