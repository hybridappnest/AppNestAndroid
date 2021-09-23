# Android壳子

|  壳子已有功能 |  
|---|
|  可配置的由web页面组成的首页 | 
|  可打开web页面，支持带参数 | 
| 可关闭web页面，并将参数带回 | 
|  js调起图片选择，选择图片或视频，或拍照录制视频，并上传oss， 之后组织参数回传给web |
|  二维码扫描 |   
|  js图片视频预览 |  
|  全局异常处理 |   
|  原生权限请求 |   
|  定位功能 | 

|  壳子功能展望 |  
|---|
|原生登录页面（需要与具体业务结合）| 
|推送厂商渠道接入（没有IM推送，为了服务端方便，可能还是要使用阿里推送等集成推送方案）|
|首页中我的tab原生页面（可以抽取简单页面结构，具体功能需要与业务结合）|
|设置页面基础功能（清楚缓存，关于App等）|
|我的tab json配置|
|首页底部tab json配置|
|登录页面web化，原生提供js方法写入用户数据（用户数据需要有一定的规则）|
|配置统一化|
|闪屏页广告资源下载|

## 简介

使用H5页面搭建一个基础应用，包含于基础功能的js交互功能 支持功能

* 首页多个H5页面
* 打开独立H5页面
* 支持js图片，视频选择上传
* 支持js拍照，录像上传
* 支持js图片，视频轮播预览
* 包含原生网络请求框架（okhttp3 + retrofit）
* 包含推送框架（厂商）
* 包含依赖注入框架（koin）
* 包含基础页面结构（支持ViewBinding,ViewModel等）
* 全局异常捕获
* 扫描功能（华为扫码库实现）
* 权限检查及请求
* 基础交互弹窗，提示Toast
* 登录基础页面(逻辑需要自行添加)

## 一、H5首页tab配置

相关类HomeTabFragment,HomeBottomNavigationItem

### 1、HomeTabFragment为首页展示的具体UI结构

其中mHomeBottomNavigationList控制展示的底部栏tab个数及相关样式

```kotlin

private val mHomeBottomNavigationList = mutableListOf<HomeBottomNavigationItem>(
    HomeBottomNavigationItem.NAV_1,
    HomeBottomNavigationItem.NAV_2,
    HomeBottomNavigationItem.NAV_3,
    HomeBottomNavigationItem.NAV_4,
    HomeBottomNavigationItem.NAV_5,
)
```

HomeBottomNavigationItem为封装tab图标，文字，颜色,页面Url等相关配置

```kotlin
sealed class HomeBottomNavigationItem constructor(
    val name: Int,
    val imageRes: Int,
    val imageResSelected: Int,
    var unRead: Int = 0,
    val textColorRes: Int = R.color.gray595F7E,
    val textColorResSelected: Int = R.color.blue32B8EC,
    var isSelected: Boolean = false,
    var url: String = "",
) 
```

mHomeBottomNavigationList 与 fragmentList 保持一一对应

### 2、WebViewFragment为具体的web页面容器

WebViewFragment构造方法支持参数如下

| 参数            | 功能          |
|----------------|---------------|
| url            | 页面地址          |
| titile         | 展示用titile文案     |
| showTitileBar  | 是否展示原生头部      |
| showBackIcon   | 是否展示原生回退按钮    |
| addTopPadding  | 是否适配刘海屏       |
| showRefreshBar | 是否展示下拉刷新      |
| pushData       | js调起新页面时的携带数据 |

```kotlin
        fun newInstance(
    url: String,
    title: String = "",
    showTitleBar: Boolean = true,
    showRightMenu: Boolean = false,
    showBackIcon: Boolean = true,
    addTopPadding: Boolean = false,
    showRefreshBar: Boolean = false,
    pushData: String = "",
): WebViewFragment
```

### 3、 js交互(JSBridge)

支持的js事件

| 参数            | 功能          |
|----------------|---------------|
| js_fun_setTitle            | 展示用titile文案         |
| js_fun_setOptionMenu         | 设置功能按钮    |
| js_fun_postNotification  | 发送意图通知     |
| js_fun_popWindow   | 推出页面    |
| js_fun_pushWindow  | 打开页面      |
| js_fun_init       | web初始化js |

js_fun_postNotification包含的js事件

```kotlin

/**
 * js调用通知的事件
 */
object JSNotificationAction {

    /**
     * 展示大图
     */
    const val jsshowGallery = "showGallery"

    /**
     * 弹出toast
     */
    const val jsShowToast = "showToast"


    /**
     * 获取用户数据，收到和回写一致
     */
    const val jsFetchUserInfo = "fetchUserInfo"

    /**
     * 获取用户数据，收到和回写一致
     */
    const val jsSignature = "signature"

    /**
     * 获取用户权限
     */
    const val jscheckPermission = "checkPermission"

    /**
     * js操作页面自动锁屏
     */
    const val jsAutolockScreen = "autolockScreen"

    /**
     * js事件标记当前页面为返回时的目标页面（应用场景：当有一个页面需要打开多个webView后续页面，处理完成后回到目标页面时使用）
     */
    const val jspopTo = "popTo"

    /**
     * js标记当前页面需要在onResume是刷新
     */
    const val jsneedRefreshOnResume = "needRefreshOnResume"

    /**
     * 刷新页面数据用
     */
    const val jsNeedReloadData = "needReloadData"
}

```

## 二、权限请求

DBXPermissionUtils中向外暴露了一个方法requestPermission

```kotlin
/**
 * 权限请求
 * @param context Context 上下文对象
 * @param notice String 提示文案
 * @param permissions Array<String> 权限集合
 * @param actionGranted Function0<Unit> 请求权限成功后执行的代码块
 * @param actionDenied Function0<Unit> 请求权限失败后执行的代码块
 */
fun requestPermission(
    context: Context,
    notice: String,
    permissions: Array<String>,
    actionGranted: () -> Unit,
    actionDenied: () -> Unit,
)
```

## 三、图片选择，拍照，上传

因图片选择等事件都需要原生Activity支持，所有做了一个中间页面JSBridgeActivity，在其内部完成需要Activity支持的事件处理

### 1、图片选择由ImagePicker负责

需要现在MainApplication中进行初始化

```kotlin
    ImagePreManager.init(this)
```

图片选择动作对入参进行了封装，直接调用ImagePickerExts中的方法即可实现

```kotlin
/**
 * Activity调起选择图片
 * @param act Activity 界面
 * @param maxCount Int 最大选择图片数
 * @param requestCode Int 请求code
 * @param mPhotoVideoPathList ArrayList<String> 已选中的图片地址
 * @param actionGrantedAfter Function0<Unit>? 因需要权限请求，该参数中传入的代码块会在请求权限成功后执行
 * @param showCamera Boolean 是否展示拍照按钮
 * @param showImage Boolean 是否展示图片
 * @param showVideo Boolean 是否展示视频
 * @param filterGif Boolean 是否过滤gif
 * @param setSingleType Boolean 是否只能单类型选中，即只能选图片或只能选视频
 * @param needWatermark Boolean 是否添加水印
 * @param actionDeniedAfter Function0<Unit>? 因需要权限请求，该参数中传入的代码块会在请求权限失败后执行
 */
fun startPickerImage(
    act: Activity,
    maxCount: Int,
    requestCode: Int,
    mPhotoVideoPathList: ArrayList<String> = arrayListOf(),
    actionGrantedAfter: (() -> Unit)? = null,
    showCamera: Boolean = true,
    showImage: Boolean = true,
    showVideo: Boolean = true,
    filterGif: Boolean = false,
    setSingleType: Boolean = false,
    needWatermark: Boolean = true,
    actionDeniedAfter: (() -> Unit)? = null,
) 
```

其中的拍照由CameraV2Activity页面完成功能，已封装了对应权限的请求功能

ImagePicker中的拍照视频默认由系统浏览器完成功能，但是为了统一处理需要交给CameraV2Activity完成工作，所以需要在MainApplication设置ImagePicker对应动作的回调，以调用CameraV2Activity页面

```kotlin
    private fun initImagePicker() {
    ImagePicker.getInstance().setCameraViewCallBack { _, type, callBack ->
        CameraV2Activity.invoke(this,
            if (type == ImagePicker.CameraView.type_all) {
                JCameraView.BUTTON_STATE_BOTH
            } else {
                JCameraView.BUTTON_STATE_ONLY_CAPTURE
            }, object : IUIKitCallBack {
                override fun onSuccess(data: Any) {
                    if (data is String) {
                        callBack.onSuccess(ImagePicker.CameraViewCallBack.type_photo, data)
                    } else if (data is Intent) {
                        val videoPath = data.getStringExtra(TUIKitConstants.CAMERA_VIDEO_PATH)
                        callBack.onSuccess(ImagePicker.CameraViewCallBack.type_video, videoPath)
                    }
                }

                override fun onError(module: String, errCode: Int, errMsg: String) {
                    callBack.onError(module, errCode, errMsg)
                }
            })
    }
}
```

### 2、上传模块(UploadViewModel)

其对外只提供一个上传方法，以回调的方式返回结果

```kotlin
    interface CallBack {
    fun onSuccess(ossTag: String, resultList: ArrayList<String>)
    fun showLoading(show: Boolean)
    fun onError(errorMsg: String)
}

fun uploadFile(fileList: ArrayList<String>, path: String, callBack: CallBack) {
    callBack.showLoading(true)
    OSSManager().startUploadFileList(fileList, path, callBack)
}
```

OSSManager中以协程封装了多文件上传的功能，具体的oss地址配置也在该类中

为了方便web页面，对上传的图片和视频进行了统一处理，视频要添加对应的封面图
WebUploadMediaHelper该类会对上传的图片视频文件进行甄别，如果是视频则对其进行截图，并在按照原有的文件顺序，对新的图片视频数据进行排序返回

## 四、我的页面

### 1、原生的我的tab页面

头部用户头像等，具体数据需要使用时具体写入

tab下的list功能可以通过json进行配置，目前定义的动作包括下面的几种

```kotlin

/**
 * 我的tab中可以执行的动作
 */
object MineListAction {
    /**
     * 空行
     */
    const val empty = "empty"

    /**
     * 设置中心
     */
    const val settingCenter = "settingCenter"

    /**
     * 扫码
     */
    const val QRCodeScanner = "QRCodeScanner"

    /**
     * 安全中心
     */
    const val about = "about"

    /**
     * web页面
     */
    const val web = "web"
}
```

json中可以配置的数据包括

```kotlin
/**
 * 我的tab下的list
 * @property name String 显示名称
 * @property iconRes Int 图标资源id
 * @property iconUrl String 图标url
 * @property action String 动作
 * @property redDot Int 未读红点数
 * @property url String 跳转url
 * @constructor
 */
data class MineItemBean(
    val name: String = "",
    val iconRes: Int = -1,
    val iconUrl: String = "",
    val action: String = MineListAction.empty,
    var redDot: Int = 0,
    val url: String = "",
) : Serializable
```











