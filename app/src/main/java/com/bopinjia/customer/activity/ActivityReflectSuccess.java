package com.bopinjia.customer.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bopinjia.customer.R;

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
                ActivityFXReflect.instance.finish();
                finish();

            }
        });
        TextView mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

        mTiTleName.setText("提现申请");
        findViewById(R.id.tv_next).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ActivityFXReflect.instance.finish();
                finish();
            }
        });

    }

}
