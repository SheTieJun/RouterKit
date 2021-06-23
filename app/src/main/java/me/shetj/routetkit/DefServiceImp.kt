package me.shetj.routetkit

import me.shetj.annotation.SModuleService
import me.shetj.service.IModuleService


@SModuleService("defService")
class DefServiceImp:UserService,IModuleService{

    private val nameConfig:String = "shetj" + System.currentTimeMillis()

    override fun getName() = nameConfig
    override fun getAge(): Int  = 25
}