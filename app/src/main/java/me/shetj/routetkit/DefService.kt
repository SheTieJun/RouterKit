package me.shetj.routetkit

import me.shetj.annotation.SModuleService
import me.shetj.service.IModuleService


@SModuleService("defService")
class DefService:IModuleService {

    fun getName() = "defService"

}