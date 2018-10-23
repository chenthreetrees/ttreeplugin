package com.threetree.pluginutil;

import android.app.Application;
import android.content.Context;

import com.threetree.pluginutil.permission.PermissionUtil;

/**
 * Created by Administrator on 2018/8/23.
 */

public final  class TtreePlugin {

    private static Context mContext;

    /**
     * 权限申请被拒绝的监听
     */
    private static PermissionUtil.OnPermissionDeniedListener mOnPermissionDeniedListener;

    /**
     * 权限申请成功的监听
     */
    private static PermissionUtil.OnPermissionGrantedListener mOnPermissionGrantedListener;

    private static IOnCutListener mOnCutListener;
    private static IOnMethodListener mOnMethodListener;
    private static IOnInterceptListener mOnInterceptListener;

    /**
     * 切面注入代码的监听
     */
    public interface IOnCutListener
    {
        void onCutEnter(int type);

        void onCutExit(int type);
    }

    public interface IOnInterceptListener
    {
        Object onIntercept(Object object, String className,
                         String methodName, String annotationName,
                         Object[] objects, String jsonValue, String returnType);
    }

    /**
     *方法进入退出监听，可做数据埋点
     */
    public interface IOnMethodListener
    {
        boolean onMethodEnter(Object object, String className, String methodName, Object[] objects);

        void onMethodExit(Object object, String className, String methodName, Object[] objects);
    }

    /**
     * 初始化
     *
     * @param application
     */
    public static void init(Application application) {
        mContext = application.getApplicationContext();
    }

    /**
     * 获取全局上下文
     *
     * @return
     */
    public static Context getContext() {
        if(mContext == null)
            throw new RuntimeException("请先在全局Application中调用 TtreePlugin.init() 初始化！");
        return mContext;
    }

    /**
     * 设置权限申请被拒绝的监听
     *
     * @param listener 权限申请被拒绝的监听器
     */
    public static void setOnPermissionDeniedListener(PermissionUtil.OnPermissionDeniedListener listener) {
        TtreePlugin.mOnPermissionDeniedListener = listener;
    }

    public static PermissionUtil.OnPermissionDeniedListener getOnPermissionDeniedListener() {
        return mOnPermissionDeniedListener;
    }

    /**
     * 设置权限申请成功的监听
     *
     * @param listener 权限申请成功的监听器
     */
    public static void setOnPermissionGrantedListener(PermissionUtil.OnPermissionGrantedListener listener) {
        TtreePlugin.mOnPermissionGrantedListener = listener;
    }

    public static PermissionUtil.OnPermissionGrantedListener getOnPermissionGrantedListener() {
        return mOnPermissionGrantedListener;
    }

    public static void setOnCutListener(IOnCutListener listener)
    {
        TtreePlugin.mOnCutListener = listener;
    }

    public static IOnCutListener getOnCutListener()
    {
        return mOnCutListener;
    }

    public static IOnMethodListener getOnMethodListener()
    {
        return mOnMethodListener;
    }

    public static void setOnMethodListener(IOnMethodListener listener)
    {
        TtreePlugin.mOnMethodListener = listener;
    }

    public static IOnInterceptListener getOnInterceptListener()
    {
        return mOnInterceptListener;
    }

    public static void setOnInterceptListener(IOnInterceptListener listener)
    {
        TtreePlugin.mOnInterceptListener = listener;
    }

    public static Object getReturnType(String returnType)
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
