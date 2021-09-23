package com.ymy.core.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.ymy.core.Ktx
import java.io.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by luyao
 * on 2018/1/19 15:50
 */
class Preference<T>(val name: String, private val default: T) : ReadWriteProperty<Any?, T> {

    companion object {
        const val SETTING_DATA_KEY = "dbx_setting"
        const val USER_ACCOUNT_ID = "user_account_id"
        const val USER_AGREEMENTS_HAS_CONFIRM = "user_agreements_has_confirm"
        const val USER_LEARN_GUIDE_HAS_SHOW = "USER_LEARN_GUIDE_HAS_SHOW"
        const val USER_LOGIN_NAME = "user_login_name"
        const val USER_LOGIN_HAS_CLICK_AGREEMENTS= "user_login_has_click_agreements"

        const val PHP_WEB_BASE_URL = "PHP_WEB_BASE_URL"
        const val HYBRID_WEB_URL = "HYBRID_WEB_URL"
        const val JAVA_BASE_URL = "JAVA_BASE_URL"
        const val QUICK_WEB_BASE_URL = "QUICK_WEB_BASE_URL"
        const val NODE_BASE_URL = "NODE_BASE_URL"

        const val WEB_ENV = "WEB_ENV"

        const val IM_CHAT_INPUT_LAYOUT_TYPE = "IM_CHAT_INPUT_LAYOUT_TYPE"

        const val LOCAL_DOMAIN_NAME = "LOCAL_DOMAIN_NAME"
        const val LOCAL_DOMAIN_PLATFORM = "LOCAL_DOMAIN_PLATFORM"
        const val LOCAL_IS_OPEN_PASSWORD = "LOCAL_IS_OPEN_PASSWORD"

        const val SETTING_NEED_VIDEO_WATERMARK = "SETTING_NEED_VIDEO_WATERMARK"
    }

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(Ktx.app)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getValue(name, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putValue(name, value)
    }

    @SuppressLint("CommitPrefEdits")
    private fun <T> putValue(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> putString(name, serialize(value))
        }.apply()
    }

    fun <T> getValue(name: String, default: T): T = with(prefs) {
        val res: Any? = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> {
                val string = getString(name, null)
                if (string == null) {
                    default
                } else {
                    try {
                        deSerialization(string)
                    } catch (e: Exception) {
                        default
                    }
                }
            }
        }
        return res as T
    }

    /**
     * 删除全部数据
     */
    fun clearPreference() {
        prefs.edit().clear().apply()
    }

    /**
     * 根据key删除存储数据
     */
    fun clearPreference(key: String) {
        prefs.edit().remove(key).apply()
    }

    /**
     * 序列化对象
     * @param person
     * *
     * @return
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun <A> serialize(obj: A): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(
            byteArrayOutputStream
        )
        objectOutputStream.writeObject(obj)
        var serStr = byteArrayOutputStream.toString("ISO-8859-1")
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8")
        objectOutputStream.close()
        byteArrayOutputStream.close()
        return serStr
    }

    /**
     * 反序列化对象
     * @param str
     * *
     * @return
     * *
     * @throws IOException
     * *
     * @throws ClassNotFoundException
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun <A> deSerialization(str: String): A {
        val redStr = java.net.URLDecoder.decode(str, "UTF-8")
        val byteArrayInputStream = ByteArrayInputStream(
            redStr.toByteArray(charset("ISO-8859-1"))
        )
        val objectInputStream = ObjectInputStream(
            byteArrayInputStream
        )
        val obj = objectInputStream.readObject() as A
        objectInputStream.close()
        byteArrayInputStream.close()
        return obj
    }


    /**
     * 查询某个key是否已经存在
     *
     * @param key
     * @return
     */
    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    fun getAll(): Map<String, *> {
        return prefs.all
    }
}