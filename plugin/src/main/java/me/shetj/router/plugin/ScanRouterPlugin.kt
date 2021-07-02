package me.shetj.router.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.json.JSONObject
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * transform 扫描
 * 1. 扫描带注解的
 * 2. 扫描获取到路由,并且通过ASM插入到 SRouterKit
 * 3. 扫描获取到ModuleService,并且通过ASM插入到 SModuleServiceKit
 *
 */
class ScanRouterPlugin : Plugin<Project>, Transform() {

    private lateinit var logger: Logger
    private val routerMap = HashMap<String, String>()
    private val serviceMap = HashMap<String, String>()
    private var routerJar: JarInput? = null
    private var outputFilePath: File? = null
    private lateinit var rootFile:File

    override fun apply(project: Project) {
        //确保只能在含有application的build.gradle文件中引入
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Android Application plugin required")
        }
        logger =  project.logger
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(this)
        rootFile = project.projectDir
    }

    override fun getName(): String {
        return "ScanRouterPlugin"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS //表明是 class 文件
    }

    /**
     * 作用所有项目，aar 导入是在jar 中
     *
     * 只有当前项目是在：input.directoryInputs ，其他都是：input.jarInputs
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {

        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        val currentTimeMillis = System.currentTimeMillis()
        //当前是否是增量编译
        val isIncremental = transformInvocation.isIncremental
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        val inputs = transformInvocation.inputs
        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        val outputProvider = transformInvocation.outputProvider
        if (!isIncremental) {
            outputProvider?.deleteAll()
        }
        val waitAbleExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        inputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                val outputFile = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                waitAbleExecutor.execute {
                    transformDir(directoryInput.file, outputFile)
                    FileUtils.copyDirectory(directoryInput.file, outputFile)
                }
            }
            input.jarInputs.forEach { jarInput ->

                val outputFile = outputProvider.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                waitAbleExecutor.execute {
                    if (handJarInput(jarInput, outputFile)) {
                        FileUtils.copyFile(jarInput.file, outputFile)
                    }
                }
            }
        }
        waitAbleExecutor.waitForTasksWithQuickFail<Any>(true)
        handRouterAndService()
        logger.warn("scan router and moduleService use time : "+(System.currentTimeMillis() - currentTimeMillis) +"ms")
    }

    /**
     * 把扫描获取的路由和ModuleService写入到文件
     */
    private fun handRouterAndService() {
        if (routerJar == null) {
            logger.error("RouterPlugin处理错误，请导入向:RouterKit[https://github.com/SheTieJun/RouterKit]")
            return
        }
        if (outputFilePath == null) {
            logger.error("RouterPlugin处理错误，无法获取输出路径")
            return
        }
        routerJar?.let {
            val jarFile = routerJar!!.file
            val tmp = File(jarFile.parent, jarFile.name + ".tmp")
            if (tmp.exists()) tmp.delete()
            val file = JarFile(jarFile)
            val dest = outputFilePath
            val enumeration = file.entries()
            val jos = JarOutputStream(FileOutputStream(tmp))

            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val className = entryName.replace("/", ".")
                val input = file.getInputStream(jarEntry)
                jos.putNextEntry(zipEntry)
                when {
                    ClassMatchKit.isMatchRouterFile(className) -> {
                        jos.write(weaveRouterSingleClassToByteArray(input))
                    }
                    ClassMatchKit.isMatchServiceFile(className) -> {
                        jos.write(weaveServiceSingleClassToByteArray(input))
                    }
                    else -> {
                        jos.write(IOUtils.toByteArray(input))
                    }
                }
                input.close()
                jos.closeEntry()
            }
            jos.close()
            file.close()
            logger.info("文件输出：$dest")
            FileUtils.copyFile(tmp, dest)
            FileUtils.deleteQuietly(tmp)
            outOutMapJson()
        }
    }

    /**
     * 输出文件
     */
    private fun outOutMapJson() {
        try {
            FileUtils.write(File(rootFile!!.absolutePath +"${File.separator}routerPluginJson${File.separator}map.json"),JSONObject(routerMap).toString())
            FileUtils.write(File(rootFile!!.absolutePath +"${File.separator}routerPluginJson${File.separator}service.json"),JSONObject(serviceMap).toString())
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun weaveRouterSingleClassToByteArray(jarFile: InputStream): ByteArray {
        val classReader = ClassReader(jarFile)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classVisitor = HandRouterClassVisitor(routerMap, cw)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    private fun weaveServiceSingleClassToByteArray(jarFile: InputStream): ByteArray {
        val classReader = ClassReader(jarFile)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classVisitor = HandServiceClassVisitor(serviceMap, cw)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }


    private fun transformDir(file: File, dest: File) {
        readClassWithPath(file, file, dest)
    }


    private fun readClassWithPath(root: File, input: File, outputFile: File) {
        val srcDirPath = input.absolutePath
        val destDirPath = outputFile.absolutePath
        input.listFiles()?.forEach { file ->
            val destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            if (file.isDirectory) {
                readClassWithPath(root, file, destFile)
            } else {
                val filePath = file.absolutePath
                if (ClassMatchKit.isNeedIgClass(file)) return
                val className = ClassMatchKit.getClassName(root, file)
                addRouteMap(filePath, className)
            }
        }
    }


    private fun addRouteMap(filePath: String, className: String) {
        addRouteMap(FileInputStream(filePath), className)
    }

    private fun addRouteMap(ins: InputStream, className: String) {
        val reader = ClassReader(ins)
        val node = ClassNode()  // class node = ClassVisitor
        reader.accept(node, ClassWriter.COMPUTE_MAXS)
        if (node.visibleAnnotations.isNotEmpty()) {
            node.visibleAnnotations.forEach { an ->
                handleClassWithAnnotation(an, className, reader)
            }
        }
    }


    private fun handJarInput(jarInput: JarInput, outputFile: File): Boolean {
        if (jarInput.file.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(jarInput.file)
            //如果是google的jar,就不处理，可能会存在错误处理，但是，后续可以进行配置
            if (ClassMatchKit.isSystemJar(jarFile.name)){
                jarFile.close()
                // 进行默认处理
                return true
            }
            logger.info(jarFile.name)
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
                    //需要放到最后处理
                    return false
                }
                if (!ClassMatchKit.isNeedIgClass(className)) {
                    try {
                        val classReader = ClassReader(inputStream)
                        val node = ClassNode()
                        classReader.accept(node, ClassWriter.COMPUTE_MAXS)
                        if (node.visibleAnnotations == null) continue
                        if (node.visibleAnnotations?.isNotEmpty() == true) {
                            node.visibleAnnotations?.forEach { an ->
                                handleClassWithAnnotation(an, className, classReader)
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


    /**
     * 处理class :通过注解
     */
    private fun handleClassWithAnnotation(
        an: AnnotationNode,
        className: String,
        classReader: ClassReader
    ) {
        if (ClassMatchKit.isMatchRouterAnnotation(an.desc)) {
            logger.info("router:" + an.desc + "||${an.values[0]} = $className")
            routerMap[an.values[1].toString()] = className
        }
        if (ClassMatchKit.isMatchServiceAnnotation(an.desc)) {
            if (ClassMatchKit.checkHasServiceImpl(classReader)) {
                logger.info("service:" + an.desc + "||${an.values[0]}=$className")
                serviceMap[an.values[1].toString()] = className
            } else {
                logger.error("$className has ${an.desc} but not impl IModuleService ")
            }
        }
    }


}