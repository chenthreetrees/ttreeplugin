package com.threetrees.plugin

import com.android.build.gradle.AppExtension
import com.threetrees.plugin.asm.AutoClassFilter
import com.threetrees.plugin.asm.AutoTransform
import com.threetrees.plugin.asm.Logger
import com.threetrees.plugin.asm.PluginSetting
import com.threetrees.plugin.util.TextUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

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
        paramsList.each {
            Map<String, Object> map ->
                String containName = map.get("ContainName")
                String className = map.get("ClassName")
                String interfaceName = map.get("InterfaceName")
                String methodName = map.get("MethodName")
                String methodDes = map.get("MethodDes")
                boolean override = false
                boolean intercept = false
                // 全类名
                if (!TextUtil.isEmpty(className)){
                    className = TextUtil.changeClassNameSeparator(className)
                }
                // 实现接口的全类名
                if (!TextUtil.isEmpty(interfaceName)){
                    interfaceName = TextUtil.changeClassNameSeparator(interfaceName)
                }

                if(map.containsKey("Override"))
                {
                    override = map.get("Override")
                }

                if(map.containsKey("Intercept"))
                {
                    intercept = map.get("Intercept")
                }

                AutoClassFilter classFilter = new AutoClassFilter()
                classFilter.setContainName(containName)
                classFilter.setClassName(className)
                classFilter.setInterfaceName(interfaceName)
                classFilter.setMethodName(methodName)
                classFilter.setMethodDes(methodDes)
                classFilter.setOverride(override)
                classFilter.setIntercept(intercept)
                Controller.addClassFilter(classFilter)

                Logger.info('应用传递过来的数据->' + '\n-containName:' + containName + '\n-className:' + className +
                        '\n-interfaceName:' + interfaceName + '\n-methodName:' +
                        methodName + '\n-methodDes:' + methodDes + "\n-override:" + override)
        }

        String annotationPath = matchData.get("AnnotationPath")
        if(!TextUtil.isEmpty(annotationPath))
        {
            annotationPath = TextUtil.changeClassNameSeparator(annotationPath)
            Controller.setAnnotationPath("L" + annotationPath)
        }

        String annotationReceiver = matchData.get("AnnotationReceiver")
        if(!TextUtil.isEmpty(annotationReceiver))
        {
            annotationReceiver = TextUtil.changeClassNameSeparator(annotationReceiver)
            Controller.setAnnotationReceiver(annotationReceiver)
        }

        String classReceiver = matchData.get("ClassReceiver")
        if(!TextUtil.isEmpty(classReceiver))
        {
            classReceiver = TextUtil.changeClassNameSeparator(classReceiver)
            Controller.setClassReceiver(classReceiver)
        }

        Closure methodVistor = matchData.get("MethodVisitor")
        Controller.setMethodVistor(methodVistor)
    }
}