package me.shetj.router.plugin

import com.android.build.api.transform.*
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/5/13 0013<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
class ScanRouterPlugin : Plugin<Project>, Transform() {


    private val routerMap = HashMap<String, String>()
    private var sRouterFile:File ?= null

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

    override fun applyToVariant(variant: VariantInfo):Boolean {
        return variant.isDebuggable
    }

    /**
     * 只作用于自己的项目
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {

        return TransformManager.PROJECT_ONLY
    }

    override fun isIncremental(): Boolean {
        return true
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

        inputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                val dest = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                transformDir(directoryInput.file, dest)
            }
            input.jarInputs.forEach { jarInput ->
                handJarInput(jarInput)
            }

        }

        handRouter()

    }

    private fun handRouter() {
        if (sRouterFile != null) {

            //TODO
        }
    }

    private fun transformJar(file: File, dest: File) {
        println("transformJar:file = ${file.absolutePath}")
    }

    private fun transformDir(file: File, dest: File) {
        readClassWithPath(file, file)
    }


    private fun readClassWithPath(root: File, dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                readClassWithPath(root, file)
            } else {
                val filePath = file.absolutePath
                if (!filePath.endsWith(".class")) return
                val className = ClassMatchKit.getClassName(root, file)
                addRouteMap(filePath, className)
            }
        }
    }

    private fun addRouteMap(filePath: String, className: String) {
        addRouteMap(FileInputStream(filePath), className)
    }

    private fun addRouteMap(ins: InputStream, className: String) {
        println("className = $className")
        val reader = ClassReader(ins)
        val node = ClassNode()  // class node = ClassVisitor
        reader.accept(node, ClassWriter.COMPUTE_MAXS)
        if (node.visibleAnnotations.isNotEmpty()) {
            node.visibleAnnotations.findLast { an ->
                ClassMatchKit.isMatchAnnotation(an.values[0].toString())
            }?.let {an ->
                routerMap[an.values[0].toString()] = className
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


    private fun handJarInput(jarInput: JarInput) {
        if (jarInput.file.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(jarInput.file)
            val enumeration = jarFile.entries()
            //用于保存
            while (enumeration.hasMoreElements()) {
                val jarEntry =  enumeration . nextElement () as JarEntry
                val entryName = jarEntry.name
                val inputStream = jarFile . getInputStream (jarEntry)
                if (entryName.endsWith(".class")) {
                    println("entryName ->$entryName")
                    val classReader =   ClassReader(inputStream)
                    val node =   ClassNode()
                    classReader.accept(node, ClassWriter.COMPUTE_MAXS)
                    if (node.visibleAnnotations.isNotEmpty()) {
                        node.visibleAnnotations.findLast { an ->
                            ClassMatchKit.isMatchAnnotation(an.values[0].toString())
                        }?.let {an ->
                            routerMap[an.values[0].toString()] = entryName
                        }
                    }
                }
            }
            jarFile.close()
        }
    }


}