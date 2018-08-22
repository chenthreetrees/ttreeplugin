package com.threetrees.plugin.asm

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Des:修改字节码
 */
public class AutoModify {

    static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile)
        /** 设置输出到的jar */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()
            String className

            ZipEntry zipEntry = new ZipEntry(entryName)

            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if (entryName.endsWith(".class") && notRClass(entryName) && notSupport(entryName)) {
                className = entryName.replace("/", ".").replace(".class", "")
                modifiedClassBytes = modifyClasses(className, sourceClassBytes)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    static byte[] modifyClasses(String className, byte[] srcByteCode) {
        byte[] classBytesCode = null
        try {
            classBytesCode = modifyClass(srcByteCode)
            return classBytesCode
        } catch (Exception e) {
            e.printStackTrace()
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode
        }
        return classBytesCode
    }
    /**
     * 真正修改类中方法字节码
     */
    private static byte[] modifyClass(byte[] srcClass) throws IOException {

        ClassReader cr = new ClassReader(srcClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new AutoClassVisitor(cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)

        return cw.toByteArray()
    }
    /**
     * 查看修改字节码后的方法
     */
    private static void seeModifyMethod(byte[] srcClass) throws IOException {

        ClassReader cr = new ClassReader(srcClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new AutoClassVisitor(cw)
        cv.seeModifyMethod = true
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
    }

    /**
     * 不是R系列的class
     */
    private static boolean notRClass(String name) {
        return !name.endsWith("R.class") && !name.contains("R\$") && !name.contains("\$") && !name.endsWith("BuildConfig.class")
    }

    /**
     * 不是Support系列包
     */
    private static boolean notSupport(String name) {
        return !name.contains("android/support")
    }
}