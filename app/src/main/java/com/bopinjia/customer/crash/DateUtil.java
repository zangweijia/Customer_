package com.bopinjia.customer.crash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;

/**
 * <h3>���ڹ�����</h3>
 * <p>��Ҫʵ�������ڵĳ��ò���
 * 
 */
@SuppressLint("SimpleDateFormat")
public final class DateUtil {

    /** yyyy-MM-dd HH:mm:ss�ַ��� */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** yyyy-MM-dd�ַ��� */
    public static final String DEFAULT_FORMAT_DATE = "yyyy-MM-dd";

    /** HH:mm:ss�ַ��� */
    public static final String DEFAULT_FORMAT_TIME = "HH:mm:ss";

    /** yyyy-MM-dd HH:mm:ss��ʽ */
    public static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        }

    };

    /** yyyy-MM-dd��ʽ */
    public static final ThreadLocal<SimpleDateFormat> defaultDateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_FORMAT_DATE);
        }

    };

    /** HH:mm:ss��ʽ */
    public static final ThreadLocal<SimpleDateFormat> defaultTimeFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_FORMAT_TIME);
        }

    };

    private DateUtil() {
        throw new RuntimeException("�� 3��");
    }

    /**
     * ��longʱ��ת��yyyy-MM-dd HH:mm:ss�ַ���<br>
     * @param timeInMillis ʱ��longֵ
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeFromMillis(long timeInMillis) {
        return getDateTimeFormat(new Date(timeInMillis));
    }

    /**
     * ��longʱ��ת��yyyy-MM-dd�ַ���<br>
     * @param timeInMillis
     * @return yyyy-MM-dd
     */
    public static String getDateFromMillis(long timeInMillis) {
        return getDateFormat(new Date(timeInMillis));
    }

    /**
     * ��dateת��yyyy-MM-dd HH:mm:ss�ַ���
     * <br>
     * @param date Date����
     * @return  yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeFormat(Date date) {
        return dateSimpleFormat(date, defaultDateTimeFormat.get());
    }

    /**
     * �������յ�intת��yyyy-MM-dd���ַ���
     * @param year ��
     * @param month �� 1-12
     * @param day ��
     * ע���±�ʾCalendar���£���ʵ��С1
     * ��������δ���ж�
     */
    public static String getDateFormat(int year, int month, int day) {
        return getDateFormat(getDate(year, month, day));
    }

    /**
     * ��dateת��yyyy-MM-dd�ַ���<br>
     * @param date Date����
     * @return yyyy-MM-dd
     */
    public static String getDateFormat(Date date) {
        return dateSimpleFormat(date, defaultDateFormat.get());
    }

    /**
     * ���HH:mm:ss��ʱ��
     * @param date
     * @return
     */
    public static String getTimeFormat(Date date) {
        return dateSimpleFormat(date, defaultTimeFormat.get());
    }

    /**
     * ��ʽ��������ʾ��ʽ
     * @param sdate ԭʼ���ڸ�ʽ "yyyy-MM-dd"
     * @param format ��ʽ�������ڸ�ʽ
     * @return ��ʽ�����������ʾ
     */
    public static String dateFormat(String sdate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        java.sql.Date date = java.sql.Date.valueOf(sdate);
        return dateSimpleFormat(date, formatter);
    }

    /**
     * ��ʽ��������ʾ��ʽ
     * @param date Date����
     * @param format ��ʽ�������ڸ�ʽ
     * @return ��ʽ�����������ʾ
     */
    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return dateSimpleFormat(date, formatter);
    }

    /**
     * ��dateת���ַ���
     * @param date Date
     * @param format SimpleDateFormat
     * <br>
     * ע�� SimpleDateFormatΪ��ʱ������Ĭ�ϵ�yyyy-MM-dd HH:mm:ss��ʽ
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String dateSimpleFormat(Date date, SimpleDateFormat format) {
        if (format == null)
            format = defaultDateTimeFormat.get();
        return (date == null ? "" : format.format(date));
    }

    /**
     * ��"yyyy-MM-dd HH:mm:ss" ��ʽ���ַ���ת��Date
     * @param strDate ʱ���ַ���
     * @return Date
     */
    public static Date getDateByDateTimeFormat(String strDate) {
        return getDateByFormat(strDate, defaultDateTimeFormat.get());
    }

    /**
     * ��"yyyy-MM-dd" ��ʽ���ַ���ת��Date
     * @param strDate
     * @return Date
     */
    public static Date getDateByDateFormat(String strDate) {
        return getDateByFormat(strDate, defaultDateFormat.get());
    }

    /**
     * ��ָ����ʽ��ʱ���ַ���ת��Date����
     * @param strDate ʱ���ַ���
     * @param format ��ʽ���ַ���
     * @return Date
     */
    public static Date getDateByFormat(String strDate, String format) {
        return getDateByFormat(strDate, new SimpleDateFormat(format));
    }

    /**
     * ��String�ַ�������һ����ʽת��Date<br>
     * ע�� SimpleDateFormatΪ��ʱ������Ĭ�ϵ�yyyy-MM-dd HH:mm:ss��ʽ
     * @param strDate ʱ���ַ���
     * @param format SimpleDateFormat����
     * @exception ParseException ���ڸ�ʽת������
     */
    private static Date getDateByFormat(String strDate, SimpleDateFormat format) {
        if (format == null)
            format = defaultDateTimeFormat.get();
        try {
            return format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * �������յ�intת��date
     * @param year ��
     * @param month �� 1-12
     * @param day ��
     * ע���±�ʾCalendar���£���ʵ��С1
     */
    public static Date getDate(int year, int month, int day) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(year, month - 1, day);
        return mCalendar.getTime();
    }

    /**
     * �����������������
     * 
     * @param strat ��ʼ���ڣ���ʽyyyy-MM-dd
     * @param end ��ֹ���ڣ���ʽyyyy-MM-dd
     * @return ���������������
     */
    public static long getIntervalDays(String strat, String end) {
        return ((java.sql.Date.valueOf(end)).getTime() - (java.sql.Date
                .valueOf(strat)).getTime()) / (3600 * 24 * 1000);
    }

    /**
     * ��õ�ǰ���
     * @return year(int)
     */
    public static int getCurrentYear() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.YEAR);
    }

    /**
     * ��õ�ǰ�·�
     * @return month(int) 1-12
     */
    public static int getCurrentMonth() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.MONTH) + 1;
    }

    /**
     * ��õ��¼���
     * @return day(int)
     */
    public static int getDayOfMonth() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * ��ý��������(��ʽ��yyyy-MM-dd)
     * @return yyyy-MM-dd
     */
    public static String getToday() {
        Calendar mCalendar = Calendar.getInstance();
        return getDateFormat(mCalendar.getTime());
    }

    /**
     * ������������(��ʽ��yyyy-MM-dd)
     * @return yyyy-MM-dd
     */
    public static String getYesterday() {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.DATE, -1);
        return getDateFormat(mCalendar.getTime());
    }

    /**
     * ���ǰ�������(��ʽ��yyyy-MM-dd)
     * @return yyyy-MM-dd
     */
    public static String getBeforeYesterday() {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.DATE, -2);
        return getDateFormat(mCalendar.getTime());
    }

    /**
     * ��ü���֮ǰ���߼���֮�������
     * @param diff ��ֵ�����������ƣ�������ǰ��
     * @return
     */
    public static String getOtherDay(int diff) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.DATE, diff);
        return getDateFormat(mCalendar.getTime());
    }

    /**
     * ȡ�ø������ڼ���һ������������ڶ���.
     * 
     * @param date ���������ڶ���
     * @param amount ��Ҫ��ӵ��������������ǰ��������ʹ�ø����Ϳ���.
     * @return Date ����һ�������Ժ��Date����.
     */
    public static String getCalcDateFormat(String sDate, int amount) {
        Date date = getCalcDate(getDateByDateFormat(sDate), amount);
        return getDateFormat(date);
    }

    /**
     * ȡ�ø������ڼ���һ������������ڶ���.
     * 
     * @param date ���������ڶ���
     * @param amount ��Ҫ��ӵ��������������ǰ��������ʹ�ø����Ϳ���.
     * @return Date ����һ�������Ժ��Date����.
     */
    public static Date getCalcDate(Date date, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, amount);
        return cal.getTime();
    }
    
    /**
     * ���һ������ʮ����֮������ڶ���
     * @param date
     * @param hOffset ʱƫ��������Ϊ��
     * @param mOffset ��ƫ��������Ϊ��
     * @param sOffset ��ƫ��������Ϊ��
     * @return
     */
    public static Date getCalcTime(Date date, int hOffset, int mOffset, int sOffset) {
    	Calendar cal = Calendar.getInstance();
    	if (date != null)
    		cal.setTime(date);
    	cal.add(Calendar.HOUR_OF_DAY, hOffset);
    	cal.add(Calendar.MINUTE, mOffset);
        cal.add(Calendar.SECOND, sOffset);
        return cal.getTime();
    }

    /**
     * ����ָ����������Сʱ���룬����һ��java.Util.Date����
     * 
     * @param year ��
     * @param month �� 0-11
     * @param date ��
     * @param hourOfDay Сʱ 0-23
     * @param minute �� 0-59
     * @param second �� 0-59
     * @return һ��Date����
     */
    public static Date getDate(int year, int month, int date, int hourOfDay,
            int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date, hourOfDay, minute, second);
        return cal.getTime();
    }

    /**
     * �������������
     * @param sDate yyyy-MM-dd��ʽ
     * @return arr[0]:�꣬ arr[1]:�� 0-11 , arr[2]��
     */
    public static int[] getYearMonthAndDayFrom(String sDate) {
        return getYearMonthAndDayFromDate(getDateByDateFormat(sDate));
    }

    /**
     * �������������
     * @return arr[0]:�꣬ arr[1]:�� 0-11 , arr[2]��
     */
    public static int[] getYearMonthAndDayFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int[] arr = new int[3];
        arr[0] = calendar.get(Calendar.YEAR);
        arr[1] = calendar.get(Calendar.MONTH);
        arr[2] = calendar.get(Calendar.DAY_OF_MONTH);
        return arr;
    }

}
