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
     * @param methodName
     * @param objects
     */
    public static void onMethodEnterForClass(String methodName,Object[] objects)
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
     * 通过匹配规则，调用的入口（方法退出时）
     * @param methodName
     * @param objects
     */
    public static void onMethodExitForClass(String methodName,Object[] objects)
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
}
