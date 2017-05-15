package com.bopinjia.customer.adapter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.util.SetPriceSize;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class AdapterProductGridViewClassSub extends CommonAdapter {

	List<ProductGridviewClassSubBean> list;
	Context context;
	int layoutId;

	public AdapterProductGridViewClassSub(List<ProductGridviewClassSubBean> list, Context context, int layoutId) {
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
		holder.setText(R.id.txt_product_name, list.get(position).getName());

		// 商品价格
		SetPriceSize.setPrice((TextView) holder.getView(R.id.txt_sell_price), "¥" + list.get(position).getPrice(), 25);

		// 市场价格
		holder.setText(R.id.txt_market_price, "¥" + list.get(position).getMarketprice());
		((TextView) holder.getView(R.id.txt_market_price)).getPaint().setStrikeThruText(true);

		//起订量
		holder.setText(R.id.txt_buy_number,  list.get(position).getNumber()+"人已购买" );

		// 商品主图
		holder.setImageURl(R.id.iv_product_thumbnails, list.get(position).getImg());

		// 国家图片
		String ss = encode(list.get(position).getCountryimg());
		holder.setImageURl(R.id.iv_country, ss);

		String RealStock = list.get(position).getRealStock();
		if (RealStock != null) {
			if (RealStock.equals("0")) {
				holder.getView(R.id.iv_no_product).setVisibility(View.VISIBLE);
			} else {
				holder.getView(R.id.iv_no_product).setVisibility(View.GONE);
			}
		}

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