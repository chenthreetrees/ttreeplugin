package com.threetree.pluginutil;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.threetree.pluginutil.permission.PermissionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/8/23.
 */

public class TtreeReceiver {

    private static final String ACTION_ENTER = "enter";
    private static final String ACTION_EXIT = "exit";

    /**
     * 通过匹配规则，调用的入口(方法进入时)
     *
     * @param object     class的this指针
     * @param className
     * @param methodName
     * @param objects    方法的参数值
     * @return true-表示该方法将被拦截，methodExit不再被调用，通过onInterceptForClass方法来处理拦截事件
     */
    public static boolean onMethodEnterForClass(Object object, String className, String methodName, Object[] objects)
    {
        return handleTrack(ACTION_ENTER, object, className, methodName, objects);
    }

    /**
     * 通过匹配规则，拦截方法
     *
     * @param object     class的this指针
     * @param className
     * @param methodName
     * @param objects    方法的参数值
     * @param returnType 方法的返回类型,参考类型表
     * @return 根据方法的返回类型，返回对应类型
     */
    public static Object onInterceptForClass(Object object, String className, String methodName, Object[] objects, String returnType)
    {
        if (TtreePlugin.getOnInterceptListener() != null)
            return TtreePlugin.getOnInterceptListener().onIntercept(object,className,methodName,null,objects,null,returnType);
        //如果拦截了方法，不重写拦截事件返回结果，则根据方法的返回值类型返回默认值
        return getReturnType(returnType);
    }

    /**
     * 通过匹配规则，调用的入口（方法退出时）
     *
     * @param object     class的this指针
     * @param className
     * @param methodName
     * @param objects    方法的参数值
     * @return 返回类型暂时无实际意义，返回false即可
     */
    public static void onMethodExitForClass(Object object, String className, String methodName, Object[] objects)
    {
        handleTrack(ACTION_EXIT, object, className, methodName, objects);
    }

    /**
     * 通过注解，调用的入口（方法进入时）
     *
     * @param annotationName
     * @param methodName
     * @param jsonValue      注解的值，json的结构是Hashmap<String,Object>,对应注解的key和value
     * @return true-表示该方法将被拦截，methodExit不再被调用，通过onInterceptForAnnotation方法来处理拦截事件
     */
    public static boolean onMethodEnterForAnnotation(Object object, String className,
                                                     String methodName, String annotationName,
                                                     Object[] objects, String jsonValue)
    {

        if ("com.threetree.pluginutil.annotation.TimeCost".equals(annotationName)) {
            TimeCost.setStartTime(methodName);
            return false;
        } else if ("com.threetree.pluginutil.annotation.Permission".equals(annotationName)) {
            handlePermission(jsonValue);
            return false;
        } else if ("com.threetree.pluginutil.annotation.Cut".equals(annotationName)) {
            handleCut(ACTION_ENTER, jsonValue);
            return false;
        } else if ("com.threetree.pluginutil.annotation.Debounce".equals(annotationName)) {
            return handleDebounce(methodName, jsonValue);
        }
        return false;
    }

    /**
     * 通过注解，拦截方法
     *
     * @param annotationName
     * @param methodName
     * @param jsonValue
     * @param returnType
     * @return 根据方法的返回类型，返回对应类型
     */
    public static Object onInterceptForAnnotation(Object object, String className,
                                                  String methodName, String annotationName,
                                                  Object[] objects, String jsonValue, String returnType)
    {
        if ("com.threetree.pluginutil.annotation.Debounce".equals(annotationName)) {//抖动拦截
            return getReturnType(returnType);
        }else
        {
            if (TtreePlugin.getOnInterceptListener() != null)
                return TtreePlugin.getOnInterceptListener().onIntercept(object,className,methodName,null,objects,null,returnType);
            //如果拦截了方法，不重写拦截事件返回结果，则根据方法的返回值类型返回默认值
            return getReturnType(returnType);
        }
    }

    /**
     * 通过注解，调用的入口（方法退出时）
     *
     * @param annotationName
     * @param methodName
     * @param jsonValue      注解的值，json的结构是Hashmap<String,Object>,对应注解的key和value
     */
    public static void onMethodExitForAnnotation(Object object, String className,
                                                 String methodName, String annotationName,
                                                 Object[] objects, String jsonValue)
    {

        if ("com.threetree.pluginutil.annotation.TimeCost".equals(annotationName)) {
            TimeCost.setEndTime(methodName);
            Log.e(methodName, TimeCost.getCostTime(methodName));
        } else if ("com.threetree.pluginutil.annotation.Cut".equals(annotationName)) {
            handleCut(ACTION_EXIT, jsonValue);
        }
    }

    private static HashMap<String, Object> jsonToMap(String jsonStr)
    {
        return new Gson().fromJson(jsonStr, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    private static boolean handleDebounce(String method, String jsonValue)
    {
        if (!TextUtils.isEmpty(jsonValue)) {
            HashMap<String, Long> map = new Gson().fromJson(jsonValue, new TypeToken<HashMap<String, Long>>() {
            }.getType());
            long interval = map.get("time");
            return !Debounce.isCanBounce(method, interval);
        }
        return true;
    }

    private static void handlePermission(String jsonValue)
    {
        if (!TextUtils.isEmpty(jsonValue)) {
            PermissionUtil permissionUtil = null;
            HashMap<String, Object> map = jsonToMap(jsonValue);
            Object obj = map.get("value");
            if (obj instanceof String) {
                String permission = (String) obj;
                permissionUtil = PermissionUtil.permission(permission);
            } else if (obj instanceof ArrayList) {
                ArrayList<String> values = (ArrayList<String>) obj;
                int size = values.size();
                String[] permissions = new String[size];
                for (int i = 0; i < size; i++) {
                    permissions[i] = values.get(i);
                }
                permissionUtil = PermissionUtil.permission(permissions);
            }
            if (permissionUtil != null) {
                permissionUtil.callback(new PermissionUtil.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted)
                    {
                        if (TtreePlugin.getOnPermissionGrantedListener() != null)
                            TtreePlugin.getOnPermissionGrantedListener().onGranted(permissionsGranted);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied)
                    {
                        if (TtreePlugin.getOnPermissionDeniedListener() != null)
                            TtreePlugin.getOnPermissionDeniedListener().onDenied(permissionsDenied);
                    }
                }).request();
            }
        }
    }

    private static void handleCut(String action, String jsonValue)
    {
        if (!TextUtils.isEmpty(jsonValue)) {
            HashMap<String, Object> map = jsonToMap(jsonValue);
            Object obj = map.get("type");
            if (obj instanceof Integer) {
                int type = (int) obj;
                if (action.equals(ACTION_ENTER)) {
                    if (TtreePlugin.getOnCutListener() != null)
                        TtreePlugin.getOnCutListener().onCutEnter(type);
                } else if (action.equals(ACTION_EXIT)) {
                    if (TtreePlugin.getOnCutListener() != null)
                        TtreePlugin.getOnCutListener().onCutExit(type);
                }
            }
        }
    }

    private static boolean handleTrack(String action, Object object, String className, String methodName, Object[] objects)
    {
        if (action.equals(ACTION_ENTER)) {
            if (TtreePlugin.getOnMethodListener() != null)
                return TtreePlugin.getOnMethodListener().onMethodEnter(object, className, methodName, objects);
        } else if (action.equals(ACTION_EXIT)) {
            if (TtreePlugin.getOnMethodListener() != null)
                TtreePlugin.getOnMethodListener().onMethodExit(object, className, methodName, objects);
        }
        return false;
    }

    private static Object getReturnType(String returnType)
    {
        if("Z".equals(returnType)){
            return false;
        }
        else if("B".equals(returnType)){
            return 0;
        }
        else if("C".equals(returnType)){
            return '\u0000';
        }
        else if("S".equals(returnType)){
            return 0;
        }
        else if("I".equals(returnType)){
            return 0;
        }
        else if("F".equals(returnType)){
            return 0.0f;
        }
        else if("D".equals(returnType)){
            return 0.0;
        }
        else if("J".equals(returnType)){
            return 0l;
        }
        return null;
    }
}
