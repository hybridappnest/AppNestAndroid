package com.ymy.core.utils

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * Created on 2020/8/4 17:40.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object FileUtils{
    @JvmStatic
    fun getFileName(pathAndName: String): String? {
        val start = pathAndName.lastIndexOf("/")
        return if (start != -1 && pathAndName.length != -1) {
            pathAndName.substring(start + 1, pathAndName.length)
        } else {
            null
        }
    }

    @JvmStatic
    fun getFileSuffix(pathAndName: String): String? {
        val start = pathAndName.lastIndexOf(".")
        return if (start != -1 && pathAndName.length != -1) {
            pathAndName.substring(start, pathAndName.length)
        } else {
            null
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param file 文件
     * @return `true`: 存在<br></br>`false`: 不存在
     */
    @JvmStatic
    fun isFileExists(file: File?): Boolean {
        return file != null && file.exists()
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    @JvmStatic
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        // 如果存在，是文件则返回 true，是目录则返回 false
        if (file.exists()) return file.isFile
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    @JvmStatic
    fun createOrExistsDir(file: File?): Boolean {
        // 如果存在，是目录则返回 true，是文件则返回 false，不存在则返回是否创建成功
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 目录路径
     * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
     */
    @JvmStatic
    fun createOrExistsDir(dirPath: String?): Boolean {
        return createOrExistsDir(getFileByPath(dirPath))
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    @JvmStatic
    fun getFileByPath(filePath: String?): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    @JvmStatic
    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    @JvmStatic
    fun readyForThemeFolder(context: Context): String? {
        val themeFolder = File(context.filesDir, "theme")
        if (!themeFolder.exists()) {
            themeFolder.mkdirs()
        }
        return themeFolder.absolutePath
    }


    @JvmStatic
    fun deleteFile(url: String?): Boolean {
        var result = false
        val file = File(url)
        if (file.exists()) {
            result = file.delete()
        }
        return result
    }

}