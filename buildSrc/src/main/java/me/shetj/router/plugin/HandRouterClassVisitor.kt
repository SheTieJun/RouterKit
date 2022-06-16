package me.shetj.router.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


/**
 * 处理类
 */
class HandRouterClassVisitor(
    private val routerMap: HashMap<String, String>,
    classVisitor: ClassVisitor
) :
    ClassVisitor(Opcodes.ASM9, classVisitor), Opcodes {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return if (name == ClassMatchKit.SCAN_CHANGE_ROUTER_METHOD) {
            RouterMethodVisitor(
                routerMap,
                super.visitMethod(access, name, descriptor, signature, exceptions)
            )
        } else {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }
    }

}