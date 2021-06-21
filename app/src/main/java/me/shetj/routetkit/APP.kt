package me.shetj.routetkit

import android.app.Application
import android.content.Context
import me.shetj.router.SRouterKit
import me.shetj.service.SModuleServiceKit

/**
 * **@packageName：** com.ebu.master<br></br>
 * **@author：** shetj<br></br>
 * **@createTime：** 2018/2/26<br></br>
 * **@company：**<br></br>
 * **@email：** 375105540@qq.com<br></br>
 * **@describe**<br></br>
 */
class APP : Application() {
    override fun onCreate() {
        super.onCreate()

        SRouterKit.isDebug = true
        SRouterKit.init(this)
        SRouterKit.addToRouter<Router2Activity>("activity/router2")

        SModuleServiceKit.init()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}