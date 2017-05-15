package com.bopinjia.customer.adapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.widget.EditText;

public class AdapterGoldCommissionPList extends CommonAdapter {

	List<CommissionProductBean> list;
	Context context;
	int layoutId;

	public AdapterGoldCommissionPList(List<CommissionProductBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {

		// 商品的国别
		holder.setText(R.id.tv_country, list.get(position).getCountry());

		// 商品名称
		holder.setText(R.id.tv_name, list.get(position).getName());

		// 商品价格
		holder.setText(R.id.tv_price, "¥" + list.get(position).getPrice());

		// 佣金
		holder.setText(R.id.tv_commission, "¥" + list.get(position).getCommission());
		
		//金牌佣金
		holder.setText(R.id.tv_gold_commission, "¥" + list.get(position).getGold_comission());
		

		// 购买人数
		holder.setText(R.id.tv_number, list.get(position).getNumber() + "人已购买");

		// 商品主图
		holder.setImageURl(R.id.iv_img, list.get(position).getImg());

		// 国家图片
		String ss = encode(list.get(position).getCountryimg());
		holder.setImageURl(R.id.iv_country, ss);

	}

	public static String encode(String url) {
		try {
			Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
			int count = 0;
			while (matcher.find()) {
				// System.out.println(matcher.group());
				String tmp = matcher.group();
				url = url.replaceAll(tmp, java.net.URLEncoder.encode(tmp, "gbk"));
			}
			// System.out.println(count);
			// url = java.net.URLEncoder.encode(url,"gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return url;
	}

}
