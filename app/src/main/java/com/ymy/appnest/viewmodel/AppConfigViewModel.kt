package com.ymy.appnest.viewmodel

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import com.ymy.appnest.ConfigManager
import com.ymy.appnest.appContext
import com.ymy.appnest.beans.AppConfigResp
import com.ymy.appnest.net.reporsity.NodeRepository
import com.ymy.appnest.web.custom.H5WebView
import com.ymy.core.base.BaseViewModel
import com.ymy.core.datastore.DataStoreHelper
import com.ymy.core.exts.fromJson
import com.ymy.core.ok3.GsonUtils.mGson
import com.ymy.core.ok3.checkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class AppConfigViewModel constructor(private val service: NodeRepository) : BaseViewModel() {
    private val mMutableLiveData = MutableLiveData<AppConfigUiModel>()
    val mUiData: LiveData<AppConfigUiModel> get() = mMutableLiveData

    private val appConfig = stringPreferencesKey("appConfig")

    fun initAppConfig() {
        viewModelScope.launch(Dispatchers.Main) {
            val appConfig = async(Dispatchers.IO) {
                service.getAppConfig()
            }
            val result = appConfig.await()
            result.checkResult(onSuccess = {
                val appConfigResps: MutableList<AppConfigResp> = mGson.fromJson(it)
                val configJson = appConfigResps[0].config
                ConfigManager.mAppConfig = mGson.fromJson(configJson)
                if (ConfigManager.mAppConfig.mainTabList.isEmpty()) {
                    getConfigFromCache()
                } else {
                    saveAppConfig(configJson)
                    emitUiState(true)
                }
            }, onError = {
                getConfigFromCache()
            })
        }
    }

    private fun getConfigFromCache() {
        viewModelScope.launch(Dispatchers.Main) {
            val cacheConfig = async(Dispatchers.IO) {
                getAppConfigFromCache()
            }
            val cacheConfigJson = cacheConfig.await()
            ConfigManager.mAppConfig = mGson.fromJson(cacheConfigJson)
            if (ConfigManager.mAppConfig.mainTabList.isEmpty()) {
                val loadNativeAppConfig = loadNativeAppConfig()
                ConfigManager.mAppConfig = mGson.fromJson(loadNativeAppConfig)
                emitUiState(false)
            } else {
                emitUiState(true)
            }
        }
    }

    private fun loadNativeAppConfig(): String {
        var content = ""
        try {
            val instream: InputStream = appContext.assets.open("main.json")
            instream.buffered().reader().use { reader ->
                content = reader.readText()
            }
        } catch (e: FileNotFoundException) {
            Logger.e("AppConfig", "main.json", "The File doesn't not exist.")
        } catch (e: IOException) {
            Logger.e("AppConfig", "main.json", "", e.message)
        }
        return content
    }

    private suspend fun getAppConfigFromCache(): String {
        return DataStoreHelper.getFromDataStore(appConfig)
    }

    private fun saveAppConfig(configJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DataStoreHelper.saveToDataStore(appConfig, configJson)
        }
    }

    private fun emitUiState(
        appConfig: Boolean = false,
    ) {
        mMutableLiveData.value = AppConfigUiModel(appConfig)
    }


    data class AppConfigUiModel(
        val appConfig: Boolean,
    )
}