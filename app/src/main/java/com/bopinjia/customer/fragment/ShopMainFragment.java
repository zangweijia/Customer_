package com.bopinjia.customer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.util.MD5;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@ContentView(R.layout.fragment_shop_main)
public class ShopMainFragment extends LazyFragment {
    private GridView gridview;
    private List<ProductGridviewClassSubBean> mList;
    private AdapterProductGridViewClassSub mAdapter;
    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
    CirclePageIndicator _Tabs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_main, null);

        gridview = (GridView) view.findViewById(R.id.gridview);
        viewpager = (ViewPager)view.findViewById(R.id.viewpager);
          _Tabs =(CirclePageIndicator)view.findViewById(R.id._indicator);
        isPrepared = true;
        lazyLoad();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        } else {
            getProductList();
            getSubCategoryNext("003");
        }
    }
    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Fragment getItem(int position) {
            return  new ShopAllProducts();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
    class tabModel {
        private String name;
        private String id;

        public void setName(String name) {
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
    private ViewPager viewpager;
    private List< tabModel> list;
    private void getSubCategoryNext(String code) {
        String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/SubCategoryNext?Code=" + code;
        XutilsHttp.getInstance().get(url, null, new getSubCategoryNextCallBack(), getActivity());
    }

    class getSubCategoryNextCallBack implements XutilsHttp.XCallBack {

        @Override
        public void onResponse(String result) {

            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject Data = jo.getJSONObject("Data");
                    JSONArray dataArray = Data.getJSONArray("ProductCategory");
                    if (dataArray != null && dataArray.length() > 0) {
                        list = new ArrayList< tabModel>();
                     tabModel modeln = new  tabModel();
                        modeln.setName("全部");
                        modeln.setId("003");
                        list.add(modeln);
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject data = dataArray.getJSONObject(i);
                             tabModel model = new   tabModel();
                            model.setName(data.getString("CName"));
                            model.setId(data.getString("CCode"));
                            list.add(model);
                        }

                        MyAdapter   adapter = new   MyAdapter(getChildFragmentManager());
                        viewpager.setAdapter(adapter);
                        _Tabs.setViewPager(viewpager);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void getProductList() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", "2");
        map.put("WhereCode", "003");
        map.put("OrderBy", "0");
        map.put("ZY", "1");
        map.put("PageIndex", "1");
        map.put("pageSize", "20");
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
        String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListCodeBpw_New?UserId=" + "2" + "&WhereCode="
                + "003" + "&OrderBy=" + "0" + "&ZY=" + "1" + "&PageIndex=" + "1"
                + "&pageSize=" + "20" + "&Sign=" + Sign + "&Ts=" + Ts;

        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {

                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        parseList(result, 0);
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

    /**
     * 获取底部商品数据
     * @param jsonarray
     * @param id
     */
    private void parseList(String jsonarray, int id) {
        try {

            JSONObject jo = new JSONObject(jsonarray);
            JSONObject jsondata = jo.getJSONObject("Data");

            JSONArray dataArray = jsondata.getJSONArray("Records");
            JSONObject Paging = jsondata.getJSONObject("Paging");
            if (dataArray != null && dataArray.length() > 0) {
                List<ProductGridviewClassSubBean> dataList = new ArrayList<ProductGridviewClassSubBean>();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    ProductGridviewClassSubBean m = new ProductGridviewClassSubBean();
                    m.setImg(data.getString("ProductThumbnail"));
                    m.setMarketprice(data.getString("MarketPrice"));
                    m.setIsshiping("1");
                    m.setNumber(data.getString("CumulativeSales"));
                    m.setName(data.getString("ProductSKUName"));
                    m.setPrice(data.getString("ScanPrice"));
                    m.setId(data.getString("ProductSKUId"));
                    m.setCountry(data.getString("CountryName"));
                    m.setCountryimg(data.getString("CountryImageUrl"));
                    m.setRealStock(data.getString("RealStock"));
                    dataList.add(m);
                }

                mList = dataList;
                mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
                gridview.setAdapter(mAdapter);

            } else {
            }
        } catch (Exception e) {
        }

    }
}
