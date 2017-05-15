package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.view.View;

public class AdapterUseRedList extends CommonAdapter{
	List<DropRedBean> list;
	Context context;
	int layoutId;
	public AdapterUseRedList(List list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {
		// TODO Auto-generated method stub
		holder.setText(R.id.tv_amount, "¥" + list.get(position).getAmount());
		holder.setText(R.id.tv_red_type, list.get(position).getRed_type());
		holder.setText(R.id.tv_red_detail, list.get(position).getRed_detail());
		holder.setText(R.id.tv_red_time, list.get(position).getRed_time());
		
		if (list.get(position).getRed_isuse().equals("0")) {
			holder.getView(R.id.tv_use).setVisibility(View.VISIBLE);
		}else if (list.get(position).getRed_isuse().equals("1")) {
			holder.getView(R.id.tv_use).setVisibility(View.GONE);
		}
		
	}

}
