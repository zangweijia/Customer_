package com.bopinjia.customer.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bopinjia.customer.R;

/**
 * Created by ZWJ on 2017/6/7.
 */

public class AddCartSuccess extends PopupWindow {
    private View mMenuView;
    public TextView btn_jixu;
    public TextView btn_go_cart;
    public ImageView iv_close;

    public AddCartSuccess(Context context) {
        super(context);
    }

    public AddCartSuccess(Activity context, View.OnClickListener itemsOnClick ) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_activity_add_cart_success, null);

        btn_jixu = (TextView) mMenuView.findViewById(R.id.btn_jixu);
        btn_go_cart = (TextView) mMenuView.findViewById(R.id.btn_go_cart);
        iv_close = (ImageView) mMenuView.findViewById(R.id.iv_close);

        btn_jixu.setOnClickListener(itemsOnClick);
        btn_go_cart.setOnClickListener(itemsOnClick);
        iv_close.setOnClickListener(itemsOnClick);


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