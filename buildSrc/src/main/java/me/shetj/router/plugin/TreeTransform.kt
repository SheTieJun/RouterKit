package me.shetj.router.plugin

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters.None
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

abstract class TreeTransform : TransformAction<None> {


    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>


    override fun transform(outputs: TransformOutputs) {
        println("Running transform on ${inputArtifact.get().asFile.absoluteFile}")
        handJarInput(inputArtifact.get().asFile)
        outputs.file(inputArtifact)
    }


    private fun handJarInput(jarInput: File): Boolean {
        if (jarInput.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(jarInput)
            //如果是google的jar,就不处理，可能会存在错误处理，但是，后续可以进行配置
            if (ClassMatchKit.isSystemJar(jarFile.name)) {
                jarFile.close()
                // 进行默认处理
                return true
            }
            val enumeration = jarFile.entries()
            //用于保存
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement() as JarEntry
                val entryName = jarEntry.name
                val inputStream = jarFile.getInputStream(jarEntry)
                val className = entryName.replace("/", ".")
                if (!ClassMatchKit.isNeedIgClass(className)) {
                    try {
                        val classReader = ClassReader(inputStream)
                        val node = ClassNode()
                        classReader.accept(node, ClassWriter.COMPUTE_MAXS)
                        if (node.visibleAnnotations == null) continue
                        if (node.visibleAnnotations?.isNotEmpty() == true) {
                            node.visibleAnnotations?.forEach { an ->
                                // className.substring(0,className.length - 6) 去掉.class
                                handleClassWithAnnotation(
                                    an,
                                    className.substring(0, className.length - 6),
                                    classReader
                                )
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
            println("router:" + an.desc + "||${an.values[0]} = $className")
            ClassMatchKit.routerMap[an.values[1].toString()] = className
        }
        if (ClassMatchKit.isMatchServiceAnnotation(an.desc)) {
            if (ClassMatchKit.checkHasServiceImpl(classReader)) {
                println("service:" + an.desc + "||${an.values[0]}=$className")
                ClassMatchKit.serviceMap[an.values[1].toString()] = className
            } else {
                println("$className has ${an.desc} but not impl IModuleService ")
            }
        }
    }


}