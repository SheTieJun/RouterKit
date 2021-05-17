package me.shetj.router.plugin

import java.io.File

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/5/13 0013<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
object ClassMatchKit {

    private const val DEF_ROUTER_FILE = "me.shetj.router.SRouterKit"
    private const val MATCH_ANNOTATION = "Lme/shetj/annotation/SRouter;"
    const val SCAN_CHANGE_METHOD = "loadRouterMap"


    private val DEFAULT_EXCLUDE = arrayListOf(  "^android\\..*",
        "^org..*",
        "^com.google.android..*",
        "^kotlin..*",
        "^androidx\\..*",
        ".*\\.R$",
        ".*\\.R\\$.*$",
        ".*\\.BuildConfig$")


    fun isMatchFile(name: String): Boolean {
//        println(name)
        return name.startsWith(DEF_ROUTER_FILE)
    }

    fun getClassName(root: File, file: File): String {
        val rootPath = root.absolutePath
        val classPath = file.absolutePath
//          println("rootPath =$rootPath")
//          println("classPath =$classPath")
        return classPath.substring(rootPath.length + 1, classPath.length - 6)
            .replace(File.separatorChar.toString(), ".")
    }

    /**
     * 是否时路由
     */
    fun isMatchAnnotation(name: String): Boolean {
        return name == MATCH_ANNOTATION
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
                || "R.class" == name || "BuildConfig.class" == name ||isSystemClass(name)
    }

    private fun isSystemClass(fileName:String): Boolean {
        DEFAULT_EXCLUDE.forEach {exclude ->
            if (fileName.matches(exclude.toRegex())) return true
        }
        return false
    }
}