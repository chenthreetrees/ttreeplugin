package com.threetree.pluginutil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/8/23.
 */

public class TimeCost {
    public static Map<String, Long> mStartTime = new HashMap<>();
    public static Map<String, Long> mEndTime = new HashMap<>();

    public static void setStartTime(String methodName)
    {
        mStartTime.put(methodName, System.currentTimeMillis());
    }

    public static void setEndTime(String methodName)
    {
        mEndTime.put(methodName, System.currentTimeMillis());
    }

    public static String getCostTime(String methodName)
    {
        long start = mStartTime.get(methodName);
        long end = mEndTime.get(methodName);
        return "method:" + methodName + " cost:" + Long.valueOf(end - start) + " ms";
    }
}
