package me.shetj.router.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL


class RouterMethodVisitor(private val routerMap:HashMap<String,String>, methodVisitor:MethodVisitor):MethodVisitor(Opcodes.ASM9,methodVisitor) {

    override fun visitCode() {
        routerMap.forEach { (path, activity) ->
            addRooterToClass(path,activity)
        }
    }

    private fun addRooterToClass(path:String,activity: String) {
        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(path);
        mv.visitLdcInsn(activity);
        mv.visitMethodInsn(INVOKEVIRTUAL, "me/shetj/router/SRouterKit", "loadRouter", "(Ljava/lang/String;Ljava/lang/String;)V", false);
    }
}