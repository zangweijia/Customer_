package com.bopinjia.customer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


public class MD5 {
	public static String Md5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			return buf.toString();// 32位的加密

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取时间戳
	 * @return
	 */
	public static String getTimeStamp(){
    	Date date = new Date();
		String str =String.valueOf(date.getTime());
		String time= str.substring(0, 10);
		
		return time;
    }

}