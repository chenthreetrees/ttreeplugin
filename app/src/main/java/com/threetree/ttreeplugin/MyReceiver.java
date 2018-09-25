package com.threetree.ttreeplugin;

import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.threetree.ttreeplugin.annotation.TestTree;

import java.util.HashMap;

/**
 * Created by Administrator on 2018/9/3.
 */

public class MyReceiver {
    private static final String ACTION_ENTER = "enter";
    private static final String ACTION_EXIT = "exit";

    /**
     * 通过匹配规则，调用的入口(方法进入时)
     * @param object
     * @param className
     * @param methodName
     * @param objects
     */
    public static void onMethodEnterForClass(Object object, String className,String methodName,Object[] objects)
    {
        Log.e("onMethodEnterForClass",methodName);
        if("onClick".equals(methodName))
        {
            if(objects != null && objects[0] instanceof View)
            {
                View view = (View)objects[0];
                Log.e("onClickEnter","view=" + view.getId());
            }
        }
    }

    /**
     * 通过匹配规则，调用的入口(方法退出时)
     * @param object 该方法所属于的类对象,即this
     * @param className 类名
     * @param methodName 方法名
     * @param objects 方法的参数值
     */
    public static void onMethodExitForClass(Object object, String className,String methodName,Object[] objects)
    {
        Log.e("onMethodExitForClass",methodName);
        if("onClick".equals(methodName))
        {
            if(objects != null && objects[0] instanceof View)
            {
                View view = (View)objects[0];
                Log.e("onClickExit","view=" + view.getId());
            }
        }
    }

    /**
     * 通过注解，调用的入口（方法进入时）
     * @param annotationName
     * @param methodName
     * @param jsonValue 注解的值，json的结构是Hashmap<String,Object>,对应注解的key和value
     */
    public static void onMethodEnterForAnnotation(String annotationName, String methodName, String jsonValue)
    {
        if(TestTree.class.getName().equals(annotationName))
        {
            Log.e("onMethodEnterForAnno",methodName);
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
        if(TestTree.class.getName().equals(annotationName))
        {
            Log.e("onMethodExitForAnno",methodName);
        }
    }

    private static HashMap<String,Object> jsonToMap(String jsonStr)
    {
        return new Gson()
                .fromJson(jsonStr, new TypeToken<HashMap<String,Object>>(){}.getType());
    }


    public static boolean shouldDoClick()
    {
        return false;
    }
}
