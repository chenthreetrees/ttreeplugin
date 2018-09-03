package com.threetrees.plugin.asm

public class Logger {
    private static boolean isDebug = true

    /**
     * 设置是否打印日志
     */
    static void setDebug(boolean isDebug) {
        this.isDebug = isDebug
    }

    static boolean isDebug() {
        return isDebug
    }

    /**
     * 打印日志
     */
    def static info(Object msg) {
        if (!isDebug) return
        try {
            println "${msg}"
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}