package com.threetree.pluginutil;

import java.util.HashMap;

/**
 * Created by Administrator on 2018/9/18.
 */

public class Debounce {

    private static HashMap<String,Long> map = new HashMap<String,Long>();

    /**
     *
     * @param method 方法名
     * @param interval 抖动的间隔时间
     * @return
     */
    public static boolean isCanBounce(String method,long interval) {
        if(!map.containsKey(method))
            return true;
        long time = map.get(method);
        if (System.currentTimeMillis() - time < interval){
            return false;
        }
        map.put(method,System.currentTimeMillis());
        return true;
    }
}
