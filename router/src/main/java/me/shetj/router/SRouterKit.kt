package me.shetj.router

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.lang.NullPointerException


/**
 * 路由入口类
 * 1. 初始化
 * 2. 跳转
 */
class SRouterKit private constructor() {

    companion object {

        const val  TAG ="SRouter"

        private lateinit var application: Context

        var isDebug = false

        @Volatile
        private var sRouter: SRouterKit? = null

        @JvmStatic
        fun init(context :Context) {
            application = context.applicationContext
            getInstance().loadRouterMap()
        }

        @JvmStatic
        fun getInstance(): SRouterKit {
            return sRouter ?: synchronized(SRouterKit::class) {
                SRouterKit().also {
                    sRouter = it
                }
            }
        }

    }

    private val routerMap:HashMap<String,String> = HashMap()


    /**
     * 根据路由[path]跳转到对应的界面
     * @param context 上下文
     * @param path 路由
     */
    fun startJump(context: Context, path:String){
        val classPath = routerMap[path]
        if (classPath.isNullOrBlank()){
            if (isDebug){
                Log.e(TAG, "startJump: can't find path ")
            }
            return
        }
        Intent().apply {
            component = ComponentName(
                context,
                classPath
            )
        }.let {
            context.startActivity(it)
        }
    }

    /**
     * 根据路由[path]跳转到对应的界面
     * @param path 路由
     */
    fun startJump(path:String){
        val classPath = routerMap[path]
        if (classPath.isNullOrBlank()){
            if (isDebug){
                Log.e(TAG, "startJump: can't find path ")
            }
            return
        }
        Intent().apply {
            component = ComponentName(
                application,classPath
            )
        }.let {
            application.startActivity(it)
        }
    }

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

}