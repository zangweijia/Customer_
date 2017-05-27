package com.bopinjia.customer.fragment.goodsInStockFragment;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.LazyFragment;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.NoScrollGridView;
import com.bopinjia.customer.view.NoScrollListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ShopAllProducts extends LazyFragment implements View.OnClickListener {
    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
    private RecyclerView mReclycleView;
    private NoScrollListview mListView;
    NoScrollGridView mGridView;

    private TextView tv_layout;
    private TextView tv_comprehensive;
    private TextView tv_sales;
    private TextView tv_price;
    private LinearLayout ll_grid;
    NestedScrollView scroller;
    /**
     * 检索
     */
    private int PageIndex = 1;
    /**
     * 一共多少页
     */
    private String mAllPages;
    View view;
    private List<ProductGridviewClassSubBean> mList;
    AdapterProductGridViewClassSub mAdapter, mListAdapter;
    private String OrderById;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shop_all_product, null);
        initView(view);
        isPrepared = true;
        lazyLoad();
        return view;
    }


    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        } else {
            init();
        }
    }

    private void initView(View view) {
        mListView = (NoScrollListview) view.findViewById(R.id.list);
        mGridView = (NoScrollGridView) view.findViewById(R.id.grid);
        ll_grid = (LinearLayout) view.findViewById(R.id.ll_grid);
        scroller = (NestedScrollView) view.findViewById(R.id.scrollview);

        tv_comprehensive = (TextView) view.findViewById(R.id.tv_comprehensive);
        tv_sales = (TextView) view.findViewById(R.id.tv_sales);
        tv_price = (TextView) view.findViewById(R.id.tv_price);
        tv_layout = (TextView) view.findViewById(R.id.tv_layout);
        tv_comprehensive.setOnClickListener(this);
        tv_sales.setOnClickListener(this);
        tv_price.setOnClickListener(this);
        tv_layout.setOnClickListener(this);
    }

    private void init() {
        setcolor(1);
        getProductList(0, "20");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    if (PageIndex < Integer.parseInt(mAllPages)) {
                        PageIndex += 1;
                        getProductList(1, "12");
                    } else if (PageIndex >= Integer.parseInt(mAllPages)) {
                        ((BaseActivity) getActivity()).showToast("没有更多了~");
                    }
                }
            }
        });
        mListView.setOnItemClickListener(clickListener);
        mGridView.setOnItemClickListener(clickListener);

    }

    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent();
            intent.putExtra("IsFreeShipping", "1");
            intent.putExtra("ProductSKUId", mList.get(i).getId());
            intent.setClass(getActivity(), ActivityProductDetailsNew.class);
            startActivity(intent);
        }
    };

    private void getProductList(final int id, String pageSize) {

        //  0-全部 1-高销量排序 2-低销量排序 3-高价格排序 4-低价格排序
        String Mid = ((BaseActivity) getActivity()).getBindingShop();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("MDUserId", Mid);
        map.put("ZY", "0");
        map.put("PageIndex", String.valueOf(PageIndex));
        map.put("pageSize", pageSize);
        map.put("TypeID", "0");
        map.put("OrderById", OrderById);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);
        StringBuffer stringBuffer = new StringBuffer();
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            stringBuffer.append(key).append("=").append(map.get(key)).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        String Sign = MD5.Md5(stringBuffer.toString());

        String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListCodeBpw_XhList?MDUserId="
                + Mid + "&ZY=" + "0" + "&PageIndex=" + String.valueOf(PageIndex) + "&pageSize="
                + pageSize + "&TypeID=" + "0" + "&OrderById=" + OrderById + "&Sign=" + Sign + "&Ts=" + Ts;

        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        parseList(result, id);
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void parseList(String jsonarray, int id) {
        try {

            JSONObject jo = new JSONObject(jsonarray);
            JSONObject jsondata = jo.getJSONObject("Data");

            JSONArray dataArray = jsondata.getJSONArray("Records");
            JSONObject Paging = jsondata.getJSONObject("Paging");
            mAllPages = Paging.getString("Pages");
            if (dataArray != null && dataArray.length() > 0) {

                List<ProductGridviewClassSubBean> dataList = new ArrayList<ProductGridviewClassSubBean>();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    ProductGridviewClassSubBean m = new ProductGridviewClassSubBean();

                    m.setIsfexiao(data.getString("BCP_IsFX"));
                    m.setCommissionPrice(data.getString("CommissionPrice"));


                    m.setImg(data.getString("ProductThumbnail"));
                    m.setMarketprice(data.getString("MarketPrice"));
                    m.setIsshiping("1");
                    //起订量
                    m.setNumber(data.getString("CustomerInitiaQuantity"));
                    m.setName(data.getString("ProductSKUName"));
                    m.setPrice(data.getString("ScanPrice"));
                    m.setId(data.getString("ProductSKUId"));
                    m.setCountry(data.getString("CountryName"));
                    m.setCountryimg(data.getString("CountryImageUrl"));
                    m.setRealStock(data.getString("RealStock"));
                    dataList.add(m);
                }
                switch (id) {
                    case 0:
                        mList = dataList;
                        mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
                        mGridView.setAdapter(mAdapter);

                        mListAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub_list);
                        mListView.setAdapter(mListAdapter);
                        break;
                    case 1:
                        if (dataList != null && !dataList.isEmpty()) {
                            mList.addAll(dataList);
                            mAdapter.notifyDataSetChanged();
                            mListAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 2:
                        mList.clear();
                        mList = dataList;
                        mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
                        mGridView.setAdapter(mAdapter);

                        mListAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub_list);
                        mListView.setAdapter(mListAdapter);

                        break;
                    default:
                        break;
                }

            } else {
            }

        } catch (Exception e) {

        }

    }

    private boolean pricetype = true;
    private boolean isGrid = true;

    /**
     * 设置颜色
     *
     * @param i i= 1 综合 i= 2 销量 i= 3 价格
     */
    private void setcolor(int i) {
        switch (i) {
            case 1:
                tv_comprehensive.setTextColor(getResources().getColor(R.color.main_color));
                tv_sales.setTextColor(getResources().getColor(R.color.bg_666666));
                tv_price.setTextColor(getResources().getColor(R.color.bg_666666));
                break;

            case 2:
                tv_comprehensive.setTextColor(getResources().getColor(R.color.bg_666666));
                tv_sales.setTextColor(getResources().getColor(R.color.main_color));
                tv_price.setTextColor(getResources().getColor(R.color.bg_666666));
                break;

            case 3:
                tv_comprehensive.setTextColor(getResources().getColor(R.color.bg_666666));
                tv_sales.setTextColor(getResources().getColor(R.color.bg_666666));
                tv_price.setTextColor(getResources().getColor(R.color.main_color));
                break;

            default:
                break;
        }

    }

    /**
     * 设置排序旁边的按钮
     *
     * @param i i=0 默认 i= 1显示向上 i=2显示向下
     */
    private void setTextViewDrawable(int id, int i) {

        TextView tv = (TextView) view.findViewById(id);

        Drawable drawable = null;
        if (i == 1) {
            drawable = getResources().getDrawable(R.drawable.ic_sort_up);
        } else if (i == 2) {
            drawable = getResources().getDrawable(R.drawable.ic_sort_down);
        } else if (i == 0) {
            drawable = getResources().getDrawable(R.drawable.ic_sort_defult);
        }
        /// 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(null, null, drawable, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_layout:
                if (isGrid) {
                    ll_grid.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                    tv_layout.setBackgroundResource(R.drawable.ic_grid);
                    isGrid = false;
                } else {
                    mListView.setVisibility(View.VISIBLE);
                    ll_grid.setVisibility(View.GONE);
                    tv_layout.setBackgroundResource(R.drawable.ic_list);
                    isGrid = true;
                }
                break;

            case R.id.tv_comprehensive:

                OrderById = "0";
                getProductList(2, "12");
                setcolor(1);

                break;
            case R.id.tv_sales:
                OrderById = "1";
                getProductList(2, "20");
                setcolor(2);
                setTextViewDrawable(R.id.tv_price, 0);
                break;

            case R.id.tv_price:
                setcolor(3);
                if (pricetype) {
                    // 高销量排序

                    OrderById = "3";
                    getProductList(2, "12");
                    pricetype = false;
                    setTextViewDrawable(R.id.tv_price, 2);
                } else {
                    // 低销量排序
                    OrderById = "4";
                    getProductList(2, "12");
                    pricetype = true;
                    setTextViewDrawable(R.id.tv_price, 1);
                }
                break;

            default:
                break;
        }
    }
}
