package com.bopinjia.customer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;

/**
 * 页面管理工具类
 * 
 * @author yushen 2015/12/08
 */
public class ScreenManager {

    private static Stack<Activity> activityStack;
    private static List<Activity> activityList;
    private static ScreenManager instance;

    private static Activity lastActivity;

    private ScreenManager() {

    }

    public static ScreenManager getScreenManager() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    // 退出栈顶Activity
    public void popActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
            activityStack.remove(activity);
            activity = null;
        }
    }

    // 获得当前栈顶Activity
    public Activity currentActivity() {
        if (!activityStack.isEmpty()) {
            Activity activity = activityStack.lastElement();
            return activity;
        }
        return null;
    }

    // 将当前Activity推入栈中
    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    // 将当前Activity加入临时列表
    public void addTempActivity(Activity activity) {
        if (activityList == null) {
            activityList = new ArrayList<Activity>();
        }
        activityList.add(activity);
    }

    // 清空Activity临时列表
    public void clearTempList() {
        if (activityList != null && !activityList.isEmpty()) {
            for (Activity activity : activityList) {
                popActivity(activity);
            }

            activityList = null;
        }
    }

    // 清空Activity临时列表
    public void popTemp(Activity activity) {
        if (activityList != null && !activityList.isEmpty()) {
            popActivity(activity);
            activityList = null;
        }
    }

    public void setLastActivity(Activity activity) {
        lastActivity = activity;
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

    // 退出栈中所有Activity
    public void popAllActivityExceptOne(Class<Activity> cls) {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(cls)) {
                break;
            }
            popActivity(activity);
        }
    }

    // 退出栈中所有Activity
    public void popAllActivity() {
        while (true) {
            Activity activity = currentActivity();
            if (activity != null) {
                popActivity(activity);
            }
        }
       
    }

}
