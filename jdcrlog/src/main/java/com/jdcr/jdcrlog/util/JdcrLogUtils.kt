package com.jdcr.jdcrlog.util

import android.util.Log
import com.jdcr.jdcrlog.JdcrLogBase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.RandomAccessFile
import java.nio.charset.Charset

object JdcrLogUtils {

    private val DEFAULT_READ_LINES = 100
    private var lastLine = 1

    fun getLatest(): Result<List<String>> {
        val filePath = JdcrLogBase.filePath
        if (!filePath.isNullOrEmpty()) {
            val result = readLines(filePath, lastLine, DEFAULT_READ_LINES)
            result.onSuccess {
                lastLine += DEFAULT_READ_LINES
            }.onFailure {
                Log.d(JdcrLogBase.baseLogTag, "读取异常:" + it.message)
            }
            return result
        } else {
            "没开日志".let {
                Log.d(JdcrLogBase.baseLogTag, it)
                return Result.failure(IllegalStateException(it))
            }
        }
    }

    fun resetReadLine() {
        lastLine = 1
    }

    private fun readFileSimple(
        filePath: String,
        reverse: Boolean,
        startLine: Int,
        readLines: Int
    ): Result<List<String>> {
        var reader: FileReader?
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(IllegalStateException("日志文件不存在"))
        }
        try {
            reader = FileReader(file)
            reader.useLines { lines ->
                if (reverse) {
                    return Result.success(
                        lines.toList().asReversed()
                            .drop(startLine - 1)
                            .take(readLines)
                    )
                }
                return Result.success(
                    lines
                        .drop(startLine - 1)//从第几行开始
                        .take(readLines)//取几行
                        .toList()
                )
            }
        } catch (e: Exception) {
            "读取日志出现异常".apply {
                Log.w(JdcrLogBase.baseLogTag, this, e)
                return Result.failure(IllegalStateException(this, e))
            }
        }
    }

    private fun readLastNLines(
        filePath: String,
        startFromEnd: Int,
        maxLines: Int,
        charset: Charset = Charsets.UTF_8
    ): Result<List<String>> {
        return try {
            if (startFromEnd <= 0 || maxLines <= 0) {
                return Result.success(emptyList())
            }

            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(FileNotFoundException("文件不存在: $filePath"))
            }

            val fileSize = file.length()
            if (fileSize == 0L) {
                return Result.success(emptyList())
            }

            val result = mutableListOf<String>()
            val buffer = ByteArray(4096)
            val lineBuffer = ByteArrayOutputStream()

            var linesToSkip = startFromEnd - 1
            var linesToRead = maxLines
            var totalLinesFound = 0

            RandomAccessFile(file, "r").use { raf ->
                var position = fileSize - 1

                while (position >= 0 && (totalLinesFound < linesToSkip + linesToRead)) {
                    // 安全计算读取大小
                    val bytesToRead = minOf(buffer.size.toLong(), position + 1).toInt()
                    position -= bytesToRead

                    // 确保seek位置不为负
                    val seekPos = maxOf(0L, position)
                    raf.seek(seekPos)

                    val bytesRead = raf.read(buffer, 0, bytesToRead)
                    if (bytesRead <= 0) break

                    // 从后往前处理字节
                    for (i in bytesRead - 1 downTo 0) {
                        if (buffer[i] == '\n'.code.toByte() || buffer[i] == '\r'.code.toByte()) {
                            if (lineBuffer.size() > 0) {
                                totalLinesFound++

                                // 跳过不需要的行
                                if (totalLinesFound <= linesToSkip) {
                                    lineBuffer.reset()
                                    continue
                                }

                                // 收集需要的行
                                val lineBytes = lineBuffer.toByteArray().apply { reverse() }
                                val line = String(lineBytes, charset).trimEnd('\r', '\n')
                                if (line.isNotEmpty()) {
                                    result.add(0, line)
                                }
                                lineBuffer.reset()

                                if (result.size >= linesToRead) break
                            }
                        } else {
                            lineBuffer.write(buffer[i].toInt())
                        }
                    }

                    if (result.size >= linesToRead) break
                }

                // 处理文件开头的最后一行
                if (lineBuffer.size() > 0 && totalLinesFound < linesToSkip + linesToRead) {
                    totalLinesFound++
                    if (totalLinesFound > linesToSkip && result.size < linesToRead) {
                        val lineBytes = lineBuffer.toByteArray().apply { reverse() }
                        val line = String(lineBytes, charset).trimEnd('\r', '\n')
                        if (line.isNotEmpty()) {
                            result.add(0, line)
                        }
                    }
                }
            }

            Result.success(result)
        } catch (e: Exception) {
            "读取日志出现异常".let {
                Log.w(JdcrLogBase.baseLogTag, it, e)
                Result.failure(IllegalStateException(it, e))
            }
        }
    }

    private fun readLines(
        filePath: String,
        startLine: Int,
        maxLines: Int,
        charset: Charset = Charsets.UTF_8
    ): Result<List<String>> {
        return try {
            if (startLine <= 0 || maxLines <= 0) {
                return Result.success(emptyList())
            }

            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(FileNotFoundException("文件不存在: $filePath"))
            }

            if (file.length() == 0L) {
                return Result.success(emptyList())
            }

            val result = mutableListOf<String>()
            var currentLine = 0

            file.inputStream().bufferedReader(charset).use { reader ->
                reader.lineSequence()
                    .forEach { line ->
                        currentLine++
                        if (currentLine < startLine) return@forEach
                        if (result.size >= maxLines) return@forEach

                        result.add(line.trimEnd('\r', '\n'))
                    }
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}