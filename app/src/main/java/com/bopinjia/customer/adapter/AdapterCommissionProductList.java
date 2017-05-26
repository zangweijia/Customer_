package com.bopinjia.customer.adapter;

import android.content.Context;
import android.view.View;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdapterCommissionProductList extends CommonAdapter {

    List<CommissionProductBean> list;
    Context context;
    int layoutId;

    public AdapterCommissionProductList(List<CommissionProductBean> list, Context context, int layoutId) {
        super(list, context, layoutId);
        this.list = list;
        this.context = context;
        this.layoutId = layoutId;
    }

    @Override
    public void convert(ViewHolderUtils holder, Object t, int position) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        // 商品的国别
        holder.setText(R.id.tv_country, list.get(position).getCountry());

        // 商品名称
        holder.setText(R.id.tv_name, list.get(position).getName());

        // 商品价格
        holder.setText(R.id.tv_price, "¥" + list.get(position).getPrice());

        String str = list.get(position).getCommission();

        double dbSellPrice = new BigDecimal(str).doubleValue();
        // 佣金
        holder.setText(R.id.tv_commission,  df.format(dbSellPrice));


        if (list.get(position).getGold_comission().equals("0")) {
        } else {
            // 金牌佣金
            holder.setText(R.id.tv_gold_commission, df.format(new BigDecimal(list.get(position).getGold_comission()).doubleValue()));
        }
        //当分销商等级为3 时隐藏金牌佣金
        if (list.get(position).getLevel().equals("3")) {
            holder.getView(R.id.tv_gold_commission).setVisibility(View.INVISIBLE);
        } else if (list.get(position).getLevel().equals("10")) {

        } else {
            holder.getView(R.id.tv_gold_commission).setVisibility(View.VISIBLE);
        }

        // 购买人数
        holder.setText(R.id.tv_number, list.get(position).getNumber() + "人已购买");

        // 商品主图
        holder.setImageURl(R.id.iv_img, list.get(position).getImg());

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
