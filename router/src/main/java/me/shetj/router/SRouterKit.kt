package me.shetj.router

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import me.shetj.exception.NoRouteFoundException
import me.shetj.exception.NoServiceFoundException
import me.shetj.exception.RouterAlreadyExistException
import kotlin.reflect.KClass


/**
 * 路由入口类
 * 1. 初始化
 * 2. 跳转
 */
class SRouterKit private constructor() {

    companion object {

        private const val TAG = "SRouter"

        private lateinit var application: Context

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
         * @param path 路由地址
         * @param isReplace 是否替换
         * @return Boolean 自定义跳转路由，true 成功，false失败
         * 用来修改或者添加 无法添加路由注解的activity
         */
        fun <T : Activity> addToRouter(
            path: String,
            activity: KClass<T>,
            isReplace: Boolean = false
        ): Boolean {
            try {
                getInstance().addToRouter(path, activity.qualifiedName.toString(), isReplace)
                return true
            } catch (e: RouterAlreadyExistException) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * @param option 跳转参数控制
         * @return true 跳转成功，跳转失败
         */
        @JvmStatic
        fun startActivity(context: Context? = null, option: RouterOption): Boolean {
            return startActivity(
                context,
                option.path,
                option.intentInfo,
                option.bundle,
                option.requestCode
            )
        }


        /**
         * @param block 跳转参数控制
         * @return true 跳转成功，跳转失败
         */
        @JvmStatic
        fun startActivity(context: Context? = null, block: RouterOption.() -> Unit): Boolean {
            return RouterOption().apply(block).let { option ->
                startActivity(
                    context,
                    option.path,
                    option.intentInfo,
                    option.bundle,
                    option.requestCode
                )
            }
        }

        /**
         * 通过到scheme进行跳转，
         * schemeWithHostAndPath = scheme://Host/Path?queryParameterNames
         * example:
         * startActivity("router://activity/router2?key=1&key2=2")
         */
        fun startActivity(schemeWithHostAndPath: String) {
            startActivityByScheme(schemeWithHostAndPath)
        }


        /**
         * 根据路由[path]跳转到对应的界面，请优先使用[context]
         * @param context 上下文
         * @param path 路由
         * @param mapInfo 需要传递的信息
         * @param bundle 需要传递的信息通过bundle传递
         */
        @JvmStatic
        @JvmOverloads
        fun startActivity(
            context: Context? = null,
            path: String? = null,
            mapInfo: HashMap<String, String>? = null,
            bundle: Bundle? = null,
            requestCode: Int? = null
        ): Boolean {
            try {
                if (!Companion::application.isInitialized) {
                    error("you should init first: \"SRouterKit.init(context)\"")
                }
                if (path.isNullOrEmpty()) {
                    error("path not null")
                }
                if ((requestCode != null && context == null) || (requestCode != null && context !is Activity)) {
                    error("if you want to use \"startActivityForResult()\" , context no be null and context is a \"Activity\"")
                }
                if (context != null) {
                    getInstance().start(context, path, mapInfo, bundle, requestCode)
                } else {
                    getInstance().start(path, mapInfo, bundle)
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }

    /**
     * 用来保存路由注册表
     */
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
        getIntentByPath(context, path).let { intent ->
            mapInfo?.forEach {
                intent.putExtra(it.key, it.value)
            }
            if (requestCode != null && (context is Activity)) {
                context.startActivityForResult(intent, requestCode, bundle)
            } else {
                context.startActivity(intent, bundle)
            }
        }
    }


    /**
     * 通过全局上下文，跳转到对应界面
     * @param path 路由
     * @param mapInfo 需要传递的信息
     * @param bundle 需要传递的信息通过bundle传递
     * @throws NoServiceFoundException 没有找到路由
     */
    private fun start(
        path: String,
        mapInfo: HashMap<String, String>? = null,
        bundle: Bundle? = null,
    ) {
        getIntentByPath(null, path).let { intent ->
            mapInfo?.forEach {
                intent.putExtra(it.key, it.value)
            }
            application.startActivity(intent, bundle)
        }
    }

    /**
     * 添加新的路由相关信息
     * @param path 路径
     * @param activity 对应的activity
     * @param isReplace 是否进行覆盖
     * @throws RouterAlreadyExistException 添加路由失败，因为已经存在了
     */
    private fun addToRouter(path: String, activity: String, isReplace: Boolean = false) {
        if (!isReplace && routerMap.containsKey(path)) {
            throw RouterAlreadyExistException("add Router fail ,load router error :path($path) already exists")
        }
        this.routerMap[path] = activity
    }

    /**
     * 给ASM 用来添加路由的
     * 因为kotlin的ASM 操作存在一些问题，比如 map,还有？
     *
     * 应该是ASM 对于kotlin的类型推导存在问题
     */
    @Suppress("unused")
    private fun loadRouter(path: String, activity: String) {
        addToRouter(path, activity, false)
    }

    /**
     * 通过path,构建intent
     * 1. 构建scheme Intent
     * 2. 构建path对应的Activity Intent
     * @throws NoServiceFoundException 没有找到路由
     */
    private fun getIntentByPath(context: Context?, path: String): Intent {
        val data: Uri = Uri.parse(path)
        val intent = Intent(Intent.ACTION_VIEW, data)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return if (intent.scheme.isNullOrEmpty()) {
            getIntent(
                context, classPath = checkAndGet(path)
            )
        } else {
            intent
        }
    }

    /**
     * 通过Transform，扫描获取到的class
     * 利用ASM加载到map
     */
    private fun loadRouterMap() {
        Log.e(
            TAG, "load router error :" +
                    "please use routerPlugin [https://github.com/SheTieJun/router_plugin] add routerMap"
        )
    }

    /**
     * 通过path 获取对应的activity
     * @throws NoRouteFoundException 没有对应的路由
     */
    private fun checkAndGet(path: String): String {
        val classPath = routerMap[path]
        if (classPath.isNullOrBlank()) {
            throw NoRouteFoundException("There is no router match the path : [ $path ]")
        }
        return classPath
    }

    /**
     * 获取intent
     */
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

    /**
     * 检查是否有Scheme对应的activity
     */
    @SuppressLint("QueryPermissionsNeeded")
    @Deprecated("暂时无用，这个只能找准确的，不能模糊查询")
    private fun checkUrlScheme(intent: Intent): Boolean {
        val packageManager: PackageManager = application.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.isNotEmpty()
    }
}