package me.shetj.router.plugin

import org.objectweb.asm.ClassReader
import java.io.IOException

/**
 * 用来判断是否实现指定接口
 */
object InterfaceImplChecked {

    /**
     * 只能用简单的因为，如果用其他2个方法，会找不到类
     */
    fun hasImplInterfacesSim(reader: ClassReader, interfaceSet: Set<String>): Boolean {
        if (isObject(reader.className)) {
            return false
        }
        return try {
            reader.interfaces.forEach {
                if (interfaceSet.contains(it)) {
                    return true
                }
            }
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 判断是否实现了指定接口
     *
     * @param reader       class reader
     * @param interfaceSet interface collection
     * @return check result
     */
    fun hasImplSpecifiedInterfaces(reader: ClassReader, interfaceSet: Set<String>): Boolean {
        if (isObject(reader.className)) {
            return false
        }
        return try {
            if (containedTargetInterface(reader.interfaces, interfaceSet)) {
                true
            } else {
                println(reader.superName)
                val parent = ClassReader(reader.superName)
                hasImplSpecifiedInterfaces(parent, interfaceSet)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查当前类是 Object 类型
     *
     * @param className class name
     * @return checked result
     */
    private fun isObject(className: String): Boolean {
        return "java/lang/Object" == className
    }

    /**
     * 检查接口及其父接口是否实现了目标接口
     *
     * @param interfaceList 待检查接口
     * @param interfaceSet  目标接口
     * @return checked result
     * @throws IOException exp
     */
    @Throws(IOException::class)
    private fun containedTargetInterface(
        interfaceList: Array<String>,
        interfaceSet: Set<String>
    ): Boolean {

        for (inter in interfaceList) {
            if (interfaceSet.contains(inter)) {
                return true
            } else {
                val reader = ClassReader(inter)
                if (containedTargetInterface(reader.interfaces, interfaceSet)) {
                    return true
                }
            }
        }
        return false
    }
}