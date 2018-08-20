package com.threetrees.plugin.asm

import com.threetrees.plugin.Controller
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
/**
 * 类的遍历，遍历其中方法，满足两个条件才能修改方法字节码：
 *      1、类要匹配，类匹配就会遍历其中的每个方法
 */
public class AutoClassVisitor extends ClassVisitor {
    /**
     * 是否查看修改后的方法
     */
    public boolean seeModifyMethod = false
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
            if (!seeModifyMethod) {
                Logger.logForEach('||* visitStart *', Logger.accCode2String(access), name, signature, superName, interfaces)
            }
        }

        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        // 内部类
        if (isMeetClassCondition) {
            if (!seeModifyMethod) {
                Logger.logForEach('||* visitInnerClass *', name, outerName, innerName, Logger.accCode2String(access))
            }
        }
        super.visitInnerClass(name, outerName, innerName, access)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        MethodVisitor adapter = null

        if (isMeetClassCondition && seeModifyMethod) {
            //查看插入字节码之后信息，注解查找就不运行了，每个方法都会遍历到，日志太多
            Logger.info("||---------------------查看修改后方法${name}-----------------------------")
            Logger.logForEach('||* visitMethod *', Logger.accCode2String(access), name, desc, signature, exceptions)
            adapter = new AutoMethodVisitor(methodVisitor, access, name, desc)
        }
        else if (isMeetClassCondition) {
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
            if (!seeModifyMethod) {
                Logger.logForEach('||* visitEnd *')
            }
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
        boolean isMeetClassCondition
        String appInterfaceName = Controller.getInterfaceName()
        String appClassName = Controller.getClassName()
        // 是否满足实现的接口
        isMeetClassCondition = isMatchingInterfaces(interfaces, appInterfaceName)
        // 类名是否满足
        if (className == appClassName) {
            isMeetClassCondition = true
        }
        // 是否使用注解
        if (Controller.isUseAnotation()) {
            isMeetClassCondition = true
        }
        return isMeetClassCondition
    }

    /**
     * 在app的module中设置的方法匹配
     *
     * @param name 方法名
     * @param desc 参数的方法的描述符
     */
    boolean isMatchingSettingMethod(String name, String desc) {
        String appMethodName = Controller.getMethodName()
        String appMethodDes = Controller.getMethodDes()
        if (name == appMethodName && desc == appMethodDes) {
            return true
        } else if (Controller.isUseAnotation()) {
            //使用注解的方式，直接就方法匹配，因为注解的方法hook是自己在app module中
            //控制的
            return true
        }
        return false
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