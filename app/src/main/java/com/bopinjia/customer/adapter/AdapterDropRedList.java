package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.view.View;

public class AdapterDropRedList extends CommonAdapter {

	List<DropRedBean> list;
	Context context;
	int layoutId;

	public AdapterDropRedList(List<DropRedBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {

		String mtype = list.get(position).getRed_type();

		holder.setText(R.id.tv_amount, "¥" + list.get(position).getAmount());
		holder.setText(R.id.tv_red_type, mtype);
		holder.setText(R.id.tv_red_detail, list.get(position).getRed_detail());
		holder.setText(R.id.tv_red_time, list.get(position).getRed_time());

		if (mtype.equals("消费红包")) {
			holder.getView(R.id.rl_bg).setBackgroundResource(R.drawable.bg_red_xf);
			holder.getView(R.id.v).setVisibility(View.VISIBLE);
		} else {
			holder.getView(R.id.rl_bg).setBackgroundResource(R.drawable.ic_red_p);
			holder.getView(R.id.v).setVisibility(View.GONE);
		}

		if (list.get(position).getRed_isuse().equals("0")) {
			holder.getView(R.id.iv_isuse).setVisibility(View.GONE);
			holder.getView(R.id.iv_expired).setVisibility(View.GONE);
		} else if (list.get(position).getRed_isuse().equals("1")) {
			holder.getView(R.id.iv_isuse).setVisibility(View.VISIBLE);
			holder.getView(R.id.iv_expired).setVisibility(View.GONE);
		} else if (list.get(position).getRed_isuse().equals("2")) {
			holder.getView(R.id.iv_isuse).setVisibility(View.GONE);
			holder.getView(R.id.iv_expired).setVisibility(View.VISIBLE);
		}

	}

}
