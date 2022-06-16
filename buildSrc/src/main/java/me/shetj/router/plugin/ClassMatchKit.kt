package me.shetj.router.plugin

import groovy.json.JsonBuilder
import org.objectweb.asm.ClassReader
import java.io.File
import org.gradle.internal.impldep.com.amazonaws.services.s3.model.JSONOutput
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/5/13 0013<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
object ClassMatchKit {

    private const val DEF_ROUTER_FILE = "me.shetj.router.SRouterKit"
    private const val DEF_SERVICE_FILE = "me.shetj.service.SModuleServiceKit"
    private const val MATCH_ROUTER_ANNOTATION = "Lme/shetj/annotation/SRouter;"
    private const val MATCH_SERVICE_ANNOTATION = "Lme/shetj/annotation/SModuleService;"

    const val MODULE_SERVICE_INTERFACE = "me/shetj/service/IModuleService"
    const val MODULE_SERVICE_INTERFACE_2 = "me.shetj.service.IModuleService"

    const val SCAN_CHANGE_ROUTER_METHOD = "loadRouterMap"
    const val SCAN_CHANGE_SERVICE_METHOD = "loadServiceMap"

    var rootFile:File ?=null
    val serviceMap = HashMap<String, String>()
    val routerMap = HashMap<String, String>()

    private val DEFAULT_EXCLUDE_JAR = arrayListOf(
        "jetified-annotations",
        "jetified-kotlin-stdlib-common",
        "jetified-kotlin-stdlib",
        "jetified-core-ktx",
        "jetified-viewpager2",
        "jetified-appcompat-resources",
        "vectordrawable-animated",
        "legacy-support-core-utils",
        "jetified-activity",
        "versionedparcelable",
        "lifecycle-runtime",
        "lifecycle-viewmodel",
        "jetified-savedstate",
        "localbroadcastmanager",
        "lifecycle-livedata-core",
        "core-runtime",
        "lifecycle-common",
        "jetified-annotation-experimental",
        "constraintlayout-solver",
        "androidx.collection",
        "androidx.annotation",
        "androidx.arch.core"
    )


    private val DEFAULT_EXCLUDE = arrayListOf(
        "^android\\..*",
        "^org..*",
        "^com.google.android..*",
        "^kotlin..*",
        "^androidx\\..*",
        "^io.reactivex..*",
        ".*\\.R$",
        ".*\\.R\\$.*$",
        "^io.reactivex..*",
        ".*\\.BuildConfig$"
    )

    fun outOutMapJson() {
        try {
            FileUtils.write(
                File(rootFile?.absolutePath + "${File.separator}routerPluginJson${File.separator}router.json"),
                JsonBuilder(routerMap).toString(),"UTF-8")
            FileUtils.write(
                File(rootFile?.absolutePath + "${File.separator}routerPluginJson${File.separator}service.json"),
                JsonBuilder(serviceMap).toString(),"UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断是不是【路由或者服务】组件
     */
    fun isMatchFile(name: String): Boolean {
        return isMatchRouterFile(name) || isMatchServiceFile(name)
    }

    fun isMatchRouterFile(name: String): Boolean {
        return name.startsWith(DEF_ROUTER_FILE)
    }

    fun isMatchServiceFile(name: String): Boolean {
        return name.startsWith(DEF_SERVICE_FILE)
    }

    fun getClassName(root: File, file: File): String {
        val rootPath = root.absolutePath
        val classPath = file.absolutePath
        return classPath.substring(rootPath.length + 1, classPath.length - 6)
            .replace(File.separatorChar.toString(), ".")
    }


    fun checkHasServiceImpl(reader: ClassReader): Boolean {
        return InterfaceImplChecked.hasImplInterfacesSim(
            reader,
            setOf(MODULE_SERVICE_INTERFACE, MODULE_SERVICE_INTERFACE_2)
        )
    }

    /**
     * 是否时路由
     */
    fun isMatchRouterAnnotation(name: String): Boolean {
        return name == MATCH_ROUTER_ANNOTATION
    }

    /**
     * 是否是服务
     */
    fun isMatchServiceAnnotation(name: String): Boolean {
        return name == MATCH_SERVICE_ANNOTATION
    }

    /**
     * 是否忽略
     */
    fun isNeedIgClass(file: File): Boolean {
        val name = file.name
        return isNeedIgClass(name)
    }

    fun isNeedIgClass(name: String): Boolean {
        return !name.endsWith(".class") || name.startsWith("R\$")
                || "R.class" == name || "BuildConfig.class" == name || isSystemClass(name)
    }

    private fun isSystemClass(fileName: String): Boolean {
        DEFAULT_EXCLUDE.forEach { exclude ->
            if (fileName.matches(exclude.toRegex())) return true
        }
        return false
    }

    internal fun isSystemJar(fileName: String): Boolean {
        DEFAULT_EXCLUDE_JAR.forEach { exclude ->
            if (fileName.contains(exclude)) return true
        }
        return false
    }
}