package me.shetj.router.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationContext
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationParameters.None
import org.gradle.api.provider.Property
import org.objectweb.asm.ClassVisitor

abstract class ServiceAsmFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {


    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        println("ServiceAsmFactory createClassVisitor")
        return HandServiceClassVisitor(ClassMatchKit.routerMap, nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return ClassMatchKit.isMatchServiceFile(classData.className)
    }

}