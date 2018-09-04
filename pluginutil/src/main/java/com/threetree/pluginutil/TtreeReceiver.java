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
     * @param methodName
     * @param objects
     */
    public static void onMethodEnterForClass(Object object,String className,String methodName,Object[] objects)
    {
        handleTrack(ACTION_ENTER,object,className,methodName,objects);
    }

    /**
     * 通过匹配规则，调用的入口（方法退出时）
     * @param methodName
     * @param objects
     */
    public static void onMethodExitForClass(Object object,String className,String methodName,Object[] objects)
    {
        handleTrack(ACTION_EXIT,object,className,methodName,objects);
    }

    /**
     * 通过注解，调用的入口（方法进入时）
     * @param annotationName
     * @param methodName
     * @param jsonValue 注解的值，json的结构是Hashmap<String,Object>,对应注解的key和value
     */
    public static void onMethodEnterForAnnotation(String annotationName, String methodName, String jsonValue)
    {

        if("com.threetree.pluginutil.annotation.TimeCost".equals(annotationName))
        {
            TimeCost.setStartTime(methodName);
        }else if("com.threetree.pluginutil.annotation.Permission".equals(annotationName))
        {
            handlePermission(jsonValue);
        }else if("com.threetree.pluginutil.annotation.Cut".equals(annotationName))
        {
            handleCut(ACTION_ENTER,jsonValue);
        }
    }

    /**
     * 通过注解，调用的入口（方法退出时）
     * @param annotationName
     * @param methodName
     * @param jsonValue 注解的值，json的结构是Hashmap<String,Object>,对应注解的key和value
     */
    public static void onMethodExitForAnnotation(String annotationName,String methodName, String jsonValue)
    {

        if("com.threetree.pluginutil.annotation.TimeCost".equals(annotationName))
        {
            TimeCost.setEndTime(methodName);
            Log.e(methodName,TimeCost.getCostTime(methodName));
        }else if("com.threetree.pluginutil.annotation.Cut".equals(annotationName))
        {
            handleCut(ACTION_EXIT,jsonValue);
        }
    }

    private static HashMap<String,Object> jsonToMap(String jsonStr)
    {
        return new Gson()
                .fromJson(jsonStr, new TypeToken<HashMap<String,Object>>(){}.getType());
    }

    private static void handlePermission(String jsonValue)
    {
        if(!TextUtils.isEmpty(jsonValue))
        {
            PermissionUtil permissionUtil=null;
            HashMap<String,Object> map = jsonToMap(jsonValue);
            Object obj = map.get("value");
            if(obj instanceof String)
            {
                String permission = (String)obj;
                permissionUtil = PermissionUtil.permission(permission);
            }else if(obj instanceof ArrayList)
            {
                ArrayList<String> values = (ArrayList<String>)obj;
                int size = values.size();
                String[] permissions = new String[size];
                for (int i=0;i<size;i++)
                {
                    permissions[i] = values.get(i);
                }
                permissionUtil = PermissionUtil.permission(permissions);
            }
            if(permissionUtil!=null)
            {
                permissionUtil.callback(new PermissionUtil.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted)
                    {
                        if(TtreePlugin.getOnPermissionGrantedListener() !=null)
                            TtreePlugin.getOnPermissionGrantedListener().onGranted(permissionsGranted);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied)
                    {
                        if(TtreePlugin.getOnPermissionDeniedListener() !=null)
                            TtreePlugin.getOnPermissionDeniedListener().onDenied(permissionsDenied);
                    }
                }).request();
            }
        }
    }

    private static void handleCut(String action,String jsonValue)
    {
        if(!TextUtils.isEmpty(jsonValue)) {
            HashMap<String, Object> map = jsonToMap(jsonValue);
            Object obj = map.get("type");
            if(obj instanceof Integer)
            {
                int type = (int)obj;
                if(action.equals(ACTION_ENTER))
                {
                    if(TtreePlugin.getOnCutListener() != null)
                        TtreePlugin.getOnCutListener().onCutEnter(type);
                }else if(action.equals(ACTION_EXIT))
                {
                    if(TtreePlugin.getOnCutListener() != null)
                        TtreePlugin.getOnCutListener().onCutExit(type);
                }
            }
        }
    }

    private static void handleTrack(String action,Object object,String className,String methodName,Object[] objects)
    {
        if(action.equals(ACTION_ENTER))
        {
            if(TtreePlugin.getOnTrackListener() != null)
                TtreePlugin.getOnTrackListener().onTrackEnter(object,className,methodName,objects);
        }else if(action.equals(ACTION_EXIT))
        {
            if(TtreePlugin.getOnTrackListener() != null)
                TtreePlugin.getOnTrackListener().onTrackExit(object,className,methodName,objects);
        }
    }

}
