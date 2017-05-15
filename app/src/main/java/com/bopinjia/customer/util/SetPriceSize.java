package com.bopinjia.customer.util;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

/**
 * 设置价格小数点后两位变小
 *
 */
public class SetPriceSize {

	public static void setPrice(TextView tv, String str, int size) {
		Spannable span = new SpannableString(str);
		int i = str.length();
		span.setSpan(new AbsoluteSizeSpan(size), i - 2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// span.setSpan(new ForegroundColorSpan(Color.RED), 11, 16,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// span.setSpan(new BackgroundColorSpan(Color.YELLOW), 11, 16,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(span);
	}

}
