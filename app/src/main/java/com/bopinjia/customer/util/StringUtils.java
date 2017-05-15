package com.bopinjia.customer.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.Toast;

/**
 * 字符串相关操作的工具类
 */
public class StringUtils {

    /**
     * 换行符
     */
    public static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * 是否是半角空格
     * 
     * @param c
     *            待校验字符
     */
    public static boolean isWhitespace(char c) {
        return c == ' ';
    }

    /**
     * 是否是0
     * 
     * @param str
     *            待校验字符
     */
    public static boolean isZero(String str) {
        try {
            BigDecimal bd = new BigDecimal(str);
            return bd.doubleValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 空格校验
     * 
     * @param c
     *            待校验字符
     */
    public static boolean isZenHankakuSpace(char c) {
        return (c == '　' || c == ' ');
    }

    /**
     * 校验密码是不是正确
     * 
     * @param pass
     * @return
     */
    public static boolean validPass(String pass) {
        Pattern pattern = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_]+$");
        Matcher matcher = pattern.matcher(pass);
        return !matcher.matches();
    }

    /**
     * 右补空格
     * 
     * @param str
     *            待处理字符串
     * @return 处理后字符串
     */
    public static String rtrim(String str) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        while ((0 < length) && isWhitespace(str.charAt(length - 1))) {
            length--;
        }

        return length < str.length() ? str.substring(0, length) : str;
    }

    /**
     * 左补空格
     * 
     * @param str
     *            待处理字符串
     * @return 处理后字符串
     */
    public static String ltrim(String str) {
        if (str == null) {
            return null;
        }

        int start = 0;
        int length = str.length();
        while ((start < length) && isWhitespace(str.charAt(start))) {
            start++;
        }

        return start > 0 ? str.substring(start, length) : str;
    }

    /**
     * 取扩展名
     * 
     * @param name
     *            文件名
     * @return 扩展名
     */
    public static String getExtension(String name) {
        if (name == null) {
            return null;
        }
        int index = name.lastIndexOf('.');
        return (index < 0) ? "" : name.substring(index);
    }

    /**
     * 首字母转换成大写
     * 
     * @param str
     *            待处理字符串
     * @return 处理后字符串
     */
    public static String capitalizeInitial(String str) {
        if (str == null || "".equals(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * 判断字符串是否为空。
     * 
     * @param str
     *            字符串
     * @return 判断结果
     */
    public static boolean isNull(String str) {
        return str == null || "".equals(str);
    }

    /**
     * 判断字符串是否为整数。
     * 
     * @param str
     *            字符串
     * @return 判断结果
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * BASE64加密
     * 
     * @param str
     *            待加密字符串
     * @return 加密后字符串
     */
    public static String base64Encoding(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    /**
     * BASE64解密
     * 
     * @param str
     *            待解密字符串
     * @return 解密后字符串
     */
    public static String base64Decoding(String str) {
        return String.valueOf(Base64.decode(str, Base64.DEFAULT));
    }

    /**
     * 按XML标签名查找省。
     * 
     * @param context
     *            上下文
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getProvinces(JSONObject model) throws Exception {
        List<String> provincesCode = new ArrayList<String>();
        List<String> provincesName = new ArrayList<String>();

        try {
            Iterator<String> iter = model.keys();

            while (iter.hasNext()) {
                String key = iter.next();
                if ("name".equals(key)) {
                    continue;
                }

                provincesCode.add(key);
            }

            StringSort sort = new StringSort();
            Collections.sort(provincesCode, sort);
            for (String code : provincesCode) {
                provincesName.add(model.getJSONObject(code).getString("name"));
            }

        } catch (Exception e) {
            throw e;
        }

        Map<String, List<String>> provinceAttrsMap = new HashMap<String, List<String>>();
        provinceAttrsMap.put("code", provincesCode);
        provinceAttrsMap.put("name", provincesName);

        return provinceAttrsMap;
    }

    /**
     * 按XML标签名查找市。
     * 
     * @param context
     *            上下文
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getCities(JSONObject model, String province) throws Exception {
        List<String> citiesCode = new ArrayList<String>();
        List<String> citiesName = new ArrayList<String>();

        try {

            JSONObject cityData = model.getJSONObject(province);
            Iterator<String> iter = cityData.keys();

            while (iter.hasNext()) {
                String key = iter.next();
                if ("name".equals(key) || "code".equals(key)) {
                    continue;
                }

                citiesCode.add(key);
            }

            StringSort sort = new StringSort();
            Collections.sort(citiesCode, sort);
            for (String code : citiesCode) {
                citiesName.add(cityData.getJSONObject(code).getString("name"));
            }

        } catch (Exception e) {
            throw e;
        }

        Map<String, List<String>> citiesAttrsMap = new HashMap<String, List<String>>();
        citiesAttrsMap.put("code", citiesCode);
        citiesAttrsMap.put("name", citiesName);

        return citiesAttrsMap;
    }

    /**
     * 按XML标签名查找区。
     * 
     * @param context
     *            上下文
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getCounties(JSONObject model, String province, String city)
                    throws Exception {
        List<String> countiesCode = new ArrayList<String>();
        List<String> countiesName = new ArrayList<String>();

        try {
            JSONObject countyData = model.getJSONObject(province).getJSONObject(city);
            Iterator<String> iter = countyData.keys();

            while (iter.hasNext()) {
                String key = iter.next();
                if ("name".equals(key) || "code".equals(key)) {
                    continue;
                }

                countiesCode.add(key);
            }

            StringSort sort = new StringSort();
            Collections.sort(countiesCode, sort);
            for (String code : countiesCode) {
                countiesName.add(countyData.getJSONObject(code).getString("name"));
            }

        } catch (Exception e) {
            throw e;
        }

        Map<String, List<String>> countiesAttrsMap = new HashMap<String, List<String>>();
        countiesAttrsMap.put("code", countiesCode);
        countiesAttrsMap.put("name", countiesName);

        return countiesAttrsMap;
    }
    
    
  
    /**
     * TOAST消息
     * 
     * @param context
     *            上下文
     * @param msgId
     *            消息ID
     */
    public static void showToast(Context context, int msgId) {
        Toast.makeText(context, context.getString(msgId), Toast.LENGTH_LONG).show();
    }

    /**
     * TOAST消息
     * 
     * @param context
     *            上下文
     * @param message
     *            消息
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 图片转字符串
     * 
     * @param bm
     *            图片
     * @return 字符串
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        return baos.toByteArray();
    }
}
