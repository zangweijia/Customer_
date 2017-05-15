package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.bean.ReflectBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.widget.TextView;

public class AdapterReflectList extends CommonAdapter {

	List<ReflectBean> list;
	Context context;
	int layoutId;

	public AdapterReflectList(List<ReflectBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {

		holder.setText(R.id.tv_time, list.get(position).getTime());

		holder.setText(R.id.tv_price, list.get(position).getPrice());

		holder.setText(R.id.tv_state, list.get(position).getState());

		if (list.get(position).getState().equals("审核中")) {
			((TextView) holder.getView(R.id.tv_state)).setTextColor(context.getResources().getColor(R.color.main_color));
		} else {
			((TextView) holder.getView(R.id.tv_state)).setTextColor(context.getResources().getColor(R.color.bg_666666));
		}

	}

}
