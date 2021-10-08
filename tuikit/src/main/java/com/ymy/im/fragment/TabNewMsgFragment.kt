package com.ymy.im.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener
import com.tencent.imsdk.v2.V2TIMConversation
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMMessage
import com.tencent.qcloud.tim.uikit.R
import com.tencent.qcloud.tim.uikit.TUIKit
import com.tencent.qcloud.tim.uikit.base.ITitleBarLayout
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack
import com.tencent.qcloud.tim.uikit.component.action.PopActionClickListener
import com.tencent.qcloud.tim.uikit.component.action.PopDialogAdapter
import com.tencent.qcloud.tim.uikit.component.action.PopMenuAction
import com.tencent.qcloud.tim.uikit.databinding.FragmentTabMsgNewBinding
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo
import com.tencent.qcloud.tim.uikit.utils.PopWindowUtil
import com.tencent.qcloud.tim.uikit.utils.ToastUtil
import com.ymy.core.Ktx
import com.ymy.core.base.Refresher
import com.ymy.core.base.RootFragment
import com.ymy.core.exts.debounceOnMainThread
import com.ymy.core.notchtools.NotchTools
import com.ymy.core.router.RoutersIM
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.NetWorkUtils
import com.ymy.im.adapter.FunctionAdapter
import com.ymy.im.signature.Menu
import java.util.*

/**
 * Created on 1/15/21 10:38.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
@Route(path = RoutersIM.IM_MsgTabFragment)
class TabNewMsgFragment : RootFragment(), Refresher,
    LifecycleObserver {
    lateinit var mBinding: FragmentTabMsgNewBinding
    lateinit var mMenu: com.ymy.im.signature.Menu
    private val functionAdapter: FunctionAdapter by lazy { FunctionAdapter() }
    private val dotViews = HashMap<String, TextView>()
    private var mConversationPopList: ListView? = null
    private val mConversationPopAdapter: PopDialogAdapter by lazy {
        PopDialogAdapter()
    }
    private var mConversationPopWindow: PopupWindow? = null
    private val mConversationPopActions: MutableList<PopMenuAction> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentTabMsgNewBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initData()
    }

    private fun initData() {
        ConversationManagerKit.getInstance().loadConversation(null)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initView() {
        // 从布局文件中获取会话列表面板
        mBinding.conversationTitle.run {
            setTitle("消息", ITitleBarLayout.POSITION.MIDDLE)
            leftGroup.visibility = View.GONE
            setRightIcon(R.drawable.conversation_more)
            setRightIcon2(R.drawable.conversation_search)
            mMenu = com.ymy.im.signature.Menu(activity, this, Menu.MENU_TYPE_CONVERSATION)
            setOnRightClickListener {
                if (mMenu.isShowing) {
                    mMenu.hide()
                } else {
                    mMenu.show()
                }
            }
            setOnRightClickListener2 {
                com.ymy.im.activity.SearchGroupNameActivity.invoke(
                    context
                )
            }
        }
        setNotch()
        initConversationList()
    }

    private fun initConversationList() {
        mBinding.conversationLayout.run {
            // 会话列表面板的默认UI和交互初始化
            initDefault()
            // 通过API设置ConversataonLayout各种属性的样例，开发者可以打开注释，体验效果
            conversationList
                .setOnItemClickListener { _, _, conversationInfo ->
                    //此处为demo的实现逻辑，更根据会话类型跳转到相关界面，开发者可根据自己的应用场景灵活实现
                    conversationInfo.run {
                        val chatInfo = ChatInfo()
                        chatInfo.type =
                            if (isGroup) V2TIMConversation.V2TIM_GROUP else V2TIMConversation.V2TIM_C2C
                        chatInfo.id = id
                        chatInfo.groupType = if (isGroup) event_group_type else ""
                        chatInfo.chatName = title
                        startChatActivity(chatInfo)
                    }
                }
            conversationList
                .setOnItemLongClickListener { view, position, conversationInfo ->
                    startPopShow(
                        view,
                        position,
                        conversationInfo
                    )
                }
        }
        initPopMenuAction()
    }

    private fun startChatActivity(chatInfo: ChatInfo) {
        val intent = Intent(TUIKit.getAppContext(), com.ymy.im.activity.ImChatActivity::class.java)
        intent.putExtra(com.ymy.im.utils.Constants.CHAT_INFO, chatInfo)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        TUIKit.getAppContext().startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (context?.let { NetWorkUtils.isNetworkAvailable(it) } == true && V2TIMManager.getInstance().loginStatus != V2TIMManager.V2TIM_STATUS_LOGINED) {
            com.ymy.im.helper.ImHelper.login(YmyUserManager.user.imId, null)
        }
        mBinding.conversationLayout.onResume()
        com.ymy.im.helper.ImHelper.needShowNotification = false
    }

    override fun onPause() {
        super.onPause()
        com.ymy.im.helper.ImHelper.needShowNotification = true
    }

    private fun setNotch() {
        val activity = activity
        if (activity != null) {
            val window = activity.window
            val statusHeight = NotchTools.getFullScreenTools().getStatusHeight(window)
            mBinding.conversationTitle.setPadding(0, statusHeight, 0, 0)
        }
    }

    val msgListener: V2TIMAdvancedMsgListener = object : V2TIMAdvancedMsgListener() {
        override fun onRecvNewMessage(msg: V2TIMMessage?) {
            getIMData()
        }
    }

    override fun onStart() {
        super.onStart()
        V2TIMManager.getMessageManager()
            .addAdvancedMsgListener(msgListener)
    }

    override fun onStop() {
        super.onStop()
        V2TIMManager.getMessageManager()
            .removeAdvancedMsgListener(msgListener)
    }

    /**
     * app切换到后台时，再打开时需要刷新url
     */
    private var appForeGroundStatusChange = false

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        appForeGroundStatusChange = true
    }

    override fun onRefresh() {
        getIMData()
        if (appForeGroundStatusChange) {
            com.ymy.im.helper.ImHelper.getDBXSendReq().getCompanyFunctionData()
            appForeGroundStatusChange = false
        }
    }

    private fun getIMData() {
        debounceIMRefresh.invoke(null)
    }

    private val debounceIMRefresh = debounceOnMainThread<Any?>(500) {
        ConversationManagerKit.getInstance().loadConversation(object :
            IUIKitCallBack {
            override fun onSuccess(data: Any) {
            }

            override fun onError(module: String, errCode: Int, errMsg: String) {
                ToastUtil.toastLongMessage("加载消息失败")
            }
        })
    }


    private fun initPopMenuAction() {
        // 设置长按conversation显示PopAction
        val conversationPopActions: MutableList<PopMenuAction> = ArrayList()
        val action = PopMenuAction()
        action.actionClickListener =
            PopActionClickListener { position, data ->
                mBinding.conversationLayout.deleteConversation(
                    position,
                    data as ConversationInfo
                )
            }
        action.actionName = Ktx.app.resources.getString(R.string.chat_delete)
        conversationPopActions.add(action)
        mConversationPopActions.clear()
        mConversationPopActions.addAll(conversationPopActions)
    }

    /**
     * 长按会话item弹框
     *
     * @param index            会话序列号
     * @param conversationInfo 会话数据对象
     * @param locationX        长按时X坐标
     * @param locationY        长按时Y坐标
     */
    private fun showItemPopMenu(
        index: Int,
        conversationInfo: ConversationInfo,
        locationX: Int,
        locationY: Int
    ) {
        if (mConversationPopActions == null || mConversationPopActions.size == 0) return
        val itemPop = LayoutInflater.from(activity).inflate(R.layout.pop_menu_layout, null)
        mConversationPopList = itemPop.findViewById<ListView>(R.id.pop_menu_list)
        mConversationPopList?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val action: PopMenuAction = mConversationPopActions.get(position)
                if (action.actionClickListener != null) {
                    action.actionClickListener.onActionClick(index, conversationInfo)
                }
                mConversationPopWindow?.dismiss()
            }

        mConversationPopList?.adapter = mConversationPopAdapter
        mConversationPopAdapter.setDataSource(mConversationPopActions)
        mConversationPopWindow = PopWindowUtil.popupWindow(
            itemPop, mBinding.root,
            locationX, locationY
        )
        mBinding.root.postDelayed(
            Runnable { mConversationPopWindow?.dismiss() },
            10000
        ) // 10s后无操作自动消失
    }

    private fun startPopShow(view: View, position: Int, info: ConversationInfo) {
        val locationArray = IntArray(2)
        view.getLocationOnScreen(locationArray)
        showItemPopMenu(position, info, locationArray[0], locationArray[1] - view.height / 2)
    }
}