package com.threetrees.plugin.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

public final class ParamsHelper
{
    /**
     * 创建局部参数代码
     * @param mv
     * @param paramsTypeClass
     * @param isStatic
     */
    public static void createObjectArray(MethodVisitor mv, List<Type> paramsTypeClass, boolean isStatic){
        //Opcodes.ICONST_0 ~ Opcodes.ICONST_5 这个指令范围
        int argsCount = paramsTypeClass.size()
        //声明 Object[argsCount];
        if(argsCount >= 6){
            mv.visitIntInsn(Opcodes.BIPUSH, argsCount)
        }else{
            mv.visitInsn(Opcodes.ICONST_0+argsCount)
        }
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")

        //如果是static方法，没有this隐含参数
        int loadIndex = (isStatic ? 0 : 1)

        //填充数组数据
        for(int i=0;i<argsCount;i++){
            mv.visitInsn(Opcodes.DUP)
            if(i <= 5){
                mv.visitInsn(Opcodes.ICONST_0+i)
            }else{
                mv.visitIntInsn(Opcodes.BIPUSH, i)
            }

            //这里又要做特殊处理，在实践过程中发现个问题：public void xxx(long a, boolean b, double c,int d)
            //当一个参数的前面一个参数是long或者是double类型的话，后面参数在使用LOAD指令，加载数据索引值要+1
            //个人猜想是和long，double是8个字节的问题有关系。这里做了处理
            //比如这里的参数：[a=LLOAD 1] [b=ILOAD 3] [c=DLOAD 4] [d=ILOAD 6];
            if(i >= 1){
                //这里需要判断当前参数的前面一个参数的类型是什么
                if("J".equals(paramsTypeClass.get(i-1).getDescriptor()) || "D".equals(paramsTypeClass.get(i-1).getDescriptor())){
                    //如果前面一个参数是long，double类型，load指令索引就要增加1
                    loadIndex ++
                }
            }
            if(!createPrimateTypeObj(mv, loadIndex, paramsTypeClass.get(i).getDescriptor())){
                mv.visitVarInsn(Opcodes.ALOAD, loadIndex)
                mv.visitInsn(Opcodes.AASTORE)
            }
            loadIndex ++
        }
    }

    /**
     * 创建基本类型对应的对象
     * @param mv
     * @param argsPostion
     * @param typeS
     * @return
     */
    private static boolean createPrimateTypeObj(MethodVisitor mv, int argsPostion, String typeS){
        if("Z".equals(typeS)){
            createBooleanObj(mv, argsPostion)
            return true
        }
        if("B".equals(typeS)){
            createByteObj(mv, argsPostion)
            return true
        }
        if("C".equals(typeS)){
            createCharObj(mv, argsPostion)
            return true
        }
        if("S".equals(typeS)){
            createShortObj(mv, argsPostion)
            return true
        }
        if("I".equals(typeS)){
            createIntegerObj(mv, argsPostion)
            return true
        }
        if("F".equals(typeS)){
            createFloatObj(mv, argsPostion)
            return true
        }
        if("D".equals(typeS)){
            createDoubleObj(mv, argsPostion)
            return true
        }
        if("J".equals(typeS)){
            createLongObj(mv, argsPostion)
            return true
        }
        return false
    }

    private static void createBooleanObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Boolean")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.ILOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createByteObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Byte")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.ILOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Byte", "<init>", "(B)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createShortObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Short")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.ILOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Short", "<init>", "(S)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createCharObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Character")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.ILOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Character", "<init>", "(C)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createIntegerObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Integer")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.ILOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createFloatObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Float")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.FLOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Float", "<init>", "(F)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createDoubleObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Double")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.DLOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Double", "<init>", "(D)V")
        mv.visitInsn(Opcodes.AASTORE)
    }

    private static void createLongObj(MethodVisitor mv, int argsPostion){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Long")
        mv.visitInsn(Opcodes.DUP)
        mv.visitVarInsn(Opcodes.LLOAD, argsPostion)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V")
        mv.visitInsn(Opcodes.AASTORE)
    }
}