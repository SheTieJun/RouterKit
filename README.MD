## Router 路由

目标需求：
1. 只要求支持Activity，暂不需要其他那些花里胡哨的功能
2. 尽量快的完成获取到路由信息

### 实现思路
- 注解 :路由切入点和资源路径
- Transform:扫描带路由的文件，收集注解的值和对应class
- 通过ASM 操作，把收集的数据插装到对应方法 `loadRouterMap`
- 路由组件初始化的时候内部调用`loadRouterMap`完成路由注册

### 更新日志
1. 2021年5月15日：已经初步实现路由跳转Activity
2. 2021年5月17日：优化相关处理
    1. 路由场景不适合增量，因为需要每次把“所有的路由”信息重新写入class
    2. 新增多线程处理扫描，提高扫描效率
    3. 添加跳转方法

### 遇到的问题
1.文件扫描
> Transform 扫描文件，对特定文件进行忽略
```kotlin
    private val DEFAULT_EXCLUDE = arrayListOf(  "^android\\..*",
        "^org..*",
        "^com.google.android..*",
        "^kotlin..*",
        "^androidx\\..*",
        ".*\\.R$",
        ".*\\.R\\$.*$",
        ".*\\.BuildConfig$")
```

2.Jar的class处理
> Jar的处理和directory的处理方式不同，虽然知识处理其中一个class,但是需要输出在同一个jar
```kotlin
private fun handJarInput(jarInput: JarInput, outputFile: File): Boolean {
        if (jarInput.file.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(jarInput.file)
            val enumeration = jarFile.entries()
            //用于保存
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement() as JarEntry
                val entryName = jarEntry.name
                val inputStream = jarFile.getInputStream(jarEntry)
                val className = entryName.replace("/", ".")
                if (ClassMatchKit.isMatchFile(className)) {
                    routerJar = jarInput
                    outputFilePath = outputFile
                    return false
                }
                if (!ClassMatchKit.isNeedIgClass(className)) {
                    try {
                        val classReader = ClassReader(inputStream)
                        val node = ClassNode()
                        classReader.accept(node, ClassWriter.COMPUTE_MAXS)
                        if (node.visibleAnnotations == null) continue
                        if (node.visibleAnnotations?.isNotEmpty() == true) {
                            node.visibleAnnotations?.findLast { an ->
                                ClassMatchKit.isMatchAnnotation(an.desc)
                            }?.let { an ->
                                routerMap[an.values[1].toString()] = className
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            jarFile.close()
            return true
        }
        return false
    }

```
3. ASM字节码操作
> 可以使用插件ASMPlugin ，生成文件的字节码，
> 但是需要自己等一下基础知识
> 比如栈，比如对象
> 遇到了操作对象中的hashMap,jar异常错误
`java.lang.IncompatibleclassChangeBrror: Found class java.util.HashMap，but interface was expected (declaration of 'java.util.HashMap' appears in /apex/com. android.runtime/javalib/core-oj.jar)`
> 最后通过取巧解决了，操作其他类的方法，来替代操作map方法

```kotlin
    override fun visitCode() {
        routerMap.forEach { (path, activity) ->
            addRooterToClass(path,activity)
        }
    }

    private fun addRooterToClass(path:String,activity: String) {
        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(path);
        mv.visitLdcInsn(activity);
        mv.visitMethodInsn(INVOKEVIRTUAL, "me/shetj/router/SRouterKit", "loadRouter", "(Ljava/lang/String;Ljava/lang/String;)V", false);
    }
```

```kotlin
    /**
     * 通过Transform，扫描获取到的class
     * 利用ASM加载到map
     */
    private  fun loadRouterMap(){
        Log.e(TAG,"load router error :please use routerPlugin add routerMap")
    }

    fun loadRouter(path: String,activity: String){
        if (routerMap.containsKey(path)){
            Log.e(TAG,"load router error :path already exists")
            return
        }
        this.routerMap[path] = activity
    }
```
4. ASM编译错误,当参数传 context ?= null的会出现编译异常 TypeNotPresentException
> 解决方法：写2个方法，空和不空分开处理