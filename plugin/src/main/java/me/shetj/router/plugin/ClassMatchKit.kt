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

    private const val DEF_ROUTER_FILE = "me.shetj.router.SRouter"
    private const val MATCH_ANNOTATION = "Lme/shetj/annotation/SRouter;"
    fun isMatchFile(root: File, file: File): Boolean {
        return getClassName(root, file) == DEF_ROUTER_FILE
    }

    fun getClassName(root: File, file: File): String {
        val rootPath = root.absolutePath
        val classPath = file.absolutePath
//          println("rootPath =$rootPath")
//          println("classPath =$classPath")
        return classPath.substring(rootPath.length + 1, classPath.length - 6)
            .replace(File.separatorChar.toString(), ".")
    }

    fun isMatchAnnotation(name:String): Boolean {
        return name == MATCH_ANNOTATION
    }
}