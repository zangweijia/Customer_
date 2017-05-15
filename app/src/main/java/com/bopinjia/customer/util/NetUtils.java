package com.bopinjia.customer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.constants.Constants;

import android.graphics.SurfaceTexture.OnFrameAvailableListener;

public final class NetUtils {


	public static String getSign(Map<String, String> map) {
		// 将map.entrySet()转换成list
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(map.entrySet());
		// 通过比较器来实现排序
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				// 升序排序
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry<String, String> mapping : list) {
			//System.out.println(mapping.getKey() + ":" + mapping.getValue());
			stringBuffer.append(mapping.getKey()).append("=").append(mapping.getValue()).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);

		String Sign = MD5.Md5(stringBuffer.toString());
		return Sign;
	}
	
}
