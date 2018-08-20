package com.threetrees.plugin

import com.android.build.gradle.AppExtension
import com.threetrees.plugin.asm.AutoClassFilter
import com.threetrees.plugin.asm.AutoTransform
import com.threetrees.plugin.asm.PluginSetting
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.threetrees.plugin.asm.Logger
import com.threetrees.plugin.util.TextUtil

class TtreePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('ttree', PluginSetting)
        Controller.setProject(project)

        //注册transform
        registerTransform(project)

        project.afterEvaluate {
            Logger.setDebug(project.ttree.isDebug)
            //初始化数据
            initData()
        }
    }

    def static registerTransform(Project project) {
        //AppExtension就是gradle文件里面的闭包代码块“android”
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AutoTransform())
    }

    static void initData() {
        Map<String, Object> matchData = Controller.getSettings().matchData

        List<Map<String, Object>> paramsList = matchData.get("ClassFilter")
        AutoClassFilter classFilter = new AutoClassFilter()
        paramsList.each {
            Map<String, Object> map ->
                String className = map.get("ClassName")
                String interfaceName = map.get("InterfaceName")
                String methodName = map.get("MethodName")
                String methodDes = map.get("MethodDes")
                // 全类名
                if (!TextUtil.isEmpty(className)){
                    className = TextUtil.changeClassNameSeparator(className)
                }
                // 实现接口的全类名
                if (!TextUtil.isEmpty(interfaceName)){
                    interfaceName = TextUtil.changeClassNameSeparator(interfaceName)
                }

                classFilter.setClassName(className)
                classFilter.setInterfaceName(interfaceName)
                classFilter.setMethodName(methodName)
                classFilter.setMethodDes(methodDes)
                Controller.setClassFilter(classFilter)
                println '应用传递过来的数据->' + '\n-className:' + className +
                        '\n-interfaceName:' + interfaceName + '\n-methodName:' + methodName + '\n-methodDes:' + methodDes
        }
        //设置是否使用注解查找相关方法，是的话把指定过来条件去掉
        boolean isAnotation = matchData.get("isAnotation")
        println '应用传递过来的数据->' + '\n-isAnotation:' + isAnotation
        Controller.setIsUseAnotation(isAnotation)

        Closure methodVistor = matchData.get("MethodVisitor")
        Controller.setMethodVistor(methodVistor)
    }
}