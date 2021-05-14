package me.shetj.router.plugin

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Opcodes.GETSTATIC


class RouterMethodVisitor(private val routerMap:HashMap<String,String>, methodVisitor:MethodVisitor):MethodVisitor(Opcodes.ASM9,methodVisitor) {

    override fun visitCode() {
        routerMap.forEach { (path, activity) ->
            println("$path=$activity")
            addRooterToClass(path,activity)
        }
        super.visitCode()
    }

    private fun addRooterToClass(path:String,activity: String) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("测试测试");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false)

    }
}