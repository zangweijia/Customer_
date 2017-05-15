package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.popupwindow.ShakeRedPacket;
import com.bopinjia.customer.popupwindow.ShakeRedPacketNoJiHui;
import com.bopinjia.customer.popupwindow.ShakeRedPacketNotHaveRed;
import com.bopinjia.customer.popupwindow.ShakeRedRule;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.LuckyPanView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityTurntable extends BaseActivity {

	private LuckyPanView mLuckyPanView;
	private ImageView mStartBtn;
	private TextView mClose;
	private TextView mNumber;
	private TextView mRed;
	private ImageView mRule;
	private int index = 3;
	// ���к������
	private ShakeRedPacket releasePopWindow;
	private String orderId;
	// û�л�����
	private ShakeRedPacketNoJiHui mNoRed;
	// û�г��к��
	private ShakeRedPacketNotHaveRed mNoHaveRed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_turntable);

		init();

	}

	private void init() {
		orderId = getIntent().getStringExtra("orderid");
		//orderId = "000000000";
		// �ر�
		mClose = (TextView) findViewById(R.id.btn_close);
		mClose.setOnClickListener(this);
		// �齱����
		mNumber = (TextView) findViewById(R.id.tv_red_number);
		mNumber.setText("" + index);
		// �ҵĺ��
		mRed = (TextView) findViewById(R.id.tv_red);
		mRed.setOnClickListener(this);
		// �������
		mRule = (ImageView) findViewById(R.id.iv_rule);
		mRule.setOnClickListener(this);

		// ת��
		mLuckyPanView = (LuckyPanView) findViewById(R.id.id_luckypan);
		// ��ʼ��ť
		mStartBtn = (ImageView) findViewById(R.id.id_start_btn);
		mStartBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.id_start_btn:
				// ��ʼ��ת
				// 1лл���� 2 ����ݵ�

				if (index != 0) {
					index -= 1;
					mNumber.setText("" + index);
					getRed();
				} else {
					showNoRed();
				}

				break;
			case R.id.iv_rule:
				// �������
				showRule();
				break;
			case R.id.tv_red:
				// ��ת������б�
				forward(ActivityRedPagketList.class);
				break;
			case R.id.btn_close:
				// �رյ�ǰҳ����ת�������б�
				// status = 2 �������б�
				Intent i = new Intent();
				i.putExtra("status", "2");
				forward(ActivityOrderList.class, i);
				finish();
				break;
			default:
				break;
		}
	}

	private void getRed() {
		// TODO Auto-generated method stub
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("OrderPaySn", orderId);
		map.put("MdUserId", getBindingShop());

		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);
		String Sign = MD5.Md5(stringBuffer.toString());

		String url = Constants.WEBAPI_ADDRESS + "api/RedPacketReceive/GetOrderCJ?OrderPaySn=" + orderId + "&UserId="
				+ getLoginUserId() + "&MdUserId=" + getBindingShop() + "&Sign=" + Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new getRedCallBack(), this);

	}

	class getRedCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				final JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject joData = jo.getJSONObject("Data");
					show(1, 1, joData.getString("RPR_Price"));
				} else {
					show(2, 2, "");
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * ����ת�� 2 лл���� 1 �н� type : 1 zhongjiang 2 δ�н�
	 *
	 * @param i
	 */
	private void show(int i, final int type, final String str) {
		mLuckyPanView.luckyStart(i);

		new Thread() {
			public void run() {
				// ����Ǻ�ʱ���������֮�����UI��
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// ����UI
						try {
							Thread.sleep(2000);
							mLuckyPanView.luckyEnd();
							Thread.sleep(3000);

							if (type == 1) {
								showPhotoPop(str);
							} else if (type == 2) {
								showNoHave();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}
		}.start();

	}

	/**
	 * �鵽���չʾpop����
	 *
	 * @param str
	 */
	public void showPhotoPop(String str) {
		releasePopWindow = new ShakeRedPacket(ActivityTurntable.this, itemsOnClick, str);
		releasePopWindow.showAtLocation(this.findViewById(R.id.lll), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	/**
	 * û�л����� չʾpop����
	 */
	public void showNoRed() {
		mNoRed = new ShakeRedPacketNoJiHui(ActivityTurntable.this, itemsOnClick);
		mNoRed.showAtLocation(this.findViewById(R.id.lll), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	/**
	 * û�г鵽���
	 */
	public void showNoHave() {
		mNoHaveRed = new ShakeRedPacketNotHaveRed(ActivityTurntable.this, itemsOnClick);
		mNoHaveRed.showAtLocation(this.findViewById(R.id.lll), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	public void showRule() {
		mRedRule = new ShakeRedRule(ActivityTurntable.this, itemsOnClick);
		mRedRule.showAtLocation(this.findViewById(R.id.lll), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	private View.OnClickListener itemsOnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.iv_close:// �ر�
					releasePopWindow.dismiss();
					break;
				case R.id.tvtry_again:
					releasePopWindow.dismiss();
					break;
				case R.id.tv_chakan:
					releasePopWindow.dismiss();
					forward(ActivityRedPagketList.class);
					break;

				// �齱����Ϊ0չʾ�Ľ���
				case R.id.iv_know:
					// ��֪���˰�ť
					mNoRed.dismiss();
					break;
				case R.id.iv_nored_close:// �ر�
					mNoRed.dismiss();
					break;
				// û�г鵽
				case R.id.tv_try_again:// ��ҡһ��
					mNoHaveRed.dismiss();
					break;
				case R.id.iv_not_havered_close:// �ر�
					mNoHaveRed.dismiss();
					break;
				case R.id.tv_konw:
					mRedRule.dismiss();
					break;

			}
		}
	};
	private ShakeRedRule mRedRule;

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.putExtra("status", "2");
			forward(ActivityOrderList.class, i);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
