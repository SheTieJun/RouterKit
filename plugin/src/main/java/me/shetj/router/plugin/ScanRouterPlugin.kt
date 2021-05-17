package me.shetj.router.plugin

import com.android.build.api.transform.*
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.tasks.Workers
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
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
 * 2. 扫描获取到 SRouterKit
 *
 *
 */
class ScanRouterPlugin : Plugin<Project>, Transform() {


    private val routerMap = HashMap<String, String>()
    private var routerJar: JarInput? = null
    private var outputFilePath: File? = null

    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(this)
    }

    override fun getName(): String {
        return "ScanRouterPlugin"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS //表明是 class 文件
    }

    override fun applyToVariant(variant: VariantInfo): Boolean {
        return variant.isDebuggable
    }

    /**
     * 只作用于自己的项目
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {

        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        //当前是否是增量编译
        val isIncremental = transformInvocation.isIncremental

        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        val inputs = transformInvocation.inputs
        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        val outputProvider = transformInvocation.outputProvider
        if (!isIncremental) {
            outputProvider?.deleteAll()
        }
        val  waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        inputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                val outputFile = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                waitableExecutor.execute {
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
                waitableExecutor.execute {
                    if (handJarInput(jarInput, outputFile)) {
                        FileUtils.copyFile(jarInput.file, outputFile)
                    }
                }
            }
        }
        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
        handRouter()
    }

    private fun handRouter() {
        if (routerJar == null) {
            println("路由生成错误，请导入向“me.shetj.router.SRouter”")
            return
        }
        if (outputFilePath == null) {
            println("路由生成错误，无法获取输出路径")
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
                if (ClassMatchKit.isMatchFile(className)) {
                    jos.write(weaveSingleClassToByteArray(input))
                } else {
                    jos.write(IOUtils.toByteArray(input))
                }
                input.close()
                jos.closeEntry()
            }
            jos.close()
            file.close()
            println(tmp)
            println(dest)
            FileUtils.copyFile(tmp, dest)
            FileUtils.deleteQuietly(tmp)
        }
    }

    private fun weaveSingleClassToByteArray(jarFile: InputStream): ByteArray {
        val classReader = ClassReader(jarFile)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classVisitor = HandRouterClassVisitor(routerMap, cw)
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

            node.visibleAnnotations.findLast { an ->
                ClassMatchKit.isMatchAnnotation(an.desc)
            }?.let { an ->
                println(an.desc+"||${an.values[0]}||${an.values[1]}||$className")
                routerMap[an.values[1].toString()] = className
            }
        }
        /*     if (node.invisibleAnnotations.isNullOrEmpty()) {
                 println("invisibleAnnotations = ${node.invisibleAnnotations.isNullOrEmpty()}")
             }else{
                 for (an in node.invisibleAnnotations) {
                     println(":invisibleAnnotations${an.desc}")
                 }
             }
             if (node.visibleTypeAnnotations.isNullOrEmpty()) {
                 println("visibleTypeAnnotations = ${node.visibleTypeAnnotations.isNullOrEmpty()}")
             }else{
                 for (an in node.visibleTypeAnnotations) {
                     println("visibleTypeAnnotations:${an.desc}")
                 }
             }

             if (node.invisibleTypeAnnotations.isNullOrEmpty()) {
                 println("invisibleTypeAnnotations = ${node.invisibleTypeAnnotations.isNullOrEmpty()}")
             }else{
                 for (an in node.invisibleTypeAnnotations) {
                     println("invisibleTypeAnnotations:${an.desc}")
                 }
             }*/
    }


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


}