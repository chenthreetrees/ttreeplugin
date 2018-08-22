package com.threetrees.plugin

import com.threetrees.plugin.asm.AutoClassFilter
import com.threetrees.plugin.asm.PluginSetting
import org.gradle.api.Project

public class Controller {
    private static Project project
    private static List<AutoClassFilter> autoClassFilters = new ArrayList<AutoClassFilter>()
    private static Closure methodVisitor
    private static boolean isUseAnotation

    public static void setProject(Project project) {
        Controller.@project = project
    }

    public static Project getProject() {
        return project
    }

    static void setIsUseAnotation(boolean isAnotation) {
        isUseAnotation = isAnotation
    }

    static boolean isUseAnotation(){
        return isUseAnotation
    }

    static PluginSetting getSettings() {
        return project.ttree
    }

    static void addClassFilter(AutoClassFilter filter) {
        autoClassFilters.add(filter)
    }

    static List<AutoClassFilter> getClassFilters() {
        return autoClassFilters
    }
    /**
     * 需要满足的类名
     */
    static String getClassName(AutoClassFilter autoClassFilter) {
        if (autoClassFilter == null) {
            return ""
        }
        return autoClassFilter.getClassName()
    }
    /**
     * 需要满足的实现接口
     */
    static String getInterfaceName(AutoClassFilter autoClassFilter) {
        if (autoClassFilter == null) {
            return ""
        }
        return autoClassFilter.getInterfaceName()
    }
    /**
     * 需要满足的方法名
     */
    static String getMethodName(AutoClassFilter autoClassFilter) {
        if (autoClassFilter == null) {
            return ""
        }
        return autoClassFilter.getMethodName()
    }

    /**
     * 需要满足的方法描述符
     */
    static String getMethodDes(AutoClassFilter autoClassFilter) {
        if (autoClassFilter == null) {
            return ""
        }
        return autoClassFilter.getMethodDes()
    }

    static void setMethodVistor(Closure visitor) {
        methodVisitor = visitor
    }

    static Closure getAppMethodVistor() {
        return methodVisitor
    }
}