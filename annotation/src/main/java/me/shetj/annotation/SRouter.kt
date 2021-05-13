package me.shetj.annotation

/**
 *
 * [SRouter] 用标记需要路由的类
 *
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/5/12 0012<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */

@Target(AnnotationTarget.CLASS)
@Retention
@Repeatable
@MustBeDocumented
annotation class SRouter (val path:String)