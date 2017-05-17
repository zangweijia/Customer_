package com.bopinjia.customer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.SetPriceSize;
import com.bopinjia.customer.util.ViewHolderUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdapterBottomProductList extends CommonAdapter {

    List<ProductGridviewClassSubBean> list;
    Context context;
    int layoutId;
    private AddCartOnclick AddCartOnclick;

    public AdapterBottomProductList(List<ProductGridviewClassSubBean> list, Context context, int layoutId,
                                    AddCartOnclick AddCartOnclick) {
        super(list, context, layoutId);
        this.list = list;
        this.context = context;
        this.layoutId = layoutId;
        this.AddCartOnclick = AddCartOnclick;
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

        // 起订量
        holder.setText(R.id.txt_buy_number, "起订量：" + list.get(position).getNumber());

        // 商品主图
        holder.setImageURl(R.id.iv_product_thumbnails, list.get(position).getImg());

//        txt_commission
        holder.setText(R.id.txt_commission, "金牌佣金：¥" + list.get(position).getCommissionPrice());

        String isFx = getBopinjiaSharedPreference(Constants.ISFXS);
        if (isFx != null) {
            if (isFx.equals("1")) {
                //isFx == 1   是分销商
                if (list.get(position).getIsfexiao() != null) {
                    if (list.get(position).getIsfexiao().equals("0")) {
                        //商品属性为0，展示默认视图
                        holder.getView(R.id.txt_buy_number).setVisibility(View.VISIBLE);
                        holder.getView(R.id.txt_commission).setVisibility(View.GONE);
                        ((TextView) holder.getView(R.id.txt_sell_price)).setTextColor(context.getResources().getColor(R.color.main_color));
                    } else {
                        //商品属性为1，展示分销佣金视图
                        holder.getView(R.id.txt_buy_number).setVisibility(View.GONE);
                        holder.getView(R.id.txt_commission).setVisibility(View.VISIBLE);
                        ((TextView) holder.getView(R.id.txt_sell_price)).setTextColor(context.getResources().getColor(R.color.bg_999999));
                    }
                }
            }
        }


        // 国家图片
        String ss = encode(list.get(position).getCountryimg());
        holder.setImageURl(R.id.iv_country, ss);

        final String RealStock = list.get(position).getRealStock();
        if (RealStock != null) {
            if (RealStock.equals("0")) {
                holder.getView(R.id.iv_no_product).setVisibility(View.VISIBLE);
            } else {
                holder.getView(R.id.iv_no_product).setVisibility(View.GONE);
            }
        }
        holder.getView(R.id.iv_add_cart).setTag(position);

        holder.getView(R.id.iv_add_cart).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (RealStock != null) {
                    if (RealStock.equals("0")) {
                        Toast.makeText(context, "该商品暂时无货 ", Toast.LENGTH_SHORT).show();
                    } else {
                        AddCartOnclick.addCart(v);
                    }
                }
            }
        });

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

    public interface AddCartOnclick {
        void addCart(View v);
    }

    public String getBopinjiaSharedPreference(String key) {
        SharedPreferences sp = context.getSharedPreferences(Constants.KEY_PREFERENCE, 0);
        if (sp != null && sp.contains(key)) {
            return sp.getString(key, "");
        }

        return null;
    }
}
