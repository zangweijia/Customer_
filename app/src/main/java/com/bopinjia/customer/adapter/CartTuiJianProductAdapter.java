package com.bopinjia.customer.adapter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.util.SetPriceSize;
import com.bopinjia.customer.util.SetPriceSize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CartTuiJianProductAdapter extends BaseAdapter {

	private Context context;
	private List<ProductListModel> list;
	private ProductListHold hold;
	private LayoutInflater inflater;
	private int layoutid;

	public CartTuiJianProductAdapter(Context context, List<ProductListModel> list, int id) {

		this.context = context;
		this.list = list;
		this.layoutid = id;
		inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(layoutid, null);
			hold = new ProductListHold();
			hold.item_name = (TextView) convertView.findViewById(R.id.txt_product_name);
			hold.item_market_price = (TextView) convertView.findViewById(R.id.txt_market_price);
			hold.item_market_price.getPaint().setStrikeThruText(true);
			hold.item_sale_price = (TextView) convertView.findViewById(R.id.txt_sell_price);
			hold.item_picture = (ImageView) convertView.findViewById(R.id.iv_product_thumbnails);
			hold.item_noproduct = (ImageView) convertView.findViewById(R.id.iv_no_product);
			convertView.setTag(hold);
		} else {
			hold = (ProductListHold) convertView.getTag();
		}
		ProductListModel jm = list.get(position);

		hold.item_name.setText(jm.getName());

		DecimalFormat df = new DecimalFormat("#,##0.00");
		hold.item_market_price.setText("¥" + df.format(new BigDecimal(jm.getMarket_price()).doubleValue()));

		SetPriceSize.setPrice(hold.item_sale_price, "¥" + df.format(new BigDecimal(jm.getSale_price()).doubleValue()),
				35);

		String RealStock = list.get(position).getRealStock();
		if (RealStock != null) {
			if (RealStock.equals("0")) {
				hold.item_noproduct.setVisibility(View.VISIBLE);
			} else {
				hold.item_noproduct.setVisibility(View.GONE);
			}
		}

		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(hold.item_picture, jm.getThumbnails(), imageOptions);

		return convertView;
	}

}
