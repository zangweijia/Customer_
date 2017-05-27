package com.bopinjia.customer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.adapter.CartTuiJianProductAdapter;
import com.bopinjia.customer.adapter.ImageViewPagerAdapter;
import com.bopinjia.customer.adapter.ProductListModel;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.SetPriceSize;
import com.bopinjia.customer.view.ItemWebView;
import com.bopinjia.customer.view.MyViewPager;
import com.bopinjia.customer.view.MyViewPager.OnSingleTouchListener;
import com.bopinjia.customer.view.NoScrollGridView;
import com.bopinjia.customer.view.NoScrollViewProduct;
import com.bopinjia.customer.view.SlideDetailsLayout;
import com.bopinjia.customer.view.SlideDetailsLayout.Status;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodsInfoFragment extends Fragment
        implements SlideDetailsLayout.OnSlideDetailsListener, OnSingleTouchListener {
    private View rootView;
    private CirclePageIndicator mIndicator;
    private MyViewPager mPager;
    private NoScrollViewProduct vp_content;
    private SlideDetailsLayout sv_switch;
    private PagerSlidingTabStrip psts_tabs;
    private TextView tv_title;
    private TextView tv_xhorzy;
    private ImageView iv_country;
    private TextView txt_product_name;
    private TextView txt_price;
    private TextView txt_market;
    private LinearLayout ll_commission;
    private TextView txt_commission;
    private TextView txt_gold_commission;
    private TextView txt_sales;
    private TextView txt_moq;
    private String skuid;
    private String isFreeShipping;
    private String userid;
    List<View> images;
    private List<ProductListModel> list;
    private NoScrollGridView mGridNew;

    public static GoodsInfoFragment newInstance(String skuid, String isFreeShipping) {
        GoodsInfoFragment newFragment = new GoodsInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("skuid", skuid);
        bundle.putString("isFreeShipping", isFreeShipping);
        newFragment.setArguments(bundle);
        return newFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            skuid = args.getString("skuid");
            isFreeShipping = args.getString("isFreeShipping");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_goods_info, null);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initListener();
        getPicList();
        ShowGDS();
        GetProductdetailLovely();

    }

    private void initView() {
        mGridNew = (NoScrollGridView) getActivity().findViewById(R.id.grid);

        mIndicator = (CirclePageIndicator) getActivity().findViewById(R.id._indicator);
        mPager = (MyViewPager) getActivity().findViewById(R.id._pager);
        vp_content = (NoScrollViewProduct) getActivity().findViewById(R.id.vp_content);
        sv_switch = (SlideDetailsLayout) getActivity().findViewById(R.id.sv_switch);
        tv_title = (TextView) getActivity().findViewById(R.id.tv_title);
        psts_tabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id.psts_tabs);

        View content = getActivity().findViewById(R.id.include_content);
        // 现货直邮标识
        tv_xhorzy = (TextView) content.findViewById(R.id.tv_xhorzy);
        // 国家图片
        iv_country = (ImageView) content.findViewById(R.id.iv_country);
        // 商品名称
        txt_product_name = (TextView) content.findViewById(R.id.txt_product_name);
        // 商品价格
        txt_price = (TextView) content.findViewById(R.id.txt_price);
        // 市场价格
        txt_market = (TextView) content.findViewById(R.id.txt_market);
        // 佣金控件
        ll_commission = (LinearLayout) content.findViewById(R.id.ll_commission);
        // 我的佣金
        txt_commission = (TextView) content.findViewById(R.id.txt_commission);
        // 金牌佣金
        txt_gold_commission = (TextView) content.findViewById(R.id.txt_gold_commission);
        // 销量
        txt_sales = (TextView) content.findViewById(R.id.txt_sales);
        // 起订量
        txt_moq = (TextView) content.findViewById(R.id.txt_moq);

        if (isFreeShipping.equals("0")) {
            tv_xhorzy.setText("现货");
            tv_xhorzy.setBackgroundResource(R.drawable.bg_xianhuo);
        } else {
            tv_xhorzy.setText("直邮");
            tv_xhorzy.setBackgroundResource(R.drawable.bg_zhiyou);
        }

        if (((BaseActivity) getActivity()).isLogged()) {
            userid = ((BaseActivity) getActivity()).getLoginUserId();
        } else {
            userid = "0";
        }
    }

    private void initListener() {
        sv_switch.setOnSlideDetailsListener(this);
    }

    @Override
    public void onStatucChanged(Status status) {

        if (status == SlideDetailsLayout.Status.OPEN) {
            // 当前为图文详情页
            vp_content.setNoScroll(true);
            tv_title.setVisibility(View.VISIBLE);
            psts_tabs.setVisibility(View.GONE);
        } else {
            // 当前为商品详情页
            vp_content.setNoScroll(false);
            tv_title.setVisibility(View.GONE);
            psts_tabs.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 得到商品SKU详情 增加是否显示分销商佣金
     */
    private void ShowGDS() {

        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("SkuId", skuid);
        map.put("UserId", userid);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/Product/ShowGDS?SkuId=" + skuid + "&UserId=" + userid + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new showGds(), getActivity());

    }

    class showGds implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject data = jo.getJSONObject("Data");
                    JSONObject sku = data.getJSONObject("sku");

                    txt_product_name.setText("              " + sku.getString("SkuName"));
                    // txt_price.setText("¥" + sku.getString("ScanPrice"));

                    SetPriceSize.setPrice(txt_price, "¥" + sku.getString("ScanPrice"), 35);

                    txt_market.setText("¥" + sku.getString("MarketPrice"));
                    txt_market.getPaint().setStrikeThruText(true);
                    // 我的佣金
                    txt_commission.setText(sku.getString("Yjprice"));
                    // 金牌佣金
                    txt_gold_commission.setText(sku.getString("Commissionprice"));

                    ((BaseActivity) getActivity()).ImageFromUrl(encode(sku.getString("CountryImageUrl")),
                            R.id.iv_country);

                    txt_sales.setText(sku.getString("CumulativeSales") + "人已购买");
                    txt_moq.setText("起订量为：" + sku.getString("CustomerInitiaQuantity"));

                    if (sku.getString("GDS_Level").equals("3")) {
                        getActivity().findViewById(R.id.txt_gold_commission).setVisibility(View.GONE);
                    } else {
                        getActivity().findViewById(R.id.txt_gold_commission).setVisibility(View.VISIBLE);
                    }

                    if (sku.getString("BCP_IsFX").equals("0")) {
                        ll_commission.setVisibility(View.GONE);
                    } else {
                        if (((BaseActivity) getActivity()).isLogged()) {
                            ll_commission.setVisibility(View.VISIBLE);
                        } else {
                            ll_commission.setVisibility(View.GONE);
                        }
                    }

                    if (sku.getString("Yjprice").equals("0")) {
                        getActivity().findViewById(R.id.txt_commission).setVisibility(View.GONE);
                    } else {
                        getActivity().findViewById(R.id.txt_commission).setVisibility(View.VISIBLE);
                    }

                    if (sku.getString("Commissionprice").equals("0.00")) {
                        getActivity().findViewById(R.id.txt_gold_commission).setVisibility(View.GONE);
                    } else {
                        getActivity().findViewById(R.id.txt_gold_commission).setVisibility(View.VISIBLE);
                    }

                    ItemWebView mDetail = (ItemWebView) getActivity().findViewById(R.id.webview);
                    mDetail.setVerticalScrollBarEnabled(false);
                    mDetail.setHorizontalScrollBarEnabled(false);

                    mDetail.getSettings().setSupportZoom(true);
                    mDetail.getSettings().setUseWideViewPort(true);
                    mDetail.getSettings().setBuiltInZoomControls(true);
                    mDetail.getSettings().setLoadWithOverviewMode(true);
                    mDetail.getSettings().setDisplayZoomControls(false);
                    mDetail.loadData(sku.getString("SkuDesc"), "text/html; charset=UTF-8", null);


                    getIsDistribution();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static String encode(String url) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
            while (matcher.find()) {
                String tmp = matcher.group();
                url = url.replaceAll(tmp, java.net.URLEncoder.encode(tmp, "gbk"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return url;
    }

    private void GetProductdetailLovely() {

        String Muserid = ((BaseActivity) getActivity()).getBindingShop();

        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", Muserid);
        map.put("SkuId", skuid);
        map.put("ZY", isFreeShipping);

        map.put("PageIndex", "1");
        map.put("Pagesize", "6");

        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/Product/GetProductdetailLovelySkuId?UserId=" + Muserid + "&SkuId="
                + skuid + "&ZY=" + isFreeShipping + "&PageIndex=1&Pagesize=6" + "&Sign=" + NetUtils.getSign(map)
                + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new GetProductdetailLovelyCallback(), getActivity());

    }

    class GetProductdetailLovelyCallback implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    ParseTuiJian(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private void ParseTuiJian(String result) {

        try {
            JSONObject jo = new JSONObject(result);

            JSONArray jDList = jo.getJSONObject("Data").getJSONArray("Records");

            if (jDList.length() > 0) {

                if (jDList != null) {
                    list = new ArrayList<ProductListModel>();
                    for (int i = 0; i < jDList.length(); i++) {
                        JSONObject data = jDList.getJSONObject(i);
                        ProductListModel m = new ProductListModel();
                        m.setMarket_price(data.getString("MarketPrice"));
                        m.setName(data.getString("ProductSKUName"));
                        m.setThumbnails(data.getString("ProductThumbnail"));
                        m.setSale_price(data.getString("ScanPrice"));
                        m.setIsShip(data.getString("IsDirectMail"));
                        m.setSkuid(data.getString("ProductSKUId"));
                        // m.setRealStock(data.getString("RealStock"));
                        list.add(m);
                    }

                    CartTuiJianProductAdapter mCartTJ = new CartTuiJianProductAdapter(getActivity(), list,
                            R.layout.wj_item_product_lovely);
                    mGridNew.setAdapter(mCartTJ);
                    mGridNew.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent ii = new Intent();
                            ii.putExtra("IsFreeShipping", list.get(position).getIsShip());
                            ii.putExtra("ProductSKUId", list.get(position).getSkuid());
                            ii.setClass(getActivity(), ActivityProductDetailsNew.class);
                            startActivity(ii);

                        }
                    });
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断是否为经销商
     */
    private void getIsDistribution() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", userid);
        map.put("MDUserId", ((BaseActivity) getActivity()).getBindingShop());

        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GDSExists?UserId=" + userid + "&MDUserId="
                + ((BaseActivity) getActivity()).getBindingShop() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new IsDistributionCallBack(), getActivity());

    }

    /**
     * 判断是否为经销商回调
     *
     * @author ZWJ
     */
    class IsDistributionCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    String Data = jo.getString("Data");

                    if (Data.equals("0")) {
                        // 一般客户
                        ll_commission.setVisibility(View.GONE);
                    } else if (Data.equals("1")) {
                        // 分销商
                        ll_commission.setVisibility(View.VISIBLE);
                    } else if (Data.equals("2")) {
                        // 一般用户 可申请分销商
                        ll_commission.setVisibility(View.GONE);
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void getPicList() {

        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("SkuId", skuid);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/Product/ShowGDS_SkuPicture?SkuId="
                + skuid + "&Sign=" + NetUtils.getSign(map)
                + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new getPicListCallBack(), getActivity());
    }

    class getPicListCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONArray piclist = jo.getJSONArray("Data");
                    images = new ArrayList<View>();
                    for (int i = 0; i < piclist.length(); i++) {
                        JSONObject j = piclist.getJSONObject(i);
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        ((BaseActivity) getActivity()).setImageURl(imageView, j.getString("PictureUrl"));
                        images.add(imageView);
                    }
                    mPager.setAdapter(new ImageViewPagerAdapter(images));
                    mIndicator.setViewPager(mPager);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onSingleTouch(View v) {
        vp_content.setNoScroll(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        list.clear();
        images.clear();
    }

}
