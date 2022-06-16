@file:Suppress("DEPRECATION")

package me.shetj.router.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import groovy.json.JsonBuilder
import java.io.File
import java.nio.file.Files.createFile
import org.apache.tools.ant.util.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.logging.Logger

/**
 * transform 扫描
 * 1. 扫描带注解的
 * 2. 扫描获取到路由,并且通过ASM插入到 SRouterKit
 * 3. 扫描获取到ModuleService,并且通过ASM插入到 SModuleServiceKit
 *
 */
class ScanRouterPlugin : Plugin<Project> {

    private lateinit var logger: Logger
    private val routerMap = HashMap<String, String>()
    private val serviceMap = HashMap<String, String>()
    private lateinit var rootFile: File
    private lateinit var projectName: String

    val artifactType = Attribute.of("artifactType", String::class.java)
    val minified = Attribute.of("minified", Boolean::class.javaObjectType)

    override fun apply(project: Project) {
        //确保只能在含有application的build.gradle文件中引入
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Android Application plugin required")
        }

        logger = project.logger
        rootFile = project.projectDir
        projectName = project.displayName

        //来用扫描
        project.dependencies.registerTransform(TreeTransform::class.java) {
            it.from.attribute(minified, false).attribute(artifactType, "jar")
            it.to.attribute(minified, true).attribute(artifactType, "jar")
        }
        //用老插入
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            println("onVariants:${variant.name}")
            variant.instrumentation.transformClassesWith(
                ServiceAsmFactory::class.java,
                InstrumentationScope.ALL
            ) {

            }
            variant.instrumentation.transformClassesWith(
                RouterAsmFactory::class.java,
                InstrumentationScope.ALL
            ) {

            }
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }

        project.gradle.buildFinished {
            outOutMapJson()
        }
    }

    private fun outOutMapJson() {
        try {
            println(JsonBuilder(ClassMatchKit.routerMap).toString())
            println(JsonBuilder(ClassMatchKit.serviceMap).toString())
            ClassMatchKit.routerMap.clear()
            ClassMatchKit.serviceMap.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}