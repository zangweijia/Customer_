package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ConfirmNoProductBean;
import com.bopinjia.customer.bean.DayDropRedBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;

public class AdapterConfirmNoProduct extends CommonAdapter {

	List<ConfirmNoProductBean> list;
	Context context;
	int layoutId;

	public AdapterConfirmNoProduct(List<ConfirmNoProductBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {
		holder.setText(R.id.tv_name, list.get(position).getName());
		holder.setText(R.id.tv_number, list.get(position).getNumber());
		holder.getView(R.id.iv_no_product).setAlpha(0.6f);
		holder.setImageURl(R.id._iv_list_thumbnails, list.get(position).getImg());
	}

}
