package me.shetj.router

import android.app.Activity
import android.os.Bundle


class RouterOption {
    var path: String ?=null
    var mapInfo: HashMap<String, String>? = null
    var bundle: Bundle? = null
    var requestCode: Int? = null
}

fun startActivity(block:RouterOption.() -> Unit){
    RouterOption().apply(block).let{
        SRouterKit.startActivity(null,it)
    }
}

fun Activity.startActivity(block:RouterOption.() -> Unit){
    RouterOption().apply(block).let{
        SRouterKit.startActivity(this,it)
    }
}