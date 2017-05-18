package com.bopinjia.customer.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterMyWalletList;
import com.bopinjia.customer.bean.MyWalletBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityFXWalletList extends BaseActivity {

    private TextView mTiTleBack;
    private TextView mTiTleName;
    private ListView mWallet;
    private ImageView mIvDate;

    //	WheelMain wheelMain;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 刷新控件
     */
    private XRefreshView outView;

    public static long lastRefreshTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_my_wallet_list);
        setTitle();
        init();

    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);

        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleName.setText("账单明细");

        mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

    }

    private void init() {

        mIvDate = (ImageView) findViewById(R.id.iv_date);
        mIvDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(ActivityFXWalletList.this, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        // TODO Auto-generated method stub
                        int mYear = year;
                        int mMonth = month;
                        int mDay = day;
                        //  小于10加0
                        String mm = ((mMonth + 1) < 10 ? 0 + (mMonth + 1) : (mMonth + 1)) + "";
                        String mday = ((mDay < 10) ? 0 + mDay : mDay) + "";

                        String time = mYear + "-" + mm + "-" + mday;
                        getMyWalletList(time, PageIndex, 0);

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                //设置时间范围
                Calendar calendarA = Calendar.getInstance();
                calendarA.clear();
                calendarA.set(1970, 0, 1);
                datePickerDialog.getDatePicker().setMinDate(calendarA.getTimeInMillis());

                datePickerDialog.show();


            }
        });

        mWallet = (ListView) findViewById(R.id.lv_wallet_list);
        getMyWalletList("0", PageIndex, 0);

        outView = (XRefreshView) findViewById(R.id.custom_view);
        outView.setPullLoadEnable(true);
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
        outView.setXRefreshViewListener(new XRefreshViewListener() {

            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getMyWalletList("0", PageIndex, 0);
                        outView.stopRefresh();
                        lastRefreshTime = outView.getLastRefreshTime();
                    }
                }, 2000);

            }

            @Override
            public void onLoadMore(boolean isSilence) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        if (PageIndex < Integer.parseInt(mAllPages)) {
                            PageIndex += 1;

                            getMyWalletList("0", PageIndex, 1);
                        } else if (PageIndex >= Integer.parseInt(mAllPages)) {

                            mWallet.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("没有更多了~");
                                    outView.stopLoadMore();
                                }
                            }, 500);
                        }

                    }
                }, 1500);
            }

            @Override
            public void onRelease(float direction) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onHeaderMove(double offset, int offsetY) {

            }
        });
        outView.restoreLastRefreshTime(lastRefreshTime);

    }

    /**
     * 检索
     */
    private int PageIndex = 1;
    /**
     * 一共多少页
     */
    private String mAllPages = "1";

    /**
     * 获取我的钱包列表
     */
    private void getMyWalletList(String time, int index, int id) {
        String s = getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", s);
        map.put("Time", time);
        map.put("PageIndex", String.valueOf(index));
        map.put("PageSize", "20");
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/List_Log?UserId=" + s + "&Time=" + time
                + "&PageIndex=" + String.valueOf(index) + "&PageSize=" + "20" + "&Sign=" + NetUtils.getSign(map)
                + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new getMyWalletListCallback(), id, null, this);

    }

    private List<MyWalletBean> Datalist;
    private AdapterMyWalletList mAdapter;

    class getMyWalletListCallback implements XCallBackID {

        @Override
        public void onResponse(String result, int id, String str) {
            try {
                JSONObject jo = new JSONObject(result);

                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    JSONObject Paging = jo.getJSONObject("Data").getJSONObject("Paging");
                    mAllPages = Paging.getString("Pages");

                    JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");
                    if (dataArray != null && dataArray.length() > 0) {
                        List<MyWalletBean> mlist = new ArrayList<MyWalletBean>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject data = dataArray.getJSONObject(i);
                            MyWalletBean mb = new MyWalletBean();

                            mb.setAccount(
                                    data.getString("UserAccountTypeId") + "(" + data.getString("UserAccountNum") + ")");

                            mb.setDatayear(data.getString("UserBill_Creatime_Y"));

                            mb.setDatetime(data.getString("UserBill_Creatime_M"));

                            mb.setPrice(data.getString("UserBill_Amount"));

                            mb.setType(data.getString("UserBill_TypeName"));

                            mb.setTypeId(data.getString("UserBill_TypeState"));
                            mlist.add(mb);

                        }
                        if (id == 0) {
                            Datalist = mlist;
                            mAdapter = new AdapterMyWalletList(Datalist, ActivityFXWalletList.this,
                                    R.layout.wj_item_my_wallet);
                            mWallet.setAdapter(mAdapter);
                        } else if (id == 1) {
                            if (mlist != null && !mlist.isEmpty()) {
                                Datalist.addAll(mlist);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                } else if (jsonresult.equals("2")) {
                    showToast("没有数据信息");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
