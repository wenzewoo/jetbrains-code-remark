package com.github.wenzewoo.coderemark.expand

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun VirtualFile.getRelativePath(project: Project): String? {
    return project.basePath?.length?.let { this.canonicalPath?.substring(it) }
}

fun VirtualFile.versionHash(project: Project): String {
    try {
        val instance = MessageDigest.getInstance("MD5")
        val digest = instance.digest(
            if (this.isWritable) this.getContentByteArray() else this.getRelativePath(project)?.toByteArray()
        )
        val result = StringBuffer()
        for (b in digest) {
            val i: Int = b.toInt() and 0xff
            var hex = Integer.toHexString(i)
            if (hex.length < 2)
                hex = "0$hex"
            result.append(hex)
        }
        return result.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun VirtualFile.getContentByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    this.inputStream.use {
        it.copyTo(outputStream)
    }
    return outputStream.toByteArray()
}