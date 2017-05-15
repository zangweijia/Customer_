package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.DayDropRedBean;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;

public class AdapterDayDropRed extends CommonAdapter {

	List<DayDropRedBean> list;
	Context context;
	int layoutId;

	public AdapterDayDropRed(List<DayDropRedBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {
		holder.setText(R.id.tv_amount, list.get(position).getAmount());
		holder.setText(R.id.tv_red_type, list.get(position).getRed_type());
		holder.setText(R.id.tv_red_detail, list.get(position).getRed_detail());
		holder.setText(R.id.tv_red_time, list.get(position).getRed_time());

	}

}
