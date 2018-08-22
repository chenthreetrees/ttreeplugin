package com.threetrees.plugin.asm

import com.threetrees.plugin.Controller
import com.threetrees.plugin.util.TextUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
/**
 * 类的遍历，遍历其中方法，满足两个条件才能修改方法字节码：
 *      1、类要匹配，类匹配就会遍历其中的每个方法
 */
public class AutoClassVisitor extends ClassVisitor {
    /**
     * 是否满足条件，满足条件的类才会修改中指定的方法
     */
    private boolean isMeetClassCondition = false

    private String mClassName
    private String[] mInterfaces

    AutoClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM4, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isMeetClassCondition = isMatchingSettingClass(name,interfaces)
        mClassName = name
        mInterfaces = interfaces
        // 打印调试信息
        if (isMeetClassCondition) {
            Logger.info('||\n||------------------------------开始遍历类 Start--------------------------------------')
            Logger.logForEach('||* visitStart *', Logger.accCode2String(access), name, signature, superName, interfaces)
        }

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        // 内部类
        if (isMeetClassCondition) {
            Logger.logForEach('||* visitInnerClass *', name, outerName, innerName, Logger.accCode2String(access))
        }
        super.visitInnerClass(name, outerName, innerName, access)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor adapter = null

        if (isMeetClassCondition) {
            //指定方法名，根据满足的类条件和方法名
            Logger.info("||-----------------开始修改方法${name}--------------------------")
            Logger.logForEach('||* visitMethod *', Logger.accCode2String(access), name, desc, signature, exceptions)
            try {
                // 注解的话，使用指定方法
                if (Controller.isUseAnotation())
                {
                    adapter = getSettingMethodVisitor(methodVisitor, access, name, desc)
                }else if (isMatchingSettingMethod(name, desc)){//匹配类和接口
                    adapter = getSettingMethodVisitor(methodVisitor, access, name, desc)
                }
            } catch (Exception e) {
                e.printStackTrace()
                adapter = null
            }
        }
        if (adapter != null) {
            return adapter
        }
        return methodVisitor
    }

    @Override
    void visitEnd() {
        if (isMeetClassCondition) {
            Logger.logForEach('||* visitEnd *')
            Logger.info('||------------------------------结束遍历类 end--------------------------------------')
        }
        super.visitEnd()
    }

    /**
     * 在app的module中设置的类与接口匹配
     *
     * @param className 类名
     * @param interfaces 类的实现接口
     */
    boolean isMatchingSettingClass(String className, String[] interfaces) {
        boolean isMatch = false
        List<AutoClassFilter> classFilters = Controller.getClassFilters()
        classFilters.each {
            AutoClassFilter filter ->
                String appContainName = filter.getContainName()
                String appInterfaceName = filter.getInterfaceName()
                String appClassName = filter.getClassName()
                //是否包含关键名称
                if(!TextUtil.isEmpty(appContainName) && className.contains(appContainName))
                {
                    isMatch = true
                }
                // 是否满足实现的接口
                else if(isMatchingInterfaces(interfaces, appInterfaceName))
                {
                    isMatch = true
                }
                // 类名是否满足
                else if (className == appClassName) {
                    isMatch = true
                }
                // 是否使用注解
                else if (Controller.isUseAnotation()) {
                    isMatch = true
                }
        }
        return isMatch
    }

    /**
     * 在app的module中设置的方法匹配
     *
     * @param name 方法名
     * @param desc 参数的方法的描述符
     */
    boolean isMatchingSettingMethod(String name, String desc) {
        boolean isMatch = false
        List<AutoClassFilter> classFilters = Controller.getClassFilters()
        classFilters.each {
            AutoClassFilter filter ->
                String appMethodName = filter.getMethodName()
                String appMethodDes = filter.getMethodDes()
                if (name == appMethodName && desc == appMethodDes) {
                    isMatch = true
                } else if (Controller.isUseAnotation()) {
                    //使用注解的方式，直接就方法匹配，因为注解的方法hook是自己在app module中
                    //控制的
                    isMatch = true
        }
        }
        return isMatch
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

    /**
     * app的module里头设置的自动埋点方法修改器
     *
     * @param className 类名
     * @param methodVisitor 需要修改的方法
     * @param name 方法名
     * @param desc 参数描述符
     * @param hasFilter 是否有过滤条件
     */
    MethodVisitor getSettingMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
        MethodVisitor adapter = null
        Closure vivi = Controller.getAppMethodVistor()
        if (vivi != null) {
            try {
                adapter = vivi(methodVisitor, access, name, desc)
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        return adapter
    }


}