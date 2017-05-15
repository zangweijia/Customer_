package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityFXDisPay;
import com.bopinjia.customer.activity.ActivityJoin;
import com.bopinjia.customer.bean.DistributionLevelBean;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AdapterDistrbutionLevel extends CommonAdapter {

	List<DistributionLevelBean> list;
	Context context;
	int layoutId;

	public AdapterDistrbutionLevel(List<DistributionLevelBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, final int position) {

		holder.setText(R.id.tv_level_name, list.get(position).getName());

		holder.setText(R.id.tv_price, list.get(position).getPrice());

		holder.setText(R.id.tv_month, list.get(position).getMonth());

		holder.setText(R.id.tv_market, "原价：" + list.get(position).getMarket());
		TextView m = holder.getView(R.id.tv_market);
		m.getPaint().setStrikeThruText(true);

		holder.setImageURl(R.id.iv_img, list.get(position).getImg());

		if (list.get(position).getType().equals("1")) {
			holder.setImageResource(R.id.iv_corner, R.drawable.ic_distribution_th);
			holder.getView(R.id.iv_corner).setVisibility(View.VISIBLE);
		} else if (list.get(position).getType().equals("2")) {
			holder.setImageResource(R.id.iv_corner, R.drawable.ic_distribution_cz);
			holder.getView(R.id.iv_corner).setVisibility(View.VISIBLE);
		} else {
			holder.getView(R.id.iv_corner).setVisibility(View.INVISIBLE);
		}
		if (list.get(position).getEntertype().equals("1")) {
			// 申请

		} else if (list.get(position).getEntertype().equals("3")) {
			// 升级
			int Level = Integer.parseInt(list.get(position).getLevel());
			
			int nowLevel = Integer.parseInt(list.get(position).getNowlevel());
			
			
			
			if (Level>=nowLevel) {
				((TextView) holder.getView(R.id.btn_kt)).setText("已开通");
				((TextView) holder.getView(R.id.btn_kt)).setEnabled(false);
				((TextView) holder.getView(R.id.btn_kt)).setBackgroundResource(R.drawable.bg_778899_6);
			} else {
				((TextView) holder.getView(R.id.btn_kt)).setText("升 级");
				((TextView) holder.getView(R.id.btn_kt)).setEnabled(true);
			}

		} else if (list.get(position).getEntertype().equals("2")) {
			// 续费
			
			
		}

		holder.getView(R.id.btn_kt).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("name", list.get(position).getName());
				i.putExtra("id", list.get(position).getId());
				i.putExtra("price", list.get(position).getPrice());
				i.putExtra("type", list.get(position).getEntertype());
				i.setClass(context, ActivityFXDisPay.class);
				context.startActivity(i);
			}
		});

	}

}
