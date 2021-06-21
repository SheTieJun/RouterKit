package me.shetj.annotation


/**
 * 用来标记各个模块提供的服务
 */

@Target(AnnotationTarget.CLASS)
@Retention
@Repeatable
@MustBeDocumented
annotation class SModuleService(val name:String)
