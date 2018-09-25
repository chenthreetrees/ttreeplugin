package com.threetrees.plugin.asm

import com.threetrees.plugin.Controller
import com.threetrees.plugin.util.TextUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
/**
 * 类的遍历，遍历其中方法，满足过滤条件或者匹配注解，则注入相应的代码
 */
public class AutoClassVisitor extends ClassVisitor {

    private String mClassName
    private String[] mInterfaces

    AutoClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mClassName = name
        mInterfaces = interfaces
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        // 内部类
        super.visitInnerClass(name, outerName, innerName, access)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor adapter = null
        try {
            adapter = getSettingMethodVisitor(methodVisitor, access, name, desc)
        }catch (Exception e)
        {
            e.printStackTrace()
            adapter = null
        }

        if (adapter != null) {
            return adapter
        }
        return methodVisitor
    }

    @Override
    void visitEnd() {
        super.visitEnd()
    }

    /**
     * 接口名是否匹配
     *
     * @param interfaces 类的实现接口
     * @param interfaceName 需要匹配的接口名
     */
    boolean isMatchingInterfaces(String[] interfaces, String interfaceName) {
        boolean isMatch = false
        // 是否满足实现的接口
        interfaces.each {
            String inteface ->
                if (inteface == interfaceName) {
                    isMatch = true
                }
        }
        return isMatch
    }

    //0-不匹配 1-类名匹配 2-接口名匹配 3-关键字匹配 4-外部配置匹配 5-拦截方法
    //这个关键的方法，匹配的规则
    int matchingType(String name, String desc)
    {
        List<AutoClassFilter> classFilters = Controller.getClassFilters()
        for (AutoClassFilter filter:classFilters)
        {
            boolean isMatchMethod = false
            String appContainName = filter.getContainName()
            String appInterfaceName = filter.getInterfaceName()
            String appClassName = filter.getClassName()
            String appMethodName = filter.getMethodName()
            String appMethodDes = filter.getMethodDes()
            //是否会被外部的配置重写
            boolean override = filter.getOverride()
            //是否拦截方法
            boolean intercept = filter.getIntercept()

            //方法匹配
            if(name == appMethodName && desc == appMethodDes)
            {
                isMatchMethod = true
            }

            // 类名是否满足
            if (isMatchMethod && mClassName == appClassName) {
                if(override)
                {
                    return 4
                }
                if(intercept)
                {
                    return 5;
                }
                Logger.info("||-----------------类名匹配${mClassName}--------------------------")
                return 1
            }

            // 是否满足实现的接口
            if(isMatchMethod && isMatchingInterfaces(mInterfaces, appInterfaceName))
            {
                if(override)
                {
                    return 4
                }
                if(intercept)
                {
                    return 5;
                }
                Logger.info("||-----------------接口名匹配${appInterfaceName}--------------------------")
                return 2
            }

            //是否包含关键名称
            if(isMatchMethod && !TextUtil.isEmpty(appContainName) && mClassName.contains(appContainName))
            {
                if(override)
                {
                    return 4
                }
                if(intercept)
                {
                    return 5;
                }
                Logger.info("||-----------------关键字匹配${appContainName}--------------------------")
                return 3
            }
        }
        return 0
    }

    /**
     * 方法修改器
     *
     * @param className 类名
     * @param methodVisitor 需要修改的方法
     * @param name 方法名
     * @param desc 参数描述符
     * @param hasFilter 是否有过滤条件
     */
    MethodVisitor getSettingMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
        AutoMethodVisitor adapter = null
        int result = matchingType(name,desc)
        Closure vivi = Controller.getMethodVistor()
        if (vivi != null && result==4) {
            Logger.info("||-----------------外部配置匹配${name}--------------------------")
            try {
                adapter = vivi(methodVisitor, access, name, desc, mClassName)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        if(adapter == null)
        {
            adapter = new AutoMethodVisitor(methodVisitor,access,name,desc,mClassName,result)
        }
        return adapter
    }


}