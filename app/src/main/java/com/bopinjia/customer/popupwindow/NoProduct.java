package com.bopinjia.customer.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterConfirmNoProduct;
import com.bopinjia.customer.bean.ConfirmNoProductBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoProduct extends PopupWindow {
	private View mMenuView;
	public TextView mLeftButton;
	public TextView mRightButton;
	public ListView list;
	public Context context;

	public NoProduct(Context context) {
		super(context);
		this.context = context;
	}

	public NoProduct(Activity context, View.OnClickListener itemsOnClick, int i,  JSONArray result) {
		super(context);
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.wj_popupwindow_no_product, null);

		mLeftButton = (TextView) mMenuView.findViewById(R.id.bt_cancel);
		mRightButton = (TextView) mMenuView.findViewById(R.id.bt_send);
		list = (ListView) mMenuView.findViewById(R.id.list);

		if (i == 0) {
			// 全部
			mLeftButton.setText("更改收货地址");
		} else {
			mLeftButton.setText("移除无货商品");
		}

		mLeftButton.setOnClickListener(itemsOnClick);
		mRightButton.setOnClickListener(itemsOnClick);

		setlist(result, list);

		this.setContentView(mMenuView);
		this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
		this.setFocusable(false);
		this.setAnimationStyle(R.style.animSimplyfade);
		this.setOutsideTouchable(false);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		this.setBackgroundDrawable(dw);

	}

	private void setlist(JSONArray dataArray, ListView listview) {

		try {
			if (dataArray != null && dataArray.length() > 0) {
				List<ConfirmNoProductBean> list = new ArrayList<ConfirmNoProductBean>();
				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);

					ConfirmNoProductBean Rfb = new ConfirmNoProductBean();
					Rfb.setImg(data.getString("ProductThumbnail"));
					Rfb.setName(data.getString("SkuName"));
					Rfb.setNumber("x" + data.getString("Quantity"));
					list.add(Rfb);

				}

				AdapterConfirmNoProduct mAdapter = new AdapterConfirmNoProduct(list, context, R.layout.wj_item_no_stock);
				listview.setAdapter(mAdapter);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
