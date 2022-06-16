package me.shetj.router.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL


/**
 *遇到了操作对象中的hashMap,jar异常错误
 *
 *`java.lang.IncompatibleclassChangeBrror: Found class java.util.HashMap，
 * but interface was expected (declaration of 'java.util.HashMap'
 * appears in /apex/com. android.runtime/javalib/core-oj.jar)`
 *
 *最后通过取巧解决了，操作其他类的方法，来替代操作map方法
 */
class RouterMethodVisitor(
    private val routerMap: HashMap<String, String>,
    methodVisitor: MethodVisitor
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {


    override fun visitEnd() {
        super.visitEnd()
        routerMap.forEach { (path, activity) ->
            addRooterToClass(path, activity)
        }
    }


    private fun addRooterToClass(path: String, activity: String) {
        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(path);
        mv.visitLdcInsn(activity);
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "me/shetj/router/SRouterKit",
            "loadRouter",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            false
        );
    }
}