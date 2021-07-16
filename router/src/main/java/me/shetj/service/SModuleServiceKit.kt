package me.shetj.service

import android.util.Log
import me.shetj.exception.NoServiceFoundException
import me.shetj.exception.ServiceAlreadyExistException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


class SModuleServiceKit private constructor() {

    companion object {

        private const val TAG = "SModuleServiceKit"

        @Volatile
        private var serviceKit: SModuleServiceKit? = null

        @JvmStatic
        fun init() {
            getInstance().loadServiceMap()
        }

        @JvmStatic
        fun getInstance(): SModuleServiceKit {
            return serviceKit ?: synchronized(SModuleServiceKit::class) {
                SModuleServiceKit().also {
                    serviceKit = it
                }
            }
        }

        /**
         * 手动添加服务
         */
        fun <T : IModuleService> addService(
            name: String,
            moduleService: KClass<T>,
            isReplace: Boolean = false
        ): Boolean {
            try {
                getInstance().put(name, moduleService.qualifiedName.toString(), isReplace)
                return true
            } catch (e: ServiceAlreadyExistException) {
                e.printStackTrace()
            }
            return false
        }

    }

    /**
     * 用来服务表
     */
    private val serviceMap: HashMap<String, String> = HashMap()

    /**
     * 用来保存实例
     */
    private val serviceImpMap: HashMap<String, Any> = HashMap()


    /**
     * 通过Transform，扫描获取到的class
     * 利用ASM加载到map
     */
    private fun loadServiceMap() {
        Log.e(
            TAG,
            "load load Service error :please " +
                    "use routerPlugin [https://github.com/SheTieJun/RouterKit] to add service"
        )
    }

    fun <T> get(name: String): T? {
        return get(name, true)
    }

    /**
     *
     * @param name 服务实例对应的名称
     * @param isSingle 是否是单例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String, isSingle: Boolean): T? {
        return try {
            if (isSingle) {
                synchronized(this) {
                    //防止创建了多个模块服务
                    val service = serviceImpMap[name]
                    if (service != null) {
                        service as T
                    } else {
                        createInstance<T>(name)?.also {
                            serviceImpMap[name] = it
                        }
                    }
                }
            } else {
                createInstance<T>(name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建对象
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> createInstance(name: String): T {
        return findService(name).let { serviceName ->
            Class.forName(serviceName).kotlin.createInstance() as T
        }
    }

    /**
     * @throws NoServiceFoundException 没有对应的服务
     */
    private fun findService(name: String): String {
        return serviceMap[name] ?: kotlin.run {
            throw NoServiceFoundException(
                "There is no ModuleService match the name : [ $name ] " +
                        "\nyou should make ModuleService implementation IModuleService and add annotation 'SModuleService' "
            )
        }
    }

    private fun put(name: String, serviceName: String) {
        put(name, serviceName, false)
    }

    private fun put(name: String, serviceName: String, isReplace: Boolean) {
        if (!isReplace && serviceMap.containsKey(name)) {
            throw ServiceAlreadyExistException("add service fail ,put service error :service($name) already exists")
        }
        serviceMap[name] = serviceName
    }

    fun remove(name: String) {
        serviceMap.remove(name)
        serviceImpMap.remove(name)
    }

    fun clean() {
        serviceImpMap.clear()
    }
}