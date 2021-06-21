package me.shetj.routetkit

import me.shetj.annotation.SModuleService
import me.shetj.service.IModuleService


@SModuleService("defService")
class DefServiceImp:DefService {
    override fun getName() = "defService"
}