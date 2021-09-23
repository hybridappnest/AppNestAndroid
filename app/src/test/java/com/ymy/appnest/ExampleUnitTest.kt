package com.ymy.appnest

import com.ymy.core.ok3.GsonUtils
import com.ymy.appnest.beans.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val list = mutableListOf<HomeBottomNavigation>()
        list.add(HomeBottomNavigation("测试", url = "https://www.baidu.com/", type = HomeTabType.web))
        list.add(HomeBottomNavigation("测试", url = "https://www.baidu.com/", type = HomeTabType.web))
        list.add(HomeBottomNavigation("测试", url = "https://www.baidu.com/", type = HomeTabType.web))
        list.add(HomeBottomNavigation("测试", url = "https://www.baidu.com/", type = HomeTabType.web))
        list.add(HomeBottomNavigation("测试", type = HomeTabType.mine))

        val dataList = mutableListOf<MineItemBean>()
        dataList.add(
            MineItemBean()
        )
        dataList.add(
            MineItemBean(
                "设置中心",
                action = MineListAction.settingCenter
            )
        )
        dataList.add(
            MineItemBean(
                "扫一扫",
                action = MineListAction.QRCodeScanner
            )
        )
        dataList.add(
            MineItemBean(
                "帮助中心",
                action = MineListAction.web,
                url = "https://www.baidu.com/"
            )
        )
        dataList.add(
            MineItemBean(
                "问题反馈",
                action = MineListAction.web,
                url = "https://www.baidu.com/"
            )
        )
        dataList.add(
            MineItemBean(
                "关于鼎保信",
                action = MineListAction.about
            )
        )

        println(GsonUtils.mGson.toJson(AppConfig(list, dataList)))
    }
}