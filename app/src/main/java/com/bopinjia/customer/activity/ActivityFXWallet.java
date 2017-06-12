package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterMyWalletList;
import com.bopinjia.customer.bean.MyWalletBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityFXWallet extends BaseActivity {

    private TextView mTiTleBack;
    private TextView mTiTleName;
    private RelativeLayout mTiTleMain;
    private ListView mWallet;
    private TextView mWalletList;


    /**
     * 余额
     */
    @ViewInject(R.id.tv_balance)
    private TextView mBalance;
    /**
     * 累计收益
     */
    @ViewInject(R.id.tv_total_amount)
    private TextView mTotalAmount;

    /**
     * 已提现
     */
    @ViewInject(R.id.tv_reflect)
    private TextView mTomyInmoney;
    /**
     * 交易中
     */
    @ViewInject(R.id.tv_in_the_deal)
    private TextView mInthedeal;
    /**
     * 可提现金额
     */
    private String mDGDSM_ToMyCashMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_my_wallet);
        x.view().inject(this);
        setTitle();
        init();
        getMyWalletList("0", "1", "5");

    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);

        View v = (View) mTiTle.findViewById(R.id.view);
        v.setVisibility(View.GONE);

        mWalletList = (TextView) mTiTle.findViewById(R.id.btn_edit);
        mWalletList.setBackgroundResource(R.drawable.ic_my_wallet_list);

        mTiTleMain = (RelativeLayout) mTiTle.findViewById(R.id.rl_main);
        mTiTleMain.setBackgroundResource(R.color.bg_ff4f04);

        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleName.setText("钱包");
        mTiTleName.setTextColor(getResources().getColor(R.color.bg_ffffff));

        mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleBack.setBackgroundResource(R.drawable.ic_back_white);

    }

    @Event(value = {R.id.btn_edit, R.id.btn_return, R.id.tv_tixian})
    private void getEvent(View v) {

        switch (v.getId()) {
            case R.id.btn_edit:
                forward(ActivityFXWalletList.class);
                break;

            case R.id.btn_return:
                finish();
                break;
            case R.id.tv_tixian:
                Intent ii = new Intent();
                ii.putExtra("reflectaccount", mDGDSM_ToMyCashMoney);
                forward(ActivityFXReflect.class, ii);
                break;
            default:
                break;
        }
    }

    private void init() {
        getDistributionInfo();
        mWallet = (ListView) findViewById(R.id.lv_wallet);
    }

    private void getDistributionInfo() {
        String userId = getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", userId);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + userId + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;
        getDistributionInfocallback c = new getDistributionInfocallback();
        XutilsHttp.getInstance().get(url, null, c, this);
    }

    class getDistributionInfocallback implements XCallBack {

        @Override
        public void onResponse(String result) {

            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject Data = jo.getJSONObject("Data");
                    // 累计收益
                    String CumulativeMoney = Data.getString("MDGDSM_CumulativeMoney");
                    mTotalAmount.setText(CumulativeMoney);
                    // 我的余额
                    String mBalances = Data.getString("MDGDSM_ToMyMoney");
                    mBalance.setText(mBalances);
                    // 已提现金额
                    String MDGDSM_ToMyInMoney = Data.getString("MDGDSM_ToMyInMoney");
                    mTomyInmoney.setText(MDGDSM_ToMyInMoney);
                    //正在交易中
                    String mInthedealtext = Data.getString("MDGDSM_CashInMoney");
                    mInthedeal.setText(mInthedealtext);

                    mDGDSM_ToMyCashMoney = Data.getString("MDGDSM_ToMyCashMoney");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取我的钱包列表
     */
    private void getMyWalletList(String time, String index, String size) {
        String s = getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", s);
        map.put("Time", time);
        map.put("PageIndex", index);
        map.put("PageSize", size);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/List_Log?UserId=" + s + "&Time=" + time
                + "&PageIndex=" + index + "&PageSize=" + size + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new getMyWalletListCallback(), this);

    }

    class getMyWalletListCallback implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");
                    if (dataArray != null && dataArray.length() > 0) {

                        mWallet.setVisibility(View.VISIBLE);
                        findViewById(R.id.linearlayout_no_info).setVisibility(View.GONE);

                        List<MyWalletBean> mlist = new ArrayList<MyWalletBean>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject data = dataArray.getJSONObject(i);
                            MyWalletBean mb = new MyWalletBean();
                            mb.setDatetime(data.getString("UserBill_Creatime"));
                            mb.setPrice(data.getString("UserBill_Amount"));
                            mb.setType(data.getString("UserBill_TypeName"));
                            mb.setTypeId(data.getString("UserBill_PState"));
                            mlist.add(mb);

                        }
                        AdapterMyWalletList mAdapter = new AdapterMyWalletList(mlist, ActivityFXWallet.this,
                                R.layout.wj_item_my_wallet);

                        mWallet.setAdapter(mAdapter);

                    }
                } else {
                    mWallet.setVisibility(View.GONE);
                    findViewById(R.id.linearlayout_no_info).setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
