package com.threetrees.plugin.asm

import com.threetrees.plugin.Controller
import com.threetrees.plugin.util.TextUtil
import groovy.json.JsonOutput
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

public class AutoMethodVisitor extends AdviceAdapter {

    String className
    String methodName
    List<AutoAnnotationVisitor> annotations

    Map<String,HashMap<String,Object>> map
    int mMatchType
    //注解的接收者
    String annotationReceive
    //类匹配的接收者
    String classReceiver
    //注解的路径
    String annotationPath

    public AutoMethodVisitor(MethodVisitor mv, int access, String name, String desc, String clsName) {
        this(mv,access,name,desc,clsName,0)
    }

    public AutoMethodVisitor(MethodVisitor mv, int access, String name, String desc,String clsName,int matchType) {
        super(Opcodes.ASM5, mv, access, name, desc)
        className = TextUtil.changeClassNameDot(clsName)
        methodName = name
        annotations = new ArrayList<AutoAnnotationVisitor>()
        map = new HashMap<>()
        annotationReceive = "com/threetree/pluginutil/TtreeReceiver"
        classReceiver = "com/threetree/pluginutil/TtreeReceiver"
        annotationPath = "Lcom/threetree/pluginutil/annotation"
        mMatchType = matchType
        if(matchType != 0)
        {
            Logger.info("||-----------------匹配到方法${methodName}--------------------------")
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {

        super.visitMethodInsn(opcode, owner, name, desc)
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute)
    }

    @Override
    public void visitEnd() {
        super.visitEnd()
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc)
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment)
    }

    @Override
    public void visitIntInsn(int i, int i1) {
        super.visitIntInsn(i, i1)
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals)
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var)
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label)
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
        super.visitLookupSwitchInsn(label, ints, labels)
    }

    @Override
    public void visitMultiANewArrayInsn(String s, int i) {
        super.visitMultiANewArrayInsn(s, i)
    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {
        super.visitTableSwitchInsn(i, i1, label, labels)
    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {
        super.visitTryCatchBlock(label, label1, label2, s)
    }

    @Override
    public void visitTypeInsn(int opcode, String s) {
        super.visitTypeInsn(opcode, s)
    }

    @Override
    public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
        super.visitLocalVariable(s, s1, s2, label, label1, i)
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode)
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        AnnotationVisitor _annotationVisitor = super.visitAnnotation(s, b);
        String path = Controller.getAnnotationPath()
        if(s.contains(annotationPath) || (!TextUtil.isEmpty(path) && s.contains(path)))
        {
            Logger.info("||-----------------匹配注解${s}--------------------------")
            map.put(s,new HashMap<String,Object>())
            AutoAnnotationVisitor autoAnnotationVisitor = new AutoAnnotationVisitor(Opcodes.ASM5,_annotationVisitor,s) {
                HashMap<String,Object> hashMap = map.get(s)
                @Override
                void visit(String name, Object value) {
                    if(name!=null && value!=null)
                    {
                        hashMap.put(name,value)
                    }
                }

                @Override
                AnnotationVisitor visitArray(String arrayName) {
                    List list = new ArrayList()
                    hashMap.put(arrayName,list)
                    return new AnnotationVisitor(Opcodes.ASM5,_annotationVisitor) {
                        @Override
                        void visit(String name, Object value) {
                            List _list = hashMap.get(arrayName)
                            _list.add(value)
                            hashMap.put(arrayName,_list)
                        }
                    }
                }
            }
            annotations.add(autoAnnotationVisitor)
            return autoAnnotationVisitor
        }

        return _annotationVisitor
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        annotations.each {
            AutoAnnotationVisitor autoAnnotationVisitor ->
                onMethod(autoAnnotationVisitor,"onMethodEnterForAnnotation")
        }

        if(mMatchType != 0)
        {
            Logger.info("||-----------------onMethodEnterForClass: ${methodName}--------------------------")
            onMethodForClass("onMethodEnterForClass");
        }

    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
        annotations.each {
            AutoAnnotationVisitor autoAnnotationVisitor ->
                onMethod(autoAnnotationVisitor,"onMethodExitForAnnotation")
        }

        if(mMatchType != 0)
        {
            Logger.info("||-----------------onMethodExitForClass: ${methodName}--------------------------")
            onMethodForClass("onMethodExitForClass");
        }
    }

    public void onMethod(AutoAnnotationVisitor autoAnnotationVisitor,String action)
    {
        String anno = TextUtil.changeClassNameDot(autoAnnotationVisitor.mAnnotationName)
        anno = TextUtil.changeName(anno)

        mv.visitLdcInsn(anno)
        mv.visitLdcInsn(methodName)
        def jsonOutput = new JsonOutput()
        def result = jsonOutput.toJson(map.get(autoAnnotationVisitor.mAnnotationName))
        //格式化输出
        println(jsonOutput.prettyPrint(result))
        mv.visitLdcInsn(result)

        if(autoAnnotationVisitor.mAnnotationName.contains(annotationPath))
        {
            mv.visitMethodInsn(INVOKESTATIC, annotationReceive,
                    action, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false)
        }else if(autoAnnotationVisitor.mAnnotationName.contains(Controller.getAnnotationPath()))
        {
            mv.visitMethodInsn(INVOKESTATIC, Controller.getAnnotationReceiver(),
                    action, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false)
        }
    }

    public void onMethodForClass(String action)
    {
        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
        if (((methodAccess & Opcodes.ACC_SYNTHETIC) != 0) && ((methodAccess & Opcodes.ACC_PRIVATE) == 0)) {
            return
        }
        if ((methodAccess & Opcodes.ACC_NATIVE) != 0) {
            return
        }

        boolean isStatic = false
        //如果是静态方法，则没有this实例
        if ((methodAccess & ACC_STATIC) == 0) {
            loadThis()
            isStatic = false
        } else {
            push((String) null);
            isStatic = true
        }

        mv.visitLdcInsn(className)
        mv.visitLdcInsn(methodName)

        List<Type> paramsTypeClass = new ArrayList()
        Type[] argsType = Type.getArgumentTypes(methodDesc)
        for (Type type : argsType) {
            paramsTypeClass.add(type)
        }
        if (paramsTypeClass.size() == 0) {
            push((String) null)
        } else {
            ParamsHelper.createObjectArray(mv, paramsTypeClass, isStatic)
        }
        String receiver = Controller.getClassReceiver()
        if(!TextUtil.isEmpty(receiver))
        {
            mv.visitMethodInsn(INVOKESTATIC, receiver,
                    action, "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", false)
        }else
        {
            mv.visitMethodInsn(INVOKESTATIC, classReceiver,
                    action, "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", false)
        }

    }
}
