package me.shetj.router.plugin

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class RouterMethodVisitor(private val routerMap:HashMap<String,String>, methodVisitor:MethodVisitor):MethodVisitor(Opcodes.ASM7,methodVisitor) {

    override fun visitCode() {
        routerMap.forEach { (path, activity) ->
            println("$path=$activity")
            addRooterToClass(path,activity)
        }
        super.visitCode()
    }

    private fun addRooterToClass(path:String,activity: String) {
        val label0 = Label()
        mv.visitLabel(label0)
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitFieldInsn(Opcodes.GETFIELD, "me/shetj/router/SRouterKit", "routerMap", "Ljava/util/HashMap;");
        mv.visitLdcInsn(path)
        mv.visitLdcInsn(activity)
        mv.visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            "java/util/HashMap",
            "put",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            false
        )
        mv.visitInsn(Opcodes.POP)
        val label2 = Label()
        mv.visitLabel(label2)
        mv.visitLocalVariable(
            "this",
            "Lme/shetj/router/SRouterKit",
            null,
            label0,
            label2,
            0
        )
    }
}