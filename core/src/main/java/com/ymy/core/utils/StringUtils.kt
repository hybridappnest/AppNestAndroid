package com.ymy.core.utils

/**
 * Created on 12/29/20 14:07.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object UrlUtils {
    const val FILE_TYPE_URL = 0
    const val FILE_TYPE_OFFICE = 1
    const val FILE_TYPE_PDF = 2
    const val FILE_TYPE_IMAGE = 3
    const val FILE_TYPE_VIDEO = 4
    fun checkUrlIsFile(url: String): Int {
        if ((url.endsWith(".doc")
                    or url.endsWith(".DOC")
                    or url.endsWith(".docx")
                    or url.endsWith(".DOCX")
                    or url.endsWith(".xls")
                    or url.endsWith(".XLS")
                    or url.endsWith(".xlsx")
                    or url.endsWith(".XLSX")
                    or url.endsWith(".xlsm")
                    or url.endsWith(".XLSM"))
        ) {
            return FILE_TYPE_OFFICE
        }
        if (url.endsWith(".pdf") or url.endsWith(".PDF")) {
            return FILE_TYPE_PDF
        }
        if (
            url.endsWith(".jpg")
            or url.endsWith(".JPG")
            or url.endsWith(".jpeg")
            or url.endsWith(".JPEG")
            or url.endsWith(".png")
            or url.endsWith(".PNG")
        ) {
            return FILE_TYPE_IMAGE
        }
        if (
            url.endsWith(".mp4")
            or url.endsWith(".mp3")
            or url.endsWith(".flv")
        ) {
            return FILE_TYPE_VIDEO
        }
        return FILE_TYPE_URL
    }
}