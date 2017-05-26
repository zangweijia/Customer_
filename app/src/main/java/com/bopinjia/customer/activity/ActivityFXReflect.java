package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXReflect extends BaseActivity {

    private TextView mTiTleName, mTiTleReflect;
    /**
     * 可提现余额
     */
    @ViewInject(R.id.tv_reflect_account)
    private TextView mReflectAccount;
    /**
     * 余额
     */
    @ViewInject(R.id.tv_price)
    private TextView mTVprice;

    private String pricr;

    public static ActivityFXReflect instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_reflect);
        instance = this;
        x.view().inject(this);

        setTitle();
        init();
    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);
        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleReflect = (TextView) mTiTle.findViewById(R.id.btn_edit);

        mTiTleReflect.setText("提现记录");
        mTiTleName.setText("提现");
    }

    private void init() {
        pricr = getIntent().getStringExtra("reflectaccount");
        mReflectAccount.setText(pricr);
        mTVprice.setText(pricr);
    }

    @Event(value = {R.id.btn_return, R.id.btn_edit, R.id.chk_alipay,
            R.id.chk_weixinpay, R.id.tv_tixian,R.id.tv_tixian_interduce})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.btn_edit:
                // 提现记录
                forward(ActivityFXReflectList.class);
                break;
            case R.id.tv_tixian:
                // 申请提现
                float fprice = Float.parseFloat(pricr);
                int iii = (int) fprice;
                if (iii > 10) {
                    submitReflect(pricr);
                } else {
                    showToast("提现金额不能低于10元");
                }
                break;
            case R.id.tv_tixian_interduce:
                Intent i= new Intent();
                i.putExtra("newsId", "11");
                forward(ActivityCustomerNews.class, i);
                break;
            default:
                break;
        }
    }

    /**
     * 申请提现
     *
     * @param account
     */
    private void submitReflect(String account) {
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        String Ts = MD5.getTimeStamp();
        map.put("GDSUserId", getLoginUserId());
        map.put("BR_Amount", account);
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

        String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/Add_New?GDSUserId=" + getLoginUserId() + "&BR_Amount=" + account
                + "&Sign=" + Sign + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new submitReflectCallBack(), this);
    }

    /**
     * 申请提现 回调
     *
     * @author ZWJ
     */
    class submitReflectCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    // 成功
                    Intent intent = new Intent(ActivityFXReflect.this, ActivityReflectSuccess.class);
                    startActivity(intent);
                } else {
                    showToast(jo.getString("Message"));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
