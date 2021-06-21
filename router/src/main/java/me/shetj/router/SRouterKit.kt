package me.shetj.router

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log


/**
 * 路由入口类
 * 1. 初始化
 * 2. 跳转
 */
class SRouterKit private constructor() {

    companion object {

        private const val TAG = "SRouter"

        private lateinit var application: Context

        var isDebug = false

        @Volatile
        private var sRouter: SRouterKit? = null

        /**
         * 最好是在线程初始化
         */
        @JvmStatic
        fun init(context: Context) {
            getInstance().loadRouterMap()
            application = context.applicationContext
        }

        @JvmStatic
        fun getInstance(): SRouterKit {
            return sRouter ?: synchronized(SRouterKit::class) {
                SRouterKit().also {
                    sRouter = it
                }
            }
        }

        /**
         * 用来修改或者添加 无法添加路由注解的activity
         */
        inline fun <reified T : Activity> addToRouter(path: String, isReplace: Boolean = false) {
            getInstance().addToRouter(path, T::class.qualifiedName.toString(), isReplace)
        }

        /**
         * 根据路由[path]跳转到对应的界面，请优先使用[context]
         * @param context 上下文
         * @param path 路由
         * @param mapInfo 需要传递的信息
         * @param bundle 需要传递的信息通过bundle传递
         */
        @JvmStatic
        fun startActivity(
            context: Context? = null,
            path: String,
            mapInfo: HashMap<String, String>? = null,
            bundle: Bundle? = null,
            requestCode: Int? = null
        ) {
            if (!Companion::application.isInitialized) {
                error("you should init first: \"SRouterKit.init(context)\"")
            }
            if (requestCode != null && context == null && context !is Activity) {
                error("if you want to use \"startActivityForResult()\" , context no be null and context is a \"Activity\"")
            }
            if (context != null){
                getInstance().start(context, path, mapInfo, bundle,requestCode)
            }else{
                getInstance().start(path, mapInfo, bundle)
            }
        }
    }

    private val routerMap: HashMap<String, String> = HashMap()


    /**
     * 根据路由[path]跳转到对应的界面，请优先使用[context]
     * @param context 上下文
     * @param path 路由
     * @param mapInfo 需要传递的信息
     * @param bundle 需要传递的信息通过bundle传递
     */
    private fun start(
        context: Context,
        path: String,
        mapInfo: HashMap<String, String>? = null,
        bundle: Bundle? = null,
        requestCode: Int? = null
    ) {
        getIntentByPath(context, path)?.let { intent ->
            mapInfo?.forEach {
                intent.putExtra(it.key, it.value)
            }
            if (requestCode != null && (context is Activity)) {
                context.startActivityForResult(intent, requestCode, bundle)
            } else {
                context.startActivity(intent,bundle)
            }
        }
    }

    private fun start(
        path: String,
        mapInfo: HashMap<String, String>? = null,
        bundle: Bundle? = null,
    ) {
        getIntentByPath(null, path)?.let { intent ->
            mapInfo?.forEach {
                intent.putExtra(it.key, it.value)
            }
            application.startActivity(intent,bundle)
        }
    }

    fun addToRouter(path: String, activity: String, isReplace: Boolean = false) {
        if (!isReplace && routerMap.containsKey(path)) {
            Log.e(TAG, "load router error :path($path) already exists")
            return
        }
        this.routerMap[path] = activity
    }

    /**
     * 给ASM 用来添加路由的
     * 因为kotlin的ASM 操作存在一些问题，比如 map,还有？
     *
     * 应该是ASM 对于kotlin的类型推导存在问题
     */
    private fun loadRouter(path: String, activity: String) {
        addToRouter(path, activity, false)
    }

    private fun getIntentByPath(context: Context?, path: String): Intent? {
        return checkAndGet(path)?.let { classPath ->
            getIntent(context, classPath = classPath)
        }
    }

    /**
     * 通过Transform，扫描获取到的class
     * 利用ASM加载到map
     */
    private fun loadRouterMap() {
        Log.e(TAG, "load router error :please use routerPlugin [https://github.com/SheTieJun/RouterKit] add routerMap")
    }


    private fun checkAndGet(path: String): String? {
        val classPath = routerMap[path]
        if (classPath.isNullOrBlank()) {
            if (isDebug) {
                Log.e(TAG, "startJump: can't find this path ($path:$classPath)")
            }
            return null
        }
        return classPath
    }

    private fun getIntent(context: Context? = null, classPath: String): Intent {
        return Intent().apply {
            component = ComponentName(
                context ?: application, classPath
            )
        }.also { intent ->
            if (context == null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}