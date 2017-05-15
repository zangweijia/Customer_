package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityOrderList extends BaseActivity {

    private List<JSONObject> mOrderList;
    private OrderListAdapter mOrderListAdapter;
    private ListView mLstOrder;
    private LinearLayout lv;
    /**
     * 检索
     */
    private int PageIndex = 1;
    /**
     * 一共多少页
     */
    private String mAllPages;
    private String mCurrStatus;

    private Dialog mDialog;
    private View dialogView;
    public static ActivityOrderList instance = null;

    /**
     * 刷新控件
     */
    private XRefreshView outView;
    public static long lastRefreshTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_activity_order_list);
        instance = this;
        // 返回
        findViewById(R.id.btn_return).setOnClickListener(this);

        // 状态按钮
        findViewById(R.id.btn_all).setOnClickListener(this);
        findViewById(R.id.btn_unpaid).setOnClickListener(this);
        findViewById(R.id.btn_unshipping).setOnClickListener(this);
        findViewById(R.id.btn_unreceiving).setOnClickListener(this);
        findViewById(R.id.btn_complete).setOnClickListener(this);
        lv = (LinearLayout) findViewById(R.id.data_null);
        findViewById(R.id.tv_go_new_product).setOnClickListener(this);

        mLstOrder = (ListView) findViewById(R.id.lst_order);

        setMenuStatus(getIntent().getStringExtra("status"));

        outView = (XRefreshView) findViewById(R.id.custom_view);
        outView.setPullLoadEnable(true);
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
        outView.setXRefreshViewListener(new XRefreshViewListener() {

            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
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

                            GetOrderList(1);
                        } else if (PageIndex >= Integer.parseInt(mAllPages)) {

                            mLstOrder.postDelayed(new Runnable() {
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

    public void tab5() {
        setMenuStatus(Constants.ORDER_STATUS_COMPLETE);
        GetOrderList(3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetOrderList(0);
    }

    /**
     * 画面控件点击回调函数
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.tv_go_new_product:
                // 没有商品回首页
                ActivityHome.instance.finish();
                forward(ActivityHome.class);
                finish();
                break;
            case R.id.btn_return:
                // 返回
                finish();
                break;
            case R.id.btn_all:
                setMenuStatus(Constants.ORDER_STATUS_ALL);
                GetOrderList(3);
                break;
            case R.id.btn_unpaid:
                setMenuStatus(Constants.ORDER_STATUS_UNPAID);
                GetOrderList(3);
                break;
            case R.id.btn_unshipping:
                setMenuStatus(Constants.ORDER_STATUS_UNSHIPPING);
                GetOrderList(3);
                break;
            case R.id.btn_unreceiving:
                setMenuStatus(Constants.ORDER_STATUS_UNRECEIVING);
                GetOrderList(3);
                break;
            case R.id.btn_complete:
                setMenuStatus(Constants.ORDER_STATUS_COMPLETE);
                GetOrderList(3);
                break;
            default:
                break;
        }
    }

    /**
     * 设置TAB的状态
     *
     * @param status 状态
     */
    private void setMenuStatus(String status) {
        this.mCurrStatus = status;
        this.PageIndex = 1;
        ((TextView) findViewById(R.id.txt_all)).setTextColor(getResources()
                .getColor(Constants.ORDER_STATUS_ALL.equals(status) ? R.color.main_color : R.color.txt_black));
        findViewById(R.id.line_bottom_all)
                .setVisibility(Constants.ORDER_STATUS_ALL.equals(status) ? View.VISIBLE : View.INVISIBLE);

        ((TextView) findViewById(R.id.txt_unpaid)).setTextColor(getResources()
                .getColor(Constants.ORDER_STATUS_UNPAID.equals(status) ? R.color.main_color : R.color.txt_black));
        findViewById(R.id.line_bottom_unpaid)
                .setVisibility(Constants.ORDER_STATUS_UNPAID.equals(status) ? View.VISIBLE : View.INVISIBLE);

        ((TextView) findViewById(R.id.txt_unshipping)).setTextColor(getResources()
                .getColor(Constants.ORDER_STATUS_UNSHIPPING.equals(status) ? R.color.main_color : R.color.txt_black));
        findViewById(R.id.line_bottom_unshipping)
                .setVisibility(Constants.ORDER_STATUS_UNSHIPPING.equals(status) ? View.VISIBLE : View.INVISIBLE);

        ((TextView) findViewById(R.id.txt_unreceiving)).setTextColor(getResources()
                .getColor(Constants.ORDER_STATUS_UNRECEIVING.equals(status) ? R.color.main_color : R.color.txt_black));
        findViewById(R.id.line_bottom_unreceiving)
                .setVisibility(Constants.ORDER_STATUS_UNRECEIVING.equals(status) ? View.VISIBLE : View.INVISIBLE);

        ((TextView) findViewById(R.id.txt_complete)).setTextColor(getResources()
                .getColor(Constants.ORDER_STATUS_COMPLETE.equals(status) ? R.color.main_color : R.color.txt_black));
        findViewById(R.id.line_bottom_complete)
                .setVisibility(Constants.ORDER_STATUS_COMPLETE.equals(status) ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 订单列表适配器
     */
    class OrderListAdapter extends BaseAdapter {

        private final Context mContext;

        public OrderListAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mOrderList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OrderListItem viewItem = new OrderListItem(mContext);
            try {
                // 数据模型
                final JSONObject model = mOrderList.get(position).getJSONObject("order");
                // 订单号 、id、创建时间 、订单类型
                viewItem.setOrder(model.getString("OrderSn"), model.getString("OrderId"), model.getString("CreateTime"),
                        model.getString("Otype"));
                // 状态
                viewItem.setStatus(model.getString("OrderStatus"), model.getString("PayStatus"),
                        model.getString("RetractableCount"));
                // 件数
                viewItem.setCount(model.getString("ProductCount"));
                // 金额
                viewItem.setAmount(model.getString("OrderAmount"));
                // 运费
                viewItem.setShipfee(model.getString("ShipFee"));
                // 设置商品列表
                viewItem.setProductsList(mOrderList.get(position).getJSONArray("orderProduct"));

            } catch (Exception e) {
                // 系统异常
                showSysErr(e);
            }
            return viewItem;
        }
    }

    /**
     * 销售列表控件
     */
    class OrderListItem extends LinearLayout {

        private String mOrderSn;
        private String mOrderStatus;
        private String mPayStatus;
        private String mOrderId;
        private String ProductSKUName;
        private String amount;
        private String mCreateTime;
        private String Otype;

        public OrderListItem(Context context) {
            super(context);
            View.inflate(getContext(), R.layout.item_order, this);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("OrderId", mOrderId);
                    intent.putExtra("OrderSn", mOrderSn);
                    intent.putExtra("OrderStatus", mOrderStatus);
                    intent.putExtra("Otype", Otype);
                    intent.putExtra("CurrStatus", mCurrStatus);
                    forward(ActivityOrderDetail.class, intent);
                }
            });

            // 取消订单
            this.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog = new Dialog(ActivityOrderList.this, R.style.CustomDialogTheme);
                    dialogView = LayoutInflater.from(ActivityOrderList.this).inflate(R.layout.send_tel_dailog, null);
                    // 设置自定义的dialog布局
                    mDialog.setContentView(dialogView);
                    // false表示点击对话框以外的区域对话框不消失，true则相反
                    mDialog.setCanceledOnTouchOutside(false);
                    // -----------------------------------
                    mDialog.show();
                    // 获取自定义dialog布局控件
                    ((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否取消该订单?");
                    Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
                    Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
                    // 确定按钮点击事件
                    confirmBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            operateOrder(mOrderSn, 1, Otype);
                            mDialog.dismiss(); // 关闭dialog
                        }
                    });
                    // 取消按钮点击事件
                    cancelBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                }
            });

            // 退款
            this.findViewById(R.id.btn_tk).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    double dbAmount = Double.parseDouble(amount);
                    DecimalFormat df = new DecimalFormat("###,##0.00");

                    Intent i = new Intent();
                    i.putExtra("OrderSn", mOrderSn);
                    i.putExtra("orderAmount", df.format(dbAmount));

                    i.putExtra("otype", Otype);
                    forward(ActivityRefund.class, i);
                }
            });

            // 催单
            this.findViewById(R.id.btn_reminder).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    operateOrder(mOrderId, 2, Otype);
                    showToast("√ 已成功发送催单信息");
                }
            });

            // 售后
            this.findViewById(R.id.bt_shouhou).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("OrderId", mOrderId);
                    intent.putExtra("OrderSn", mOrderSn);
                    intent.putExtra("OrderStatus", mOrderStatus);
                    intent.putExtra("Otype", Otype);
                    intent.putExtra("CurrStatus", mCurrStatus);
                    forward(ActivityOrderDetail.class, intent);
                }
            });

            // 确认收货
            this.findViewById(R.id.txt_order_confirmreceipt).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // operateOrder(mOrderId, "OrderReceive");
                    orderReceive(mOrderId, Otype);
                }
            });
            // 查看物流
            this.findViewById(R.id.btn_order_status).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到物流详情
                    Intent intent = new Intent();
                    intent.putExtra("Otype", Otype);
                    intent.putExtra("OrderId", mOrderId);
                    forward(ActivityOrderStateList.class, intent);
                }
            });
            this.findViewById(R.id.complete_order_status).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到物流详情
                    Intent intent = new Intent();
                    intent.putExtra("Otype", Otype);
                    intent.putExtra("OrderId", mOrderId);
                    forward(ActivityOrderStateList.class, intent);
                }
            });
            this.findViewById(R.id.confirmreceipt_order_status).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到物流详情
                    Intent intent = new Intent();
                    intent.putExtra("Otype", Otype);
                    intent.putExtra("OrderId", mOrderId);
                    forward(ActivityOrderStateList.class, intent);
                }
            });
            // this.findViewById(R.id.bt_order_status).setOnClickListener(new
            // OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // // 跳转到物流详情
            // Intent intent = new Intent();
            // intent.putExtra("Otype", Otype);
            // intent.putExtra("OrderId", mOrderId);
            // forward(ActivityOrderStateList.class, intent);
            // }
            // });

            // 删除订单
            this.findViewById(R.id.btn_delete).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog = new Dialog(ActivityOrderList.this, R.style.CustomDialogTheme);
                    dialogView = LayoutInflater.from(ActivityOrderList.this).inflate(R.layout.send_tel_dailog, null);
                    // 设置自定义的dialog布局
                    mDialog.setContentView(dialogView);
                    // false表示点击对话框以外的区域对话框不消失，true则相反
                    mDialog.setCanceledOnTouchOutside(false);
                    // -----------------------------------
                    mDialog.show();
                    // 获取自定义dialog布局控件
                    ((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否删除该订单?");
                    Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
                    Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
                    // 确定按钮点击事件
                    confirmBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            operateOrder(mOrderSn, 3, Otype);
                            showToast("订单已删除！");
                            mDialog.dismiss(); // 关闭dialog
                        }
                    });
                    // 取消按钮点击事件
                    cancelBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });

                }
            });

            // 付款
            this.findViewById(R.id.btn_pay).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    double dbAmount = Double.parseDouble(amount);
                    DecimalFormat df = new DecimalFormat("###,##0.00");
                    // 跳转到支付页面
                    Intent intent = new Intent();
                    intent.putExtra("Mode", Otype);
                    intent.putExtra("OrderId", mOrderSn);
                    intent.putExtra("JsonSkuName", ProductSKUName);
                    intent.putExtra("payAmount", df.format(dbAmount));
                    intent.putExtra("CreateTime", mCreateTime);
                    forward(ActivityPay.class, intent);
                }
            });

        }

        // 设置状态
        public void setStatus(String orderStatus, String payStatus, String state) {
            this.mOrderStatus = orderStatus;
            this.mPayStatus = payStatus;
            int statusDispId = -1;
            // 支付状态是未付款
            if ("0".equals(payStatus)) {
                if ("2".equals(orderStatus)) {
                    // 已取消
                    statusDispId = R.string.txt_order_canceled;
                    findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
                    // findViewById(R.id.see_reason).setVisibility(View.VISIBLE);
                    findViewById(R.id.complete_order_status).setVisibility(View.GONE);
                } else if ("3".equals(orderStatus)) {
                    // 已关闭
                    statusDispId = R.string.txt_order_close;
                    findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
                    findViewById(R.id.complete_order_status).setVisibility(View.GONE);

                } else {
                    // 待付款
                    statusDispId = R.string.txt_order_unpaid;
                    findViewById(R.id.ll_unpaid).setVisibility(View.VISIBLE);
                }

            } else {
                // 支付状态是已付款
                if ("1".equals(payStatus)) {
                    // 订单状态是未确认
                    if ("0".equals(orderStatus)) {
                        // 待发货
                        statusDispId = R.string.txt_order_unshipping;
                        findViewById(R.id.ll_unship).setVisibility(View.VISIBLE);
                    } else if ("1".equals(orderStatus)) {
                        // 待发货
                        statusDispId = R.string.txt_order_unshipping;
                        findViewById(R.id.ll_unship).setVisibility(View.VISIBLE);
                    } else if ("4".equals(orderStatus)) {
                        // 订单状态是确认
                        // 待收货
                        statusDispId = R.string.txt_order_unreceiving;
                        findViewById(R.id.ll_confirmreceipt).setVisibility(View.VISIBLE);
                    } else if ("6".equals(orderStatus)) {
                        // 订单状态是已完成
                        // 已完成
                        statusDispId = R.string.txt_order_complete;
                        findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
                        findViewById(R.id.complete_order_status).setVisibility(View.GONE);

                        if (state.equals("0")) {
                            findViewById(R.id.bt_shouhou).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.bt_shouhou).setVisibility(View.VISIBLE);
                        }
                    } else if ("5".equals(orderStatus)) {
                        // 已退货
                        statusDispId = R.string.txt_order_close;
                        findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
                        findViewById(R.id.complete_order_status).setVisibility(View.GONE);
                    } else if ("3".equals(orderStatus)) {
                        // 已关闭
                        statusDispId = R.string.txt_order_close;
                        findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
                        findViewById(R.id.complete_order_status).setVisibility(View.GONE);
                    } else if ("2".equals(orderStatus)) {
                        // 已取消
                        statusDispId = R.string.txt_order_canceled;
                        findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);

                        // findViewById(R.id.see_reason).setVisibility(View.VISIBLE);
                        findViewById(R.id.complete_order_status).setVisibility(View.GONE);

                    } else if ("7".equals(orderStatus)) {
                        // 已关闭
                        statusDispId = R.string.txt_order_daishenhe;
                        findViewById(R.id.ll_unship).setVisibility(View.GONE);
                        findViewById(R.id.btn_order_status).setVisibility(View.GONE);

                    } else if ("999".equals(orderStatus)) {
                        // 待退款
                        statusDispId = R.string.txt_order_daituikuan;
                        findViewById(R.id.ll_unship).setVisibility(View.GONE);
                        findViewById(R.id.btn_order_status).setVisibility(View.GONE);
                    }

                }
            }

            if (statusDispId != -1) {
                ((TextView) findViewById(R.id.txt_status)).setText(statusDispId);
            }

        }

        // 设置订单号 ，时间,
        public void setOrder(String orderSn, String orderId, String time, String Otype) {
            this.mOrderSn = orderSn;
            this.mOrderId = orderId;
            this.mCreateTime = time;
            this.Otype = Otype;
            ((TextView) findViewById(R.id.txt_order_no)).setText(orderSn);
        }

        // 设置件数
        public void setCount(String count) {
            String formatCount = MessageFormat.format(getString(R.string.txt_order_count), count);
            ((TextView) findViewById(R.id.txt_count)).setText(formatCount);
        }

        // 设置金额
        public void setAmount(String amount) {
            this.amount = amount;
            double dbAmount = Double.parseDouble(amount);
            DecimalFormat df = new DecimalFormat("###,##0.00");
            ((TextView) findViewById(R.id.txt_amount)).setText("合计：¥" + df.format(dbAmount));
        }

        // 设置运费
        public void setShipfee(String shipfee) {

            String formatShipfee = MessageFormat.format(getString(R.string.txt_order_shipfee), shipfee);
            ((TextView) findViewById(R.id.txt_shipfee)).setText(formatShipfee);

        }

        // 设置商品列表
        public void setProductsList(JSONArray products) {
            try {
                LinearLayout lstProducts = (LinearLayout) findViewById(R.id.lst_product);

                for (int i = 0; i < products.length(); i++) {
                    final JSONObject product = products.getJSONObject(i);

                    LinearLayout productView = new LinearLayout(getContext());
                    View.inflate(getContext(), R.layout.item_order_product2, productView);
                    ProductSKUName = product.getString("ProductSKUName");
                    ((TextView) productView.findViewById(R.id.txt_product_name))
                            .setText(product.getString("ProductSKUName"));
                    ((TextView) productView.findViewById(R.id.txt_product_tax)).setText(MessageFormat
                            .format(getString(R.string.txt_cart_product_tax), product.getString("ProductTaxMoney")));
                    ((TextView) productView.findViewById(R.id.txt_price))
                            .setText("¥" + product.getString("ProductPrice"));
                    ((TextView) productView.findViewById(R.id.txt_count)).setText("x" + product.getString("BuyCount"));

                    setImageFromUrl(product.getString("ThumbnailsUrl"),
                            (ImageView) productView.findViewById(R.id.iv_product_thumbnails));

                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                    params.weight = 1;

                    productView.setLayoutParams(params);

                    productView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mLstOrder.getHeight() / 6));

                    lstProducts.addView(productView);

                    if (i < products.length() - 1) {
                        // 分割线
                        View view = new View(getContext());
                        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                (int) getResources().getDimension(R.dimen.divider_line)));
                        view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));

                        lstProducts.addView(view);
                    }
                }
            } catch (Exception e) {
                // 系统异常
                showSysErr(e);
            }
        }
    }

    /**
     * 订单处理
     *
     * @param orderId
     * @param i       1:取消，2：催单，3：删除
     */
    private void operateOrder(String orderId, int i, String mOtype) {
        String api = "";
        if (i == 1) {
            api = "api/Order/Cancel_New";
        } else if (i == 2) {
            api = "api/Order/Reminder";
        } else if (i == 3) {
            api = "api/Order/Delete_New";
        }

        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", getLoginUserId());
        map.put("OrderId", orderId);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);
        map.put("Otype", mOtype);
        StringBuffer stringBuffer = new StringBuffer();
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            stringBuffer.append(key).append("=").append(map.get(key)).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        String Sign = MD5.Md5(stringBuffer.toString());

        Map<String, String> maps = new HashMap<String, String>();

        maps.put("UserId", getLoginUserId());
        maps.put("OrderId", orderId);
        maps.put("Otype", mOtype);
        maps.put("Sign", Sign);
        maps.put("Ts", Ts);

        XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + api, maps, new operateOrderCallBack(), this);

    }

    class operateOrderCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    GetOrderList(3);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void orderReceive(String orderId, String Otype) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("OrderId", orderId);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Otype", Otype);
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

        Map<String, String> maps = new HashMap<String, String>();

        maps.put("OrderId", orderId);
        maps.put("Otype", Otype);
        maps.put("Sign", Sign);
        maps.put("Ts", Ts);

        XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/Order/Receive", maps, new orderReceiveCallBack(),
                this);

    }

    class orderReceiveCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    setMenuStatus(Constants.ORDER_STATUS_COMPLETE);
                    GetOrderList(3);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取订单列表
     */
    private void GetOrderList(final int id) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", getLoginUserId());
        map.put("StateId", mCurrStatus);
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

        String url = Constants.WEBAPI_ADDRESS + "api/Order/List_New?UserId=" + getLoginUserId() + "&StateId="
                + mCurrStatus + "&PageIndex=" + String.valueOf(PageIndex) + "&Sign=" + Sign + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new GetOrderListCallBack(), id, null, this);
    }

    class GetOrderListCallBack implements XCallBackID {

        @Override
        public void onResponse(String result, int id, String str) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    lv.setVisibility(View.GONE);
                    mLstOrder.setVisibility(View.VISIBLE);

                    parse(id, result);
                } else if (jsonresult.equals("2")) {
                    mLstOrder.setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void parse(int id, String result) {
        try {
            JSONObject jo = new JSONObject(result);
            JSONObject jdata = jo.getJSONObject("Data");
            JSONObject Paging = jdata.getJSONObject("Paging");
            JSONArray dataArray = jdata.getJSONArray("Records");
            mAllPages = Paging.getString("Pages");

            if (dataArray != null && dataArray.length() > 0) {
                List<JSONObject> dataList = new ArrayList<JSONObject>();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    dataList.add(data);
                }
                switch (id) {
                    case 0:
                        // 画面初期化时 的检索
                        mOrderList = dataList;
                        mOrderListAdapter = new OrderListAdapter(ActivityOrderList.this);
                        mLstOrder.setAdapter(mOrderListAdapter);

                        break;
                    case 1:
                        // 上拉加载更多时
                        if (dataList != null && !dataList.isEmpty()) {
                            mOrderList.addAll(dataList);
                            mOrderListAdapter.notifyDataSetChanged();
                        }
                        outView.stopLoadMore();
                        break;
                    case 3:
                        // 画面初期化时 的检索
                        mOrderList = dataList;
                        mOrderListAdapter = new OrderListAdapter(this);
                        mLstOrder.setAdapter(mOrderListAdapter);
                        mOrderListAdapter.notifyDataSetChanged();

                        break;
                    default:
                        break;
                }
            } else {
                mOrderList = new ArrayList<JSONObject>();
                mOrderListAdapter = new OrderListAdapter(this);
                mLstOrder.setAdapter(mOrderListAdapter);
            }

        } catch (Exception e) {
            showSysErr(e);
        }

    }

}
