package com.ymy.appnest.web

import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.Preference
import java.net.URL

/**
 * Created on 2020/9/1 09:27.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object WebUrlConstant {
    /**
     * 地图业务相关
     */
    var hybridWebUrl by Preference(Preference.HYBRID_WEB_URL, "https://h5prod.dingbaox.com")

    /**
     * 新版教育培训域名
     */
    var quickWebBaseUrl by Preference(
        Preference.QUICK_WEB_BASE_URL,
        "https://quickpage.dingbaox.com/pages/prod"
    )

    /**
     * web页面的域名列表
     */
    var webBaseURLList = listOf(URL(hybridWebUrl).host, URL(quickWebBaseUrl).host)
        get() {
            return listOf(URL(hybridWebUrl).host, URL(quickWebBaseUrl).host)
        }

    /**
     * 发布帮助相关
     */
    @Deprecated("已不再使用该域名")
    var basePhpWebUrl by Preference(Preference.PHP_WEB_BASE_URL, "")

    /**
     * 地图
     */
    var mapUrl = "$hybridWebUrl/map/appMap"
        get() {
            return "$hybridWebUrl/map/appMap"
        }
//
//    /**
//     * 地图选择器
//     */
//     var mapSelectMenuUrl = "$baseUrl/selectmenu"

    /**
     * 表格
     */
    var SheetUrl = "$hybridWebUrl/sheet"
        get() {
            return "$hybridWebUrl/sheet"
        }

    /**
     * 教育培训tab
     */
    var tabTrainingUrl = "$hybridWebUrl/train/train"
        get() {
            return "$hybridWebUrl/train/train"
        }

    /**
     * 用户协议
     */
    var userAgreementUrl = "$hybridWebUrl/userAagreement"
        get() {
            return "$hybridWebUrl/userAagreement"
        }

    /**
     * 隐私协议
     */
    var privacyPolicyUrl = "$quickWebBaseUrl/help_privacyPolicy.html"
        get() {
            return "$quickWebBaseUrl/help_privacyPolicy.html"
        }

    /**
     * 免责声明
     */
    var disclaimerUrl = "$hybridWebUrl/helpPrivacyPolicy"
        get() {
            return "$hybridWebUrl/helpPrivacyPolicy"
        }

    /**
     * 版权声明
     */
    var LicensesUrl = "$hybridWebUrl/appAndroidOpenSourceLicenses"
        get() {
            return "$hybridWebUrl/appAndroidOpenSourceLicenses"
        }

    /**
     * 问题反馈
     */
    var settingFeedBack = "$hybridWebUrl/feedback"
        get() {
            return "$hybridWebUrl/feedback"
        }

    /**
     * 帮助中心
     */
    var helperCenterUrl = "$quickWebBaseUrl/appHelpList.html"
        get() {
            return "$quickWebBaseUrl/appHelpList.html"
        }


    /**
     * 发布页面重要通知
     */
    var publishNoticeImport = "$basePhpWebUrl/notice/createNotice?type=1"
    var publishNoticeLaw = "$basePhpWebUrl/notice/createNotice?type=2"
    var publishNoticeSystem = "$basePhpWebUrl/notice/createNotice?type=3"
    var publishNoticeRewardAndPunishmentSystem = "$basePhpWebUrl/notice/createNotice?type=4"

    /**
     * 发布页面新建日常记录
     */
    var dailyRecordMeeting = "$basePhpWebUrl/daily_record/create_meeting"
    var dailyRecordActivity = "$basePhpWebUrl/daily_record/create_activity"
    var dailyRecordTrain = "$basePhpWebUrl/daily_record/create_train"

    /**
     * 发布页面新建任务
     */
    var createTaskInspection = "$hybridWebUrl/publishInspection"
    var createTaskInspectionSmoke = "$hybridWebUrl/publishSmokeInspection"
    var createTaskHiddenPerils = "$hybridWebUrl/hidden_perils"

    /**
     * 隐患上报
     */
    var createTaskHiddenPerilsReport = "$hybridWebUrl/dangerReport"
    var createTaskPracticeTrain = "$hybridWebUrl/task/train_task"
    var createTaskTrain = "$hybridWebUrl/addTrainTask"

    /**
     * 学习-通知通告
     */
    var tabTrainingNotice = "$basePhpWebUrl/notice/home"


//      报警群相关url
    /**
     * 报警详情
     */
    var alarmDetailWebUrl = "$hybridWebUrl/alarmdetail"

    /**
     * 时间轴组件
     */
    var alarmTimeLineWebUrl = "$hybridWebUrl/timeline"

    /**
     * 报警设备历史记录
     */
    var alarmDeviceHistoryWebUrl = "$hybridWebUrl/imAlarmHistory"

    /**
     * 报警沿途录像
     */
    var alarmVideo = "$hybridWebUrl/imAlarmHistory"

    /**
     * 移动轨迹
     */
    var alarmMoveLineWebUrl = "$hybridWebUrl/map/appMap?pageType=risk"

//      报警群相关url

//      工单群相关url
    /**
     * 工单详情
     */
    var workOrderDetailWebUrl = "$hybridWebUrl/workOrderDetail"
//      工单群相关url

//      巡检群相关url
    /**
     * 巡检详情
     */
    var inspectionDetailWebUrl = "$hybridWebUrl/inspectionHandle"

    /**
     * 巡检处理
     */
    var inspectionHandleWebUrl = "$hybridWebUrl/inspectionHandle"
//      巡检群相关url

//      隐患排查群相关url
    /**
     * 隐患排查处理
     */
    var hiddenDangerHandleWebUrl = "$hybridWebUrl/hiddenDangerTask"
//      隐患排查群相关url

//      预案演练群相关url
    /**
     * 预案演练详情
     */
    var yaylDetailWebUrl = "$hybridWebUrl/planDrillDetail"

    /**
     * 预案演练处理
     */
    var yaylHandleWebUrl = "$hybridWebUrl/planDrillTask"

    /**
     * 预案演练移动轨迹
     */
    var yaylMoveLineWebUrl = "$hybridWebUrl/map/appMap?pageType=practical"

    /**
     * 预案演练聊天历史
     */
    var yaylIMChatHistory = "$hybridWebUrl/imChatHistory"

//      预案演练群相关url

//      实践能力群相关url
    /**
     * 实践能力处理
     */
    var sjpxHandleWebUrl = "$hybridWebUrl/practicalTrainHandle"

    /**
     * 实践能力详情
     */
    var sjpxResultWebUrl = "$hybridWebUrl/practicalTrainResult"
//       实践能力群相关url
//      中控交接群相关url
    /**
     * 中控交接处理
     */
    var zkjjHandleWebUrl = "$hybridWebUrl/handoverDetail"
//       中控交接群相关url


//    我的页面相关

    /**
     * 用户报警历史
     */
    var userAlarmHistoryUrl = "$hybridWebUrl/userAlarmHistory"

    /**
     * 用户巡检历史
     */
    var userInspectionHistoryWebUrl = "$hybridWebUrl/userInspectionHistory"

    /**
     * 用户隐患排查历史
     */
    var userHiddenDangerHistoryWebUrl = "$hybridWebUrl/hiddenDangerHistory"

    /**
     * 用户工单历史
     */
    var userWorkOrderHistoryWebUrl = "$hybridWebUrl/userWorkOrderHistory"

    /**
     * 用户教育培训
     */
    var userTeachHistoryWebUrl = "$hybridWebUrl/userTrainHistory"

    /**
     * 用户实践能力
     */
    var userPlanDrillHistoryWebUrl = "$hybridWebUrl/userPracticalTrainHistory"

    /**
     * 预案演练培训
     */
    var userActingHistoryWebUrl = "$hybridWebUrl/userPlanDrillHistory"
        get() {
            return "$hybridWebUrl/userPlanDrillHistory"
        }


    /**
     * 主机日志
     */
    var userLogHistoryWebUrl = "$hybridWebUrl/fireEnginePrintingHistory"
        get() {
            return "$hybridWebUrl/fireEnginePrintingHistory"
        }

    /**
     * 用户中控交接历史
     */
    var userHandoverHistoryWebUrl = "$hybridWebUrl/userHandoverHistory"
        get() {
            return "$hybridWebUrl/userHandoverHistory"
        }
//    我的页面相关

    /**
     * 网页预览文件，pdf,word,excel
     */
    var previewFileUrl = "$hybridWebUrl/userHandoverHistory?url="


    /**
     *
     * @param url String
     * @param params Map<String, String>
     * @return String
     */
    fun addUrlParams(
        url: String,
        params: Map<String, Any> = mapOf(),
        addTimeParams: Boolean = true,
    ): String {
        val paramsMap = mutableMapOf<String, Any>()
        params.forEach {
            paramsMap[it.key] = it.value
        }
        if (!paramsMap.contains("userID")) {
            paramsMap["userID"] = YmyUserManager.user.userId
        }
        if (!paramsMap.contains("mapid")) {
            paramsMap["mapid"] = YmyUserManager.mapId
        }
        if (!paramsMap.contains("companyId")) {
            paramsMap["companyId"] = YmyUserManager.user.companyId.toString()
        }
        if (!paramsMap.contains("company_id")) {
            paramsMap["company_id"] = YmyUserManager.user.companyId.toString()
        }
        if (!paramsMap.contains("company_alias")) {
            paramsMap["company_alias"] = YmyUserManager.user.companyName
        }
//        if (addTimeParams) {
//            if (!paramsMap.contains("t")) {
//                paramsMap["t"] = System.currentTimeMillis().toString()
//            }
//        }
        if (!paramsMap.contains("token")) {
            paramsMap["token"] = YmyUserManager.user.token
        }
        val sb = StringBuilder(url)
        if (!url.contains("?")) {
            sb.append("?")
        }
        val lastIndexOf = sb.lastIndexOf("?")
        if (lastIndexOf != sb.length - 1) {
            sb.append("&")
        }
        paramsMap.forEach {
            if (!sb.contains("${it.key}=")) {
                sb.append("${it.key}=${it.value}&")
            }
        }
        return sb.substring(0, sb.length - 1).toString()
    }

    /**
     * 手机消息提醒设置
     */
    val phoneNoticehuawei = "$quickWebBaseUrl/androidNoticeHuawei.html"
    val phoneNoticevivo = "$quickWebBaseUrl/androidNoticeVivo.html"
    val phoneNoticeoppe = "$quickWebBaseUrl/androidNoticeOppo.html"
    val phoneNoticexiaomi = "$quickWebBaseUrl/androidNoticeXiaomi.html"
    val phoneNoticemeizu = "$quickWebBaseUrl/androidNoticeMeizu.html"
    val phoneNoticesansung = "$quickWebBaseUrl/androidNoticeSansung.html"


    /**
     * 教育培训短视频学习tab
     */
    var tabLearnTikTokUrl = "$quickWebBaseUrl/trainExamHome.html"
        get() {
            return "$quickWebBaseUrl/trainExamHome.html"
        }

    /**
     * 工作台Url
     */
    var tabWorkBenchUrl = "$quickWebBaseUrl/workbench.html"
        get() {
            return "$quickWebBaseUrl/workbench.html"
        }
}
