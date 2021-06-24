package me.shetj.exception

import java.lang.RuntimeException

/**
 * 用来防止混淆异常
 */
open class BaseException(msg:String):RuntimeException(msg) {
}