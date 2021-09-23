package com.ymy.appnest.di

import com.ymy.core.ok3.DBXRetrofitClient
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import com.ymy.appnest.ConfigManager
import com.ymy.appnest.net.QuickApiClient
import com.ymy.appnest.net.reporsity.NodeRepository
import com.ymy.appnest.viewmodel.AppConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
val viewModelModule = module {
    viewModel { AppConfigViewModel(get()) }
}

val repositoryModule = module {
    single {
        DBXRetrofitClient.getService(
            QuickApiClient::class.java,
            ConfigManager.nodeBaseUrl
        )
    }
    single { NodeRepository(get()) }
}

val managerModule = module {
}

val scopeViewModelModule = module {
    scope(named("PICKER_IMAGE_SCOPE")) {
        scoped { PickerViewModel() }
    }
}

val appModule = listOf(viewModelModule, repositoryModule, scopeViewModelModule, managerModule)