package me.shetj.service

import android.util.Log


class SModuleServiceKit private constructor() {

    private val isLoadService:Any = Any()

    companion object {

        private const val TAG = "SModuleServiceKit"

        var isDebug = false

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

    }

    private val serviceMap: HashMap<String, String> = HashMap()

    private val serviceImpMap: HashMap<String, Any> = HashMap()


    /**
     * 通过Transform，扫描获取到的class
     * 利用ASM加载到map
     */
    private fun loadServiceMap() {
        Log.e(TAG, "load load Service error :please use routerPlugin [https://github.com/SheTieJun/RouterKit] to add service")
    }


    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): T? {
        return try {
            synchronized(isLoadService){
                //防止创建了多个模块服务
                val service = serviceImpMap[name]
                if (service != null) {
                    service as T
                } else {
                    serviceMap[name]?.let { serviceName ->
                        Class.forName(serviceName).newInstance() as T
                    }?.also {
                        serviceImpMap[name] = it
                    }
                }
            }
        } catch (e: Exception) {
            if (isDebug) {
                Log.e(TAG, " can't find this $name with service")
            }
            e.printStackTrace()
            null
        }
    }

    fun put(name: String, serviceName: String) {
        serviceMap[name] = serviceName
    }
}