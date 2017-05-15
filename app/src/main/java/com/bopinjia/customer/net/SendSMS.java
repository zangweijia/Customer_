package com.bopinjia.customer.net;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CancelledException;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;

import android.content.Context;
import android.widget.Toast;

public class SendSMS {

	private MessageCallBack ms;
	private Context context;

	public SendSMS(Context context, MessageCallBack mh) {
		this.ms = mh;
		this.context = context;
	}

	/**
	 * 发送短信
	 * 
	 * @param message
	 */
	public void sendSMS(String message, String phone) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("Phone", phone);
		map.put("Message", message);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/SMS/Send");
		params.addBodyParameter("Phone", phone);
		params.addBodyParameter("Message", message);

		params.addBodyParameter("Sign", Sign);
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						ms.DisposalProblem();
					} else {
						Toast.makeText(context, "短信息发送失败", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	public interface MessageCallBack {
		void DisposalProblem();
	}

}
