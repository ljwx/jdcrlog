package com.jdcr.jdcrlog.util

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal fun File.keepLastNLines(maxLines: Int): Boolean {
    if (maxLines <= 0) return false
    if (!exists() || !isFile) return true
    if (length() == 0L) return true
    val parent = parentFile
    if (parent == null || !parent.exists() && !parent.mkdirs()) {
        return false
    }
    val deque = ArrayDeque<String>(maxLines.coerceAtMost(4096))
    try {
        BufferedReader(
            InputStreamReader(FileInputStream(this), StandardCharsets.UTF_8)
        ).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                if (deque.size == maxLines) deque.removeFirst()
                deque.addLast(line)
            }
        }
    } catch (e: Exception) {
        return false
    }
    val temp = File(parent, "$name.${System.nanoTime()}.tmp")
    return try {
        BufferedWriter(
            OutputStreamWriter(FileOutputStream(temp), StandardCharsets.UTF_8)
        ).use { w ->
            deque.forEach { line ->
                w.write(line)
                w.newLine()
            }
        }
        replaceWithTempFile(temp)
    } catch (e: Exception) {
        temp.delete()
        false
    }
}
/** 用 [temp] 覆盖当前文件；尽量 rename，失败则 copy。 */
private fun File.replaceWithTempFile(temp: File): Boolean {
    if (!temp.exists()) return false
    return try {
        if (exists()) {
            if (!delete()) {
                temp.copyTo(this, overwrite = true)
                temp.delete()
                return true
            }
        }
        if (!temp.renameTo(this)) {
            temp.copyTo(this, overwrite = true)
            temp.delete()
        }
        true
    } catch (e: Exception) {
        try {
            temp.delete()
        } catch (_: Exception) {
        }
        false
    }
}