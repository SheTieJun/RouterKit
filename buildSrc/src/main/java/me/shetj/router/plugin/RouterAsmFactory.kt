package me.shetj.router.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationContext
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationParameters.None
import org.gradle.api.provider.Property
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor

abstract class RouterAsmFactory: AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        println("RouterAsmFactory createClassVisitor")
        return HandRouterClassVisitor(ClassMatchKit.routerMap,nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {

        return ClassMatchKit.isMatchRouterFile(classData.className)
    }

}