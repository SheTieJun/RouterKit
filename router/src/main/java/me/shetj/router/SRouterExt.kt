package me.shetj.router

import android.app.Activity
import android.net.Uri
import android.os.Bundle

/**
 * 跳转参数控制
 */
class RouterOption {
    var path: String? = null //路由
    var intentInfo: HashMap<String, String>? = null   //intent.put(key,value)
    var bundle: Bundle? = null
    var requestCode: Int? = null
}

/**
 * 该跳转不支持requestCode
 */
fun startActivity(block: RouterOption.() -> Unit): Boolean {
    return RouterOption().apply(block).let {
        SRouterKit.startActivity(null, it)
    }
}


/**
 * 通过到scheme进行跳转，
 * schemeWithHostAndPath = scheme://Host/Path?queryParameterNames
 * example:
 * startActivity("router://activity/router2?key=1&key2=2")
 */
fun startActivityByScheme(schemeWithHostAndPath: String) {
    startActivity {
        Uri.parse(schemeWithHostAndPath).also { uri ->
            this.path = "${uri.scheme}://${uri.host}${uri.path}"
            uri.queryParameterNames?.also {
                this.intentInfo = HashMap()
            }?.onEach { key ->
                uri.getQueryParameter(key)?.let { this.intentInfo?.put(key, it) }
            }
        }
    }
}

/**
 *  支持requestCode
 */
fun Activity.startActivity(block: RouterOption.() -> Unit): Boolean {
    return RouterOption().apply(block).let {
        SRouterKit.startActivity(this, it)
    }
}