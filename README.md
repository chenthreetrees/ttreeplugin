# TtreePlugin

该插件主要提供一套配置规则，外部包或者app工程直接拓展使用场景和功能。
本着尽量不去修改插件代码的思想来编写，感谢[Neacy](https://github.com/Neacy)的帮助。

原理：该插件主要通过注解和匹配规则来找到对应的方法，在方法的enter和exit的时候注入相应的代码

## 配置规则

插件的默认配置如果不能满足需求，可以通过在app的gradle文件里面自定义配置
如果只需要简单的功能，可以先略过此步骤

在gradle文件最后添加ttree,如下：
```
ttree {
	//是否开启调试，编译时打印出log
    isDebug = true
    //具体配置
    matchData = [
            'AnnotationPath' : 'com.threetree.ttreeplugin.annotation',
            'AnnotationReceiver' : 'com.threetree.ttreeplugin.MyReceiver',
            'ClassReceiver' : 'com.threetree.ttreeplugin.MyReceiver',
            'ClassFilter'  : [
                    //根据类型匹配
                    ['ClassName' : 'com.threetree.ttreeplugin.MainActivity',
                     'MethodName': 'testClassName', 'MethodDes': '()V'],
                    //根据关键字匹配
                    ['ContainName' : 'Activity',
                     'MethodName': 'testContainName', 'MethodDes': '(Ljava/lang/String;)V'],
                    //根据接口名匹配
                    ['InterfaceName': 'android.view.View$OnClickListener',
                     'MethodName': 'onClick', 'MethodDes': '(Landroid/view/View;)V'],
                    //根据类型匹配
                    ['ClassName' : 'com.threetree.ttreeplugin.MainActivity',
                     'MethodName': 'testOverride', 'MethodDes': '()V', 'Override' : true]
            ],
            //插入的字节码，方法的执行顺序visitAnnotation->onMethodEnter->onMethodExit
            'MethodVisitor': {
                MethodVisitor methodVisitor, int access, String name, String desc, String className ->
                    AutoMethodVisitor adapter = new AutoMethodVisitor(methodVisitor, access, name, desc, className) {
                        @Override
                        protected void onMethodEnter() {
                            super.onMethodEnter();
                            if("testOverride".equals(name))
                            {
                                methodVisitor.visitLdcInsn(name)
                                methodVisitor.visitLdcInsn("========start=========")
                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false)
                            }
                        }

                        @Override
                        protected void onMethodExit(int opcode) {
                            super.onMethodExit(opcode)
                        }
                    }
                    return adapter
            }
    ]
}
```

**注意:** ttree，isDebug，matchData，AnnotationPath,AnnotationReceiver,ClassReceiver
ClassFilter，ClassName，MethodName，ContainName，InterfaceName，MethodDes，Override，MethodVisitor，Intercept
这些约定的拼写不能错误。

**AnnotationPath:** String类型，使用自定义注解时，注解所在的包路径

**AnnotationReceiver:** String类型，使用自定义注解时，处理注解的类，必须使用全路径。

处理的方法：
匹配注解，方法进入时调用`public static void onMethodExitForAnnotation(String annotationName,String methodName, String jsonValue)`

匹配注解，方法拦截时调用`public static Object onInterceptForAnnotation(String annotationName, String methodName, String jsonValue, String returnType)`

匹配注解，方法退出时调用`public static void onMethodExitForAnnotation(String annotationName,String methodName, String jsonValue)`

其中需要注意，方法名和参数类型，返回类型必须与上面的一致，jsonValue的结构是Hashmap<String,Object>,对应注解的key和value，当使用拦截规则时候，onMethodExitForAnnotation将不会再被调用。

**ClassReceiver:** String类型，使用匹配规则时的处理类，必须使用全路径，可以与AnnotationReceiver同名。

处理的方法：

根据类名等规则匹配，方法进入时调用`public static void onMethodEnterForClass(Object object,String className,String methodName,Object[] objects)`

根据类名等规则匹配，方法拦截时调用`public static Object onInterceptForClass(Object object,String className,String methodName,Object[] objects,String returnType)`

根据类名等规则匹配，方法退出时调用`public static void onMethodExitForClass(Object object,String className,String methodName,Object[] objects)`

其中需要注意，方法名和参数类型，返回类型必须与上面的一致，object 表示该方法的类对象指针，即this（注意内部类的this），静态方法该值为null，

className表示全路径类名（注意内部类的类名），methodName表示方法名，objects存放了methodName这个方法的参数值，

自定义ClassReceiver之后，拓展包里面的ClassReceiver将不再接收，当使用拦截规则时候，onMethodExitForAnnotation将不会再被调用。
					
**ClassName:** String类型，类名，全路径

**InterfaceName:** String类型，接口名，全路径

**ContainName:** String类型，关键字，包含该关键字的所有类

**MethodName:** String类型，方法名				
	
**MethodDes:** String类型，方法的描述符

**Override:** boolean类型，是否重载MethodVisitor（高级用法，需要对asm有一定的了解，不推荐使用）

**MethodVisitor:** 在Override为true的时候使用（参考demo）

**Intercept:**boolean类型，是否需要拦截方法（如果需要使用注解来拦截方法，注解需要定义一个boolean类型的key为onIntercept，在使用的地方赋值为true）

**匹配规则优先级:** ClassName > InterfaceName > ContainName


## 使用

在项目的gradle文件引用插件：
```
buildscript {
    repositories {
        maven {
            url 'https://jitpack.io'
        }
    }
    dependencies {
        classpath 'com.github.chenthreetrees:ttreeplugin:2.0.7'
    }
}
```

在app的gradle文件使用插件：
```
apply plugin: 'ttreeplugin'
```

### 使用插件拓展包

项目的gradle文件：
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

在app的gradle文件引用依赖：
```
compile 'com.github.chenthreetrees:ttreepluginext:1.0.6'
```

在application初始化：
`TtreePlugin.init(application)`

**该拓展包主要有以下功能：**

**方法耗时计算**

在需要统计耗时的方法添加注解
`@TimeCost`

**切面注入代码**

在需要注入代码的方法添加注解
`@Cut`，注解值可以传type来区分
在合适的地方添加监听：
```
TtreePlugin.setOnCutListener(new TtreePlugin.IOnCutListener() {
            @Override
            public void onCutEnter(int type)
            {
            }

            @Override
            public void onCutExit(int type)
            {
            }
        });
```
**仿抖动**

在需要防抖动的地方添加`@Debounce(time = 1000,onIntercept = true)`,
time为抖动的时间


**动态权限申请**

**注意**：动态申请的权限需要在manifest注册

在需要动态申请权限的方法添加注解
`@Permission`，权限使用`PermissionConsts`里面的值
例如：
```
@Permission(PermissionConsts.STORAGE)

@Permission({PermissionConsts.STORAGE,PermissionConsts.CAMERA})
```

在合适的地方（早于注解）设置权限申请回调：
```
//权限被拒绝
TtreePlugin.setOnPermissionDeniedListener()
//授权成功
TtreePlugin.setOnPermissionGrantedListener()
```

**数据埋点**

在gradle文件添加过滤条件：

根据需求添加条件，比如想对所有的view的onclick做统一的埋点处理，可以使用如下规则
```
ttree {

    //具体配置
    matchData = [
            'ClassFilter'  : [                   
                    //根据接口名匹配
                    ['InterfaceName': 'android.view.View$OnClickListener',
                     'MethodName': 'onClick', 'MethodDes': '(Landroid/view/View;)V']                    
            ]
}
```

MethodDes表示方法描述符，参考如下：

| 标识字符          | 含义               |
| ------             | ------             |
| B              | 基本类型 byte            |
| C              | 基本类型 char            |
| D              | 基本类型 double            |
| F              | 基本类型 float            |
| I              | 基本类型 int            |
| J              | 基本类型 long            |
| S	             | 基本类型 short            |
| Z	             | 基本类型 boolean            |
| V              | 特殊类型 void            |
| L              | 对象类型，如Ljava/lang/Object;            |

用描述符来描述方法时，按照先参数列表，后返回值的顺序描述，参数列表按顺序放在“()”之内。
对于数组类型，每一维度使用一个前置的“[”字符来描述，如String[][]类型的二维数组，将被记录为“[[Ljava/lang/String;”。
方法`int indexOf(char[] source,int sourceOffset,int sourceCount,String content)`的描述符为“([IILjava/lang/String;)I”。

请详细阅读配置规则，或者参考demo

在合适的地方添加监听：
```
TtreePlugin.setOnTrackListener(new TtreePlugin.IOnTrackListener() {
            @Override
            public void onTrackEnter(String className,String methodName, Object[] objects)
            {  
            }

            @Override
            public void onTrackExit(String className,String methodName, Object[] objects)
            {
            }
        });
```
**编译完成后，可以在app项目路径build\intermediates\transforms\AutoTransform查看最终注入的代码**

**其他使用场景，在平时开发中有遇到，再进行拓展**

**如果想要自定义，请参考demo和源码**












